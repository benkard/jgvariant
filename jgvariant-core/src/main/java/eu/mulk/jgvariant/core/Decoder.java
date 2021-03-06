// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.core;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Type class for decodable types.
 *
 * <p>Use the {@code of*} family of constructor methods to acquire a suitable {@link Decoder} for
 * the type you wish to decode.
 *
 * <p><strong>Example</strong>
 *
 * <p>To parse a GVariant of type {@code "a(si)"}, which is an array of pairs of {@link String} and
 * {@code int}, you can use the following code:
 *
 * {@snippet lang="java" :
 * record ExampleRecord(String s, int i) {}
 *
 * var decoder =
 *   Decoder.ofArray(
 *     Decoder.ofStructure(
 *       ExampleRecord.class,
 *       Decoder.ofString(UTF_8),
 *       Decoder.ofInt().withByteOrder(LITTLE_ENDIAN)));
 *
 * byte[] bytes;
 * List<ExampleRecord> example = decoder.decode(ByteBuffer.wrap(bytes));
 * }
 *
 * @param <T> the type that the {@link Decoder} can decode.
 */
@SuppressWarnings("java:S1610")
@API(status = Status.EXPERIMENTAL)
public abstract class Decoder<T> {

  private Decoder() {}

  /**
   * Decodes a {@link ByteBuffer} holding a serialized GVariant into a value of type {@code T}.
   *
   * <p><strong>Note:</strong> Due to the way the GVariant serialization format works, it is
   * important that the start and end boundaries of the passed byte slice correspond to the actual
   * start and end of the serialized value. The format does generally not allow for the dynamic
   * discovery of the end of the data structure.
   *
   * @param byteSlice a byte slice holding a serialized GVariant.
   * @return the deserialized value.
   * @throws java.nio.BufferUnderflowException if the byte buffer is shorter than the requested
   *     data.
   * @throws IllegalArgumentException if the serialized GVariant is ill-formed
   */
  public abstract @NotNull T decode(ByteBuffer byteSlice);

  abstract byte alignment();

  abstract @Nullable Integer fixedSize();

  final boolean hasFixedSize() {
    return fixedSize() != null;
  }

  /**
   * Switches the input {@link ByteBuffer} to a given {@link ByteOrder} before reading from it.
   *
   * @param byteOrder the byte order to use.
   * @return a new, decorated {@link Decoder}.
   */
  public final Decoder<T> withByteOrder(ByteOrder byteOrder) {
    return new ByteOrderFixingDecoder(byteOrder);
  }

  /**
   * Creates a new {@link Decoder} from an existing one by applying a function to the result.
   *
   * @param function the function to apply.
   * @return a new, decorated {@link Decoder}.
   * @see java.util.stream.Stream#map
   */
  public final <U> Decoder<U> map(Function<@NotNull T, @NotNull U> function) {
    return new MappingDecoder<>(function);
  }

  /**
   * Creates a new {@link Decoder} from an existing one by applying a function to the input.
   *
   * @param function the function to apply.
   * @return a new, decorated {@link Decoder}.
   * @see java.util.stream.Stream#map
   */
  public final Decoder<T> contramap(UnaryOperator<ByteBuffer> function) {
    return new ContramappingDecoder(function);
  }

  /**
   * Creates a new {@link Decoder} that delegates to one of two other {@link Decoder}s based on a
   * condition on the input {@link ByteBuffer}.
   *
   * @param selector the predicate to use to determine the decoder to use.
   * @return a new {@link Decoder}.
   */
  public static <U> Decoder<U> ofPredicate(
      Predicate<ByteBuffer> selector, Decoder<U> thenDecoder, Decoder<U> elseDecoder) {
    return new PredicateDecoder<>(selector, thenDecoder, elseDecoder);
  }

  /**
   * Creates a {@link Decoder} for an {@code Array} type.
   *
   * @param elementDecoder a {@link Decoder} for the elements of the array.
   * @param <U> the element type.
   * @return a new {@link Decoder}.
   */
  public static <U> Decoder<List<U>> ofArray(Decoder<U> elementDecoder) {
    return new ArrayDecoder<>(elementDecoder);
  }

