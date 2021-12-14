package eu.mulk.jgvariant.core;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * Type class for decodable {@link Variant} types.
 *
 * <p>Use the {@code of*} family of constructor methods to acquire a suitable {@link Decoder} for
 * the type you wish to decode.
 *
 * <p><strong>Example</strong>
 *
 * <p>To parse a GVariant of type {@code "a(si)"}, which is an array of pairs of {@link String} and
 * {@code int}, you can use the following code:
 *
 * <pre>{@code
 * record ExampleRecord(String s, int i) {}
 *
 * var decoder =
 *   Decoder.ofArray(
 *     Decoder.ofStructure(
 *       ExampleRecord.class,
 *       Decoder.ofString(UTF_8),
 *       Decoder.ofInt().withByteOrder(LITTLE_ENDIAN)));
 *
 * byte[] bytes = ...;
 * List<ExampleRecord> example = decoder.decode(ByteBuffer.wrap(bytes));
 * }</pre>
 *
 * @param <T> the type that the {@link Decoder} can decode.
 */
@SuppressWarnings("java:S1610")
public abstract class Decoder<T> {

  private Decoder() {}

  /**
   * @throws java.nio.BufferUnderflowException if the byte buffer is shorter than the requested
   *     data.
   */
  public abstract T decode(ByteBuffer byteSlice);

  abstract byte alignment();

  @Nullable
  abstract Integer fixedSize();

  final boolean hasFixedSize() {
    return fixedSize() != null;
  }

  /**
   * Switches the input {@link ByteBuffer} to a given {@link ByteOrder} before reading from it.
   *
   * @param byteOrder the byte order to use.
   * @return a new, decorated {@link Decoder}.
   */
  public Decoder<T> withByteOrder(ByteOrder byteOrder) {
    var delegate = this;

    return new Decoder<>() {
      @Override
      public byte alignment() {
        return delegate.alignment();
      }

      @Override
      public @Nullable Integer fixedSize() {
        return delegate.fixedSize();
      }

      @Override
      public T decode(ByteBuffer byteSlice) {
        byteSlice.order(byteOrder);
        return delegate.decode(byteSlice);
      }
    };
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
   * Creates a {@link Decoder} for a {@code Structure} type.
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
   * Creates a {@link Decoder} for the {@link Variant} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Variant> ofVariant() {
    return new VariantDecoder();
  }

  /**
   * Creates a {@link Decoder} for the {@code Boolean} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Boolean> ofBoolean() {
    return new BooleanDecoder();
  }

  /**
   * Creates a {@link Decoder} for the 8-bit {@ode byte} type.
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
    Integer fixedSize() {
      return null;
    }

    @Override
    public List<U> decode(ByteBuffer byteSlice) {
      List<U> elements;

      var elementSize = elementDecoder.fixedSize();
      if (elementSize != null) {
        // A simple C-style array.
        elements = new ArrayList<>(byteSlice.limit() / elementSize);
        for (int i = 0; i < byteSlice.limit(); i += elementSize) {
          var element = elementDecoder.decode(byteSlice.slice(i, elementSize));
          elements.add(element);
        }
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
          elements.add(elementDecoder.decode(byteSlice.slice(position, framingOffset - position)));
          position = align(framingOffset, alignment());
        }
      }

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
    public Optional<U> decode(ByteBuffer byteSlice) {
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

    private final RecordComponent[] recordComponents;
    private final Class<U> recordType;
    private final Decoder<?>[] componentDecoders;

    StructureDecoder(Class<U> recordType, Decoder<?>... componentDecoders) {
      var recordComponents = recordType.getRecordComponents();

      if (componentDecoders.length != recordComponents.length) {
        throw new IllegalArgumentException(
            "number of decoders (%d) does not match number of structure components (%d)"
                .formatted(componentDecoders.length, recordComponents.length));
      }

      this.recordComponents = recordComponents;
      this.recordType = recordType;
      this.componentDecoders = componentDecoders;
    }

    @Override
    public byte alignment() {
      return (byte) Arrays.stream(componentDecoders).mapToInt(Decoder::alignment).max().orElse(1);
    }

    @Override
    public Integer fixedSize() {
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
    public U decode(ByteBuffer byteSlice) {
      int framingOffsetSize = byteCount(byteSlice.limit());

      var recordConstructorArguments = new Object[recordComponents.length];

      int position = 0;
      int framingOffsetIndex = 0;
      int componentIndex = 0;
      for (var componentDecoder : componentDecoders) {
        position = align(position, componentDecoder.alignment());

        var fixedComponentSize = componentDecoder.fixedSize();
        if (fixedComponentSize != null) {
          recordConstructorArguments[componentIndex] =
              componentDecoder.decode(byteSlice.slice(position, fixedComponentSize));
          position += fixedComponentSize;
        } else {
          if (componentIndex == recordComponents.length - 1) {
            // The last component never has a framing offset.
            int endPosition = byteSlice.limit() - framingOffsetIndex * framingOffsetSize;
            recordConstructorArguments[componentIndex] =
                componentDecoder.decode(byteSlice.slice(position, endPosition - position));
            position = endPosition;
          } else {
            int framingOffset =
                getIntN(
                    byteSlice.slice(
                        byteSlice.limit() - (1 + framingOffsetIndex) * framingOffsetSize,
                        framingOffsetSize));
            recordConstructorArguments[componentIndex] =
                componentDecoder.decode(byteSlice.slice(position, framingOffset - position));
            position = framingOffset;
            ++framingOffsetIndex;
          }
        }

        ++componentIndex;
      }

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
    public Variant decode(ByteBuffer byteSlice) {
      // TODO
      throw new UnsupportedOperationException("not implemented");
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
    public Boolean decode(ByteBuffer byteSlice) {
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
    public Byte decode(ByteBuffer byteSlice) {
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
    public Short decode(ByteBuffer byteSlice) {
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
    public Integer decode(ByteBuffer byteSlice) {
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
    public Long decode(ByteBuffer byteSlice) {
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
    public Double decode(ByteBuffer byteSlice) {
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
    public String decode(ByteBuffer byteSlice) {
      byteSlice.limit(byteSlice.limit() - 1);
      return charset.decode(byteSlice).toString();
    }
  }
}
