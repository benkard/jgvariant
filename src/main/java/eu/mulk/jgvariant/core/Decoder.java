package eu.mulk.jgvariant.core;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import eu.mulk.jgvariant.core.Value.Array;
import eu.mulk.jgvariant.core.Value.Bool;
import eu.mulk.jgvariant.core.Value.Float64;
import eu.mulk.jgvariant.core.Value.Int16;
import eu.mulk.jgvariant.core.Value.Int32;
import eu.mulk.jgvariant.core.Value.Int64;
import eu.mulk.jgvariant.core.Value.Int8;
import eu.mulk.jgvariant.core.Value.Maybe;
import eu.mulk.jgvariant.core.Value.Str;
import eu.mulk.jgvariant.core.Value.Structure;
import eu.mulk.jgvariant.core.Value.Variant;
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
 * Type class for decodable {@link Value} types.
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
 * record ExampleRecord(Value.Str s, Value.Int32 i) {}
 *
 * var decoder =
 *   Decoder.ofArray(
 *     Decoder.ofStructure(
 *       ExampleRecord.class,
 *       Decoder.ofStr(UTF_8),
 *       Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN)));
 *
 * byte[] bytes = ...;
 * Value.Array<Value.Structure<ExampleRecord>> example = decoder.decode(ByteBuffer.wrap(bytes));
 * }</pre>
 *
 * @param <T> the type that the {@link Decoder} can decode.
 */
@SuppressWarnings("java:S1610")
public abstract class Decoder<T extends Value> {

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
   * Creates a {@link Decoder} for an {@link Array} type.
   *
   * @param elementDecoder a {@link Decoder} for the elements of the array.
   * @param <U> the element type.
   * @return a new {@link Decoder}.
   */
  public static <U extends Value> Decoder<Array<U>> ofArray(Decoder<U> elementDecoder) {
    return new Decoder<>() {
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
      public Array<U> decode(ByteBuffer byteSlice) {
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
            elements.add(
                elementDecoder.decode(byteSlice.slice(position, framingOffset - position)));
            position = align(framingOffset, alignment());
          }
        }

        return new Array<>(elements);
      }
    };
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

  /**
   * Creates a {@link Decoder} for a {@link Maybe} type.
   *
   * @param elementDecoder a {@link Decoder} for the contained element.
   * @param <U> the element type.
   * @return a new {@link Decoder}.
   */
  public static <U extends Value> Decoder<Maybe<U>> ofMaybe(Decoder<U> elementDecoder) {
    return new Decoder<>() {
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
      public Maybe<U> decode(ByteBuffer byteSlice) {
        if (!byteSlice.hasRemaining()) {
          return new Maybe<>(Optional.empty());
        } else {
          if (!elementDecoder.hasFixedSize()) {
            // Remove trailing zero byte.
            byteSlice.limit(byteSlice.limit() - 1);
          }

          return new Maybe<>(Optional.of(elementDecoder.decode(byteSlice)));
        }
      }
    };
  }

  /**
   * Creates a {@link Decoder} for a {@link Structure} type.
   *
   * @param recordType the {@link Record} type that represents the components of the structure.
   * @param componentDecoders a {@link Decoder} for each component of the structure.
   * @param <U> the {@link Record} type that represents the components of the structure.
   * @return a new {@link Decoder}.
   */
  @SafeVarargs
  public static <U extends Record> Decoder<Structure<U>> ofStructure(
      Class<U> recordType, Decoder<? extends Value>... componentDecoders) {
    var recordComponents = recordType.getRecordComponents();
    if (componentDecoders.length != recordComponents.length) {
      throw new IllegalArgumentException(
          "number of decoders (%d) does not match number of structure components (%d)"
              .formatted(componentDecoders.length, recordComponents.length));
    }

    return new Decoder<>() {
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
      public Structure<U> decode(ByteBuffer byteSlice) {
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
          return new Structure<>(recordConstructor.newInstance(recordConstructorArguments));
        } catch (NoSuchMethodException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException e) {
          throw new IllegalStateException(e);
        }
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Variant} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Variant> ofVariant() {
    return new Decoder<>() {
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
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Bool} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Bool> ofBool() {
    return new Decoder<>() {
      @Override
      public byte alignment() {
        return 1;
      }

      @Override
      public Integer fixedSize() {
        return 1;
      }

      @Override
      public Bool decode(ByteBuffer byteSlice) {
        return new Bool(byteSlice.get() != 0);
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Int8} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Int8> ofInt8() {
    return new Decoder<>() {
      @Override
      public byte alignment() {
        return 1;
      }

      @Override
      public Integer fixedSize() {
        return 1;
      }

      @Override
      public Int8 decode(ByteBuffer byteSlice) {
        return new Int8(byteSlice.get());
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Int16} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Int16> ofInt16() {
    return new Decoder<>() {
      @Override
      public byte alignment() {
        return 2;
      }

      @Override
      public Integer fixedSize() {
        return 2;
      }

      @Override
      public Int16 decode(ByteBuffer byteSlice) {
        return new Int16(byteSlice.getShort());
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Int32} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Int32> ofInt32() {
    return new Decoder<>() {
      @Override
      public byte alignment() {
        return 4;
      }

      @Override
      public Integer fixedSize() {
        return 4;
      }

      @Override
      public Int32 decode(ByteBuffer byteSlice) {
        return new Int32(byteSlice.getInt());
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Int64} type.
   *
   * <p><strong>Note:</strong> It is often useful to apply {@link #withByteOrder(ByteOrder)} to the
   * result of this method.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Int64> ofInt64() {
    return new Decoder<>() {
      @Override
      public byte alignment() {
        return 8;
      }

      @Override
      public Integer fixedSize() {
        return 8;
      }

      @Override
      public Int64 decode(ByteBuffer byteSlice) {
        return new Int64(byteSlice.getLong());
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Float64} type.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Float64> ofFloat64() {
    return new Decoder<>() {
      @Override
      public byte alignment() {
        return 8;
      }

      @Override
      public Integer fixedSize() {
        return 8;
      }

      @Override
      public Float64 decode(ByteBuffer byteSlice) {
        return new Float64(byteSlice.getDouble());
      }
    };
  }

  /**
   * Creates a {@link Decoder} for the {@link Str} type.
   *
   * <p><strong>Note:</strong> While GVariant does not prescribe any particular encoding, {@link
   * java.nio.charset.StandardCharsets#UTF_8} is the most common choice.
   *
   * @return a new {@link Decoder}.
   */
  public static Decoder<Str> ofStr(Charset charset) {
    return new Decoder<>() {
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
      public Str decode(ByteBuffer byteSlice) {
        byteSlice.limit(byteSlice.limit() - 1);
        return new Str(charset.decode(byteSlice).toString());
      }
    };
  }
}