  /**
   * Creates a {@link Decoder} for a {@code Dictionary} type.
   *
   * @param keyDecoder a {@link Decoder} for the key component of the dictionary entry.
   * @param valueDecoder a {@link Decoder} for the value component of the dictionary entry.
   * @return a new {@link Decoder}.
   */
  public static <K, V> Decoder<Map<K, V>> ofDictionary(
      Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
    return new DictionaryDecoder<>(keyDecoder, valueDecoder);
  }

  /**
   * Creates a {@link Decoder} for an {@code Array} type of element type {@code byte} into a
   * primitive {@code byte[]} array.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<byte[]> ofByteArray() {
    return new ByteArrayDecoder();
  }

  /**
   * Creates a {@link Decoder} for a {@code Maybe} type.
   *
   * @param elementDecoder a {@link Decoder} for the contained element.
   * @param <U> the element type.
   * @return a new {@link Decoder}.
   */
  public static <U> Decoder<Optional<U>> ofMaybe(Decoder<U> elementDecoder) {
    return new MaybeDecoder<>(elementDecoder);
  }

  /**
   * Creates a {@link Decoder} for a {@code Structure} type, decoding into a {@link Record}.
   *
   * @param recordType the {@link Record} type that represents the components of the structure.
   * @param componentDecoders a {@link Decoder} for each component of the structure.
   * @param <U> the {@link Record} type that represents the components of the structure.
   * @return a new {@link Decoder}.
   */
  public static <U extends Record> Decoder<U> ofStructure(
      Class<U> recordType, Decoder<?>... componentDecoders) {
    return new StructureDecoder<>(recordType, componentDecoders);
  }

  /**
   * Creates a {@link Decoder} for a {@code Structure} type, decoding into a {@link List}.
   *
   * <p>Prefer {@link #ofStructure(Class, Decoder[])} if possible, which is both more type-safe and
   * more convenient.
   *
   * @param componentDecoders a {@link Decoder} for each component of the structure.
   * @return a new {@link Decoder}.
   */
  public static Decoder<Object[]> ofStructure(Decoder<?>... componentDecoders) {
    return new TupleDecoder(componentDecoders);
  }

  /**
   * Creates a {@link Decoder} for a {@code Dictionary Entry} type, decoding into a {@link
   * Map.Entry}.
   *
   * @param keyDecoder a {@link Decoder} for the key component of the dictionary entry.
   * @param valueDecoder a {@link Decoder} for the value component of the dictionary entry.
   * @return a new {@link Decoder}.
   */
  public static <K, V> Decoder<Map.Entry<K, V>> ofDictionaryEntry(
      Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
    return new DictionaryEntryDecoder<>(keyDecoder, valueDecoder);
  }

  /**
   * Creates a {@link Decoder} for the {@link Variant} type.
   *
   * <p>The contained {@link Object} can be of one of the following types:
   *
   * <ul>
   *   <li>{@link Boolean}
   *   <li>{@link Byte}
   *   <li>{@link Short}
   *   <li>{@link Integer}
   *   <li>{@link Long}
   *   <li>{@link String}
   *   <li>{@link Optional} (a GVariant {@code Maybe} type)
   *   <li>{@link List} (a GVariant array)
   *   <li>{@code Object[]} (a GVariant structure)
   *   <li>{@link java.util.Map} (a dictionary)
   *   <li>{@link java.util.Map.Entry} (a dictionary entry)
   *   <li>{@link Variant} (a nested variant)
   * </ul>
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Variant> ofVariant() {
    return new VariantDecoder();
  }

  /**
   * Creates a {@link Decoder} for the {@code boolean} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Boolean> ofBoolean() {
    return new BooleanDecoder();
  }

  /**
   * Creates a {@link Decoder} for the 8-bit {@code byte} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Byte> ofByte() {
    return new ByteDecoder();
  }

  /**
   * Creates a {@link Decoder} for the 16-bit {@code short} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Short> ofShort() {
    return new ShortDecoder();
  }

  /**
   * Creates a {@link Decoder} for the 32-bit {@code int} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Integer> ofInt() {
    return new IntegerDecoder();
  }

  /**
   * Creates a {@link Decoder} for the 64-bit {@code long} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Long> ofLong() {
    return new LongDecoder();
  }

  /**
   * Creates a {@link Decoder} for the {@code double} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Double> ofDouble() {
    return new DoubleDecoder();
  }

  /**
   * Creates a {@link Decoder} for the {@link String} type.
   *
   * <p><strong>Note:</strong> While GVariant does not prescribe any particular encoding, {@link
   * java.nio.charset.StandardCharsets#UTF_8} is the most common choice.
   *
   * @param charset the {@link Charset} the string is encoded in.
   * @return a new {@link Decoder}.
   */
  public static Decoder<String> ofString(Charset charset) {
    return new StringDecoder(charset);
  }

