package eu.mulk.jgvariant.core;

import java.util.List;
import java.util.Optional;

/**
 * A value representable by the <a href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a>
 * serialization format, tagged with its type.
 *
 * <p>{@link Variant} is a sum type (sealed interface) that represents a GVariant value. Its
 * subtypes represent the different types of values that GVariant supports.
 *
 * @see Decoder#ofVariant()
 */
public sealed interface Variant {

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
  record Array<T>(List<T> values) implements Variant {}

  /**
   * A value that is either present or absent.
   *
   * @param <T> the contained type.
   * @see Decoder#ofMaybe
   */
  record Maybe<T>(Optional<T> value) implements Variant {}

  /**
   * A tuple of values of fixed types.
   *
   * <p>GVariant structures are represented as {@link Record} types. For example, a two-element
   * structure consisting of a string and an int can be modelled as follows:
   *
   * <pre>{@code
   * record TestRecord(String s, int i) {}
   * var testStruct = new Structure<>(new TestRecord("hello", 123);
   * }</pre>
   *
   * @param <T> the {@link Record} type that represents the components of the structure.
   * @see Decoder#ofStructure
   */
  record Structure<T extends Record>(T values) implements Variant {}

  /**
   * Either true or false.
   *
   * @see Decoder#ofBoolean()
   */
  record Bool(boolean value) implements Variant {}

  /**
   * A {@code byte}-sized integer.
   *
   * @see Decoder#ofByte()
   */
  record Byte(byte value) implements Variant {}

  /**
   * A {@code short}-sized integer.
   *
   * @see Decoder#ofShort()
   */
  record Short(short value) implements Variant {}

  /**
   * An {@code int}-sized integer.
   *
   * @see Decoder#ofInt()
   */
  record Int(int value) implements Variant {}

  /**
   * A {@code long}-sized integer.
   *
   * @see Decoder#ofLong()
   */
  record Long(long value) implements Variant {}

  /**
   * A double-precision floating point number.
   *
   * @see Decoder#ofDouble()
   */
  record Double(double value) implements Variant {}

  /**
   * A character string.
   *
   * @see Decoder#ofString
   */
  record String(java.lang.String value) implements Variant {}
}
