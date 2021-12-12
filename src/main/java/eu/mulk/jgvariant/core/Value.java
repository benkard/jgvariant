package eu.mulk.jgvariant.core;

import java.util.List;
import java.util.Optional;

/**
 * A value representable by the <a href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a>
 * serialization format.
 *
 * <p>{@link Value} is a sum type (sealed interface) that represents a GVariant value. Its subtypes
 * represent the different types of values that GVariant supports.
 *
 * @see Decoder
 */
public sealed interface Value {

  /**
   * A homogeneous sequence of GVariant values.
   *
   * <p>Arrays of fixed width (i.e. of values of fixed size) are represented in a similar way to
   * plain C arrays. Arrays of variable width require additional space for padding and framing.
   *
   * <p>Heterogeneous sequences are represented by {@code Array<Variant>}.
   *
   * @param <T> the type of the elements of the array.
   * @see Decoder#ofArray
   */
  record Array<T extends Value>(List<T> values) implements Value {}

  /**
   * A value that is either present or absent.
   *
   * @param <T> the contained type.
   * @see Decoder#ofMaybe
   */
  record Maybe<T extends Value>(Optional<T> value) implements Value {}

  /**
   * A tuple of values of fixed types.
   *
   * <p>GVariant structures are represented as {@link Record} types. For example, a two-element
   * structure consisting of a string and an int can be modelled as follows:
   *
   * <pre>{@code
   * record TestRecord(Str s, Int32 i) {}
   * var testStruct = new Structure<>(new TestRecord(new Str("hello"), new Int32(123));
   * }</pre>
   *
   * @param <T> the {@link Record} type that represents the components of the structure.
   * @see Decoder#ofStructure
   */
  record Structure<T extends Record>(T values) implements Value {}

  /**
   * A dynamically typed box that can hold a single value of any GVariant type.
   *
   * @see Decoder#ofVariant
   */
  record Variant(Class<? extends Value> type, Value value) implements Value {}

  /**
   * Either true or false.
   *
   * @see Decoder#ofBool()
   */
  record Bool(boolean value) implements Value {
    static Bool TRUE = new Bool(true);
    static Bool FALSE = new Bool(false);
  }

  /**
   * A {@code byte}-sized integer.
   *
   * @see Decoder#ofInt8()
   */
  record Int8(byte value) implements Value {}

  /**
   * A {@code short}-sized integer.
   *
   * @see Decoder#ofInt16()
   */
  record Int16(short value) implements Value {}

  /**
   * An {@code int}-sized integer.
   *
   * @see Decoder#ofInt32()
   */
  record Int32(int value) implements Value {}

  /**
   * A {@code long}-sized integer.
   *
   * @see Decoder#ofInt64()
   */
  record Int64(long value) implements Value {}

  /**
   * A double-precision floating point number.
   *
   * @see Decoder#ofFloat64()
   */
  record Float64(double value) implements Value {}

  /**
   * A character string.
   *
   * @see Decoder#ofStr
   */
  record Str(String value) implements Value {}
}
