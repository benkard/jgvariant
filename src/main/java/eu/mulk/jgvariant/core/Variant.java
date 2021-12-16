package eu.mulk.jgvariant.core;

/**
 * A dynamically typed GVariant value carrying a {@link Signature} describing its type.
 *
 * <p>{@link #value()} can be of one of the following types:
 *
 * <ul>
 *   <li>{@link Boolean}
 *   <li>{@link Byte}
 *   <li>{@link Short}
 *   <li>{@link Integer}
 *   <li>{@link Long}
 *   <li>{@link String}
 *   <li>{@link java.util.Optional} (a GVariant {@code Maybe} type)
 *   <li>{@link java.util.List} (a GVariant array)
 *   <li>{@link Object[]} (a GVariant structure)
 * </ul>
 *
 * @param signature the signature describing the type of the value.
 * @param value the value itself; one of {@link Boolean}, {@link Byte}, {@link Short}, {@link
 *     Integer}, {@link Long}, {@link String}, {@link java.util.Optional}, {@link java.util.List},
 *     {@link Object[]}.
 */
public record Variant(Signature signature, Object value) {}
