// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * Provides {@link eu.mulk.jgvariant.core.Decoder}, the foundational class for <a
 * href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a> parsing.
 *
 * <p>Instances of {@link eu.mulk.jgvariant.core.Decoder} read a given value type from a {@link
 * java.nio.ByteBuffer}. The class also contains factory methods to create those instances.
 *
 * <p><strong>Example</strong>
 *
 * <p>To parse a GVariant of type {@code "a(si)"}, which is an array of pairs of {@link
 * java.lang.String} and {@code int}, you can use the following code:
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
 */
package eu.mulk.jgvariant.core;