  private static int align(int offset, byte alignment) {
    return offset % alignment == 0 ? offset : offset + alignment - (offset % alignment);
  }

  private static int getIntN(ByteBuffer byteSlice) {
    var intBytes = new byte[4];
    byteSlice.get(intBytes, 0, Math.min(4, byteSlice.limit()));
    return ByteBuffer.wrap(intBytes).order(LITTLE_ENDIAN).getInt();
  }

  @SuppressWarnings("java:S3358")
  private static int byteCount(int n) {
    return n < (1 << 8) ? 1 : n < (1 << 16) ? 2 : 4;
  }

  private static class ArrayDecoder<U> extends Decoder<List<U>> {

    private final Decoder<U> elementDecoder;

    ArrayDecoder(Decoder<U> elementDecoder) {
      this.elementDecoder = elementDecoder;
    }

    @Override
    public byte alignment() {
      return elementDecoder.alignment();
    }

    @Override
    @Nullable
    public Integer fixedSize() {
      return null;
    }

    @Override
    public @NotNull List<U> decode(ByteBuffer byteSlice) {
      List<U> elements;

      var elementSize = elementDecoder.fixedSize();
      if (elementSize != null) {
        // A simple C-style array.
        elements = new ArrayList<>(byteSlice.limit() / elementSize);
        for (int i = 0; i < byteSlice.limit(); i += elementSize) {
          var element = elementDecoder.decode(slicePreservingOrder(byteSlice, i, elementSize));
          elements.add(element);
        }
      } else if (byteSlice.limit() == 0) {
        // A degenerate zero-length array of variable width.
        elements = List.of();
      } else {
        // An array with aligned elements and a vector of framing offsets in the end.
        int framingOffsetSize = byteCount(byteSlice.limit());
        int lastFramingOffset =
            getIntN(byteSlice.slice(byteSlice.limit() - framingOffsetSize, framingOffsetSize));
        int elementCount = (byteSlice.limit() - lastFramingOffset) / framingOffsetSize;

        elements = new ArrayList<>(elementCount);
        int position = 0;
        for (int i = 0; i < elementCount; i++) {
          int framingOffset =
              getIntN(
                  byteSlice.slice(lastFramingOffset + i * framingOffsetSize, framingOffsetSize));
          elements.add(
              elementDecoder.decode(
                  slicePreservingOrder(byteSlice, position, framingOffset - position)));
          position = align(framingOffset, alignment());
        }
      }

      return elements;
    }
  }

  private static class DictionaryDecoder<K, V> extends Decoder<Map<K, V>> {

    private final ArrayDecoder<Map.Entry<K, V>> entryArrayDecoder;

    DictionaryDecoder(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
      this.entryArrayDecoder =
          new ArrayDecoder<>(new DictionaryEntryDecoder<>(keyDecoder, valueDecoder));
    }

    @Override
    public byte alignment() {
      return entryArrayDecoder.alignment();
    }

    @Override
    @Nullable
    public Integer fixedSize() {
      return entryArrayDecoder.fixedSize();
    }

    @Override
    public @NotNull Map<K, V> decode(ByteBuffer byteSlice) {
      List<Map.Entry<K, V>> entries = entryArrayDecoder.decode(byteSlice);
      return entries.stream().collect(toMap(Entry::getKey, Entry::getValue));
    }
  }

  private static class ByteArrayDecoder extends Decoder<byte[]> {

    private static final int ELEMENT_SIZE = 1;

    @Override
    public byte alignment() {
      return ELEMENT_SIZE;
    }

