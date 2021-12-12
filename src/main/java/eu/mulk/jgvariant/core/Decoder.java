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

  public static Decoder<Bool> ofBoolean() {
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
