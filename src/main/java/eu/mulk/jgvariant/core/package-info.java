/**
 * Provides {@link eu.mulk.jgvariant.core.Variant} and {@link eu.mulk.jgvariant.core.Decoder}, the
 * foundational classes for <a href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a>
 * parsing.
 *
 * <p>{@link eu.mulk.jgvariant.core.Variant} is a sum type (sealed interface) that represents a
 * GVariant value. Its subtypes represent the different types of values that GVariant supports.
 *
 * <p>Instances of {@link eu.mulk.jgvariant.core.Decoder} read a given concrete subtype of {@link
 * eu.mulk.jgvariant.core.Variant} from a {@link java.nio.ByteBuffer}. The class also contains
 * factory methods to create those instances.
 *
 * <p><strong>Example</strong>
 *
 * <p>To parse a GVariant of type {@code "a(si)"}, which is an array of pairs of {@link
 * java.lang.String} and {@code int}, you can use the following code:
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
 */
package eu.mulk.jgvariant.core;