    @Override
    @Nullable
    Integer fixedSize() {
      return null;
    }

    @Override
    public byte @NotNull [] decode(ByteBuffer byteSlice) {
      // A simple C-style array.
      byte[] elements = new byte[byteSlice.limit() / ELEMENT_SIZE];
      byteSlice.get(elements);
      return elements;
    }
  }

  private static class MaybeDecoder<U> extends Decoder<Optional<U>> {

    private final Decoder<U> elementDecoder;

    MaybeDecoder(Decoder<U> elementDecoder) {
      this.elementDecoder = elementDecoder;
    }

    @Override
    public byte alignment() {
      return elementDecoder.alignment();
    }

    @Override
    @Nullable
    Integer fixedSize() {
      return null;
    }

    @Override
    public @NotNull Optional<U> decode(ByteBuffer byteSlice) {
      if (!byteSlice.hasRemaining()) {
        return Optional.empty();
      } else {
        if (!elementDecoder.hasFixedSize()) {
          // Remove trailing zero byte.
          byteSlice.limit(byteSlice.limit() - 1);
        }

        return Optional.of(elementDecoder.decode(byteSlice));
      }
    }
  }

  private static class StructureDecoder<U extends Record> extends Decoder<U> {

    private final Class<U> recordType;
    private final TupleDecoder tupleDecoder;

    StructureDecoder(Class<U> recordType, Decoder<?>... componentDecoders) {
      var recordComponents = recordType.getRecordComponents();
      if (componentDecoders.length != recordComponents.length) {
        throw new IllegalArgumentException(
            "number of decoders (%d) does not match number of structure components (%d)"
                .formatted(componentDecoders.length, recordComponents.length));
      }

      this.recordType = recordType;
      this.tupleDecoder = new TupleDecoder(componentDecoders);
    }

    @Override
    public byte alignment() {
      return tupleDecoder.alignment();
    }

    @Override
    public @Nullable Integer fixedSize() {
      return tupleDecoder.fixedSize();
    }

    @Override
    public @NotNull U decode(ByteBuffer byteSlice) {
      Object[] recordConstructorArguments = tupleDecoder.decode(byteSlice);

      try {
        var recordComponentTypes =
            Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        var recordConstructor = recordType.getDeclaredConstructor(recordComponentTypes);
        return recordConstructor.newInstance(recordConstructorArguments);
      } catch (NoSuchMethodException
          | InstantiationException
          | IllegalAccessException
          | InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static class TupleDecoder extends Decoder<Object[]> {

    private final Decoder<?>[] componentDecoders;

    TupleDecoder(Decoder<?>... componentDecoders) {
      this.componentDecoders = componentDecoders;
    }

    @Override
    public byte alignment() {
      return (byte) Arrays.stream(componentDecoders).mapToInt(Decoder::alignment).max().orElse(1);
    }

    @Override
    public @Nullable Integer fixedSize() {
      int position = 0;
      for (var componentDecoder : componentDecoders) {
        var fixedComponentSize = componentDecoder.fixedSize();
        if (fixedComponentSize == null) {
          return null;
        }

        position = align(position, componentDecoder.alignment());
        position += fixedComponentSize;
      }

      if (position == 0) {
        return 1;
      }

      return align(position, alignment());
    }

    @Override
    public Object @NotNull [] decode(ByteBuffer byteSlice) {
      int framingOffsetSize = byteCount(byteSlice.limit());

      var objects = new Object[componentDecoders.length];

      int position = 0;
      int framingOffsetIndex = 0;
      int componentIndex = 0;
      for (var componentDecoder : componentDecoders) {
        position = align(position, componentDecoder.alignment());

        var fixedComponentSize = componentDecoder.fixedSize();
        if (fixedComponentSize != null) {
          objects[componentIndex] =
              componentDecoder.decode(
                  slicePreservingOrder(byteSlice, position, fixedComponentSize));
          position += fixedComponentSize;
        } else {
          if (componentIndex == componentDecoders.length - 1) {
            // The last component never has a framing offset.
            int endPosition = byteSlice.limit() - framingOffsetIndex * framingOffsetSize;
            objects[componentIndex] =
                componentDecoder.decode(
                    slicePreservingOrder(byteSlice, position, endPosition - position));
            position = endPosition;
          } else {
            int framingOffset =
                getIntN(
                    byteSlice.slice(
                        byteSlice.limit() - (1 + framingOffsetIndex) * framingOffsetSize,
                        framingOffsetSize));
            objects[componentIndex] =
                componentDecoder.decode(
                    slicePreservingOrder(byteSlice, position, framingOffset - position));
            position = framingOffset;
            ++framingOffsetIndex;
          }
        }

        ++componentIndex;
      }

      return objects;
    }
  }

  private static class DictionaryEntryDecoder<K, V> extends Decoder<Map.Entry<K, V>> {

    private final TupleDecoder tupleDecoder;

    DictionaryEntryDecoder(Decoder<K> keyDecoder, Decoder<V> valueDecoder) {
      this.tupleDecoder = new TupleDecoder(keyDecoder, valueDecoder);
    }

    @Override
    public byte alignment() {
      return tupleDecoder.alignment();
    }

    @Override
    public @Nullable Integer fixedSize() {
      return tupleDecoder.fixedSize();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map.@NotNull Entry<K, V> decode(ByteBuffer byteSlice) {
      Object[] components = tupleDecoder.decode(byteSlice);
      return new SimpleImmutableEntry<>((K) components[0], (V) components[1]);
    }
  }

  private static class VariantDecoder extends Decoder<Variant> {

    @Override
    public byte alignment() {
      return 8;
    }

    @Override
    @Nullable
    Integer fixedSize() {
      return null;
    }

    @Override
    public @NotNull Variant decode(ByteBuffer byteSlice) {
      for (int i = byteSlice.limit() - 1; i >= 0; --i) {
        if (byteSlice.get(i) != 0) {
          continue;
        }

        var dataBytes = slicePreservingOrder(byteSlice, 0, i);
        var signatureBytes = byteSlice.slice(i + 1, byteSlice.limit() - (i + 1));

        Signature signature;
        try {
          signature = Signature.parse(signatureBytes);
        } catch (ParseException e) {
          throw new IllegalArgumentException(e);
        }

        return new Variant(signature, signature.decoder().decode(dataBytes));
      }

      throw new IllegalArgumentException("variant signature not found");
    }
  }

  private static class BooleanDecoder extends Decoder<Boolean> {

    @Override
    public byte alignment() {
      return 1;
    }

    @Override
    public Integer fixedSize() {
      return 1;
    }

    @Override
    public @NotNull Boolean decode(ByteBuffer byteSlice) {
      return byteSlice.get() != 0;
    }
  }

  private static class ByteDecoder extends Decoder<Byte> {

    @Override
    public byte alignment() {
      return 1;
    }

    @Override
    public Integer fixedSize() {
      return 1;
    }

    @Override
    public @NotNull Byte decode(ByteBuffer byteSlice) {
      return byteSlice.get();
    }
  }

  private static class ShortDecoder extends Decoder<Short> {

    @Override
    public byte alignment() {
      return 2;
    }

    @Override
    public Integer fixedSize() {
      return 2;
    }

    @Override
    public @NotNull Short decode(ByteBuffer byteSlice) {
      return byteSlice.getShort();
    }
  }

  private static class IntegerDecoder extends Decoder<Integer> {

    @Override
    public byte alignment() {
      return 4;
    }

    @Override
    public Integer fixedSize() {
      return 4;
    }

    @Override
    public @NotNull Integer decode(ByteBuffer byteSlice) {
      return byteSlice.getInt();
    }
  }

  private static class LongDecoder extends Decoder<Long> {

    @Override
    public byte alignment() {
      return 8;
    }

    @Override
    public Integer fixedSize() {
      return 8;
    }

    @Override
    public @NotNull Long decode(ByteBuffer byteSlice) {
      return byteSlice.getLong();
    }
  }

  private static class DoubleDecoder extends Decoder<Double> {

    @Override
    public byte alignment() {
      return 8;
    }

    @Override
    public Integer fixedSize() {
      return 8;
    }

    @Override
    public @NotNull Double decode(ByteBuffer byteSlice) {
      return byteSlice.getDouble();
    }
  }

  private static class StringDecoder extends Decoder<String> {

    private final Charset charset;

    public StringDecoder(Charset charset) {
      this.charset = charset;
    }

    @Override
    public byte alignment() {
      return 1;
    }

    @Override
    @Nullable
    Integer fixedSize() {
      return null;
    }

    @Override
    public @NotNull String decode(ByteBuffer byteSlice) {
      byteSlice.limit(byteSlice.limit() - 1);
      return charset.decode(byteSlice).toString();
    }
  }

  private class MappingDecoder<U> extends Decoder<U> {

    private final Function<@NotNull T, @NotNull U> function;

    MappingDecoder(Function<@NotNull T, @NotNull U> function) {
      this.function = function;
    }

    @Override
    public byte alignment() {
      return Decoder.this.alignment();
    }

    @Override
    public @Nullable Integer fixedSize() {
      return Decoder.this.fixedSize();
    }

    @Override
    public @NotNull U decode(ByteBuffer byteSlice) {
      return function.apply(Decoder.this.decode(byteSlice));
    }
  }

  private class ContramappingDecoder extends Decoder<T> {

    private final UnaryOperator<ByteBuffer> function;

    ContramappingDecoder(UnaryOperator<ByteBuffer> function) {
      this.function = function;
    }

    @Override
    public byte alignment() {
      return Decoder.this.alignment();
    }

    @Override
    public @Nullable Integer fixedSize() {
      return Decoder.this.fixedSize();
    }

    @Override
    public @NotNull T decode(ByteBuffer byteSlice) {
      var transformedBuffer = function.apply(byteSlice.asReadOnlyBuffer().order(byteSlice.order()));
      return Decoder.this.decode(transformedBuffer);
    }
  }

  private class ByteOrderFixingDecoder extends Decoder<T> {

    private final ByteOrder byteOrder;

    ByteOrderFixingDecoder(ByteOrder byteOrder) {
      this.byteOrder = byteOrder;
    }

    @Override
    public byte alignment() {
      return Decoder.this.alignment();
    }

    @Override
    public @Nullable Integer fixedSize() {
      return Decoder.this.fixedSize();
    }

    @Override
    public @NotNull T decode(ByteBuffer byteSlice) {
      var newByteSlice = byteSlice.duplicate();
      newByteSlice.order(byteOrder);
      return Decoder.this.decode(newByteSlice);
    }
  }

  private static ByteBuffer slicePreservingOrder(ByteBuffer byteSlice, int index, int length) {
    return byteSlice.slice(index, length).order(byteSlice.order());
  }

  private static class PredicateDecoder<U> extends Decoder<U> {

    private final Predicate<ByteBuffer> selector;
    private final Decoder<U> thenDecoder;
    private final Decoder<U> elseDecoder;

    PredicateDecoder(
        Predicate<ByteBuffer> selector, Decoder<U> thenDecoder, Decoder<U> elseDecoder) {
      this.selector = selector;
      this.thenDecoder = thenDecoder;
      this.elseDecoder = elseDecoder;
      if (thenDecoder.alignment() != elseDecoder.alignment()) {
        throw new IllegalArgumentException(
            "incompatible alignments in predicate branches: then=%d, else=%d"
                .formatted(thenDecoder.alignment(), elseDecoder.alignment()));
      }

      if (!Objects.equals(thenDecoder.fixedSize(), elseDecoder.fixedSize())) {
        throw new IllegalArgumentException(
            "incompatible sizes in predicate branches: then=%s, else=%s"
                .formatted(
                    Objects.requireNonNullElse(thenDecoder.fixedSize(), "(null)"),
                    Objects.requireNonNullElse(elseDecoder.fixedSize(), "(null)")));
      }
    }

    @Override
    public byte alignment() {
      return thenDecoder.alignment();
    }

    @Override
    public @Nullable Integer fixedSize() {
      return thenDecoder.fixedSize();
    }

    @Override
    public @NotNull U decode(ByteBuffer byteSlice) {
      var b = selector.test(byteSlice);
      byteSlice.rewind();
      return b ? thenDecoder.decode(byteSlice) : elseDecoder.decode(byteSlice);
    }
  }
}
