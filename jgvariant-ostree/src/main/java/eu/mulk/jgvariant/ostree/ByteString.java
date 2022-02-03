// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A wrapper for a {@code byte[]} that implements {@link #equals(Object)}, {@link #hashCode()}, and
 * {@link #toString()} according to value semantics.
 *
 * @param bytes the byte array that this ByteString wraps.
 */
public record ByteString(byte[] bytes) {

  private static final Decoder<ByteString> DECODER = Decoder.ofByteArray().map(ByteString::new);

  /**
   * Returns a decoder for a {@code byte[]} that wraps the result in {@link ByteString}.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<ByteString> decoder() {
    return DECODER;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ByteString byteString) && Arrays.equals(bytes, byteString.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    return "ByteString{hex=\"%s\"}".formatted(hex());
  }

  /**
   * Converts the contained byte array into a hex string.
   *
   * <p>Useful for printing.
   *
   * @return a hex string representation of this byte string.
   */
  public String hex() {
    return HexFormat.of().formatHex(bytes);
  }

  /**
   * Parses a hex string into a {@link ByteString}.
   *
   * @param hex a hex string.
   * @return a {@link ByteString} corresponding to the given hex string.
   */
  public static ByteString ofHex(String hex) {
    return new ByteString(HexFormat.of().parseHex(hex));
  }

  /**
   * Converts the contained byte array into modified Base64 (with {@code "/"} replaced with {@code
   * "-"}).
   *
   * <p>Modified Base64 is Base64 with {@code "/"} replaced with {@code "_"}. It is used to address
   * static deltas in an OSTree repository.
   *
   * <p>Useful for printing.
   *
   * @return a modified Base64 representation of the bytes making up this checksum.
   */
  public String modifiedBase64() {
    return Base64.getEncoder().withoutPadding().encodeToString(bytes).replace('/', '_');
  }

  /**
   * Parses a modified Base64 string into a {@link Checksum}.
   *
   * <p>Modified Base64 is Base64 with {@code "/"} replaced with {@code "_"}. It is used to address
   * static deltas in an OSTree repository.
   *
   * @param mbase64 a hex string.
   * @return a {@link Checksum} corresponding to the given modified Base64 string.
   */
  public static ByteString ofModifiedBase64(String mbase64) {
    return new ByteString(Base64.getDecoder().decode(mbase64.replace('_', '/')));
  }

  /**
   * Returns the number of bytes in the byte string.
   *
   * @return the number of bytes in the byte string.
   */
  public int size() {
    return bytes.length;
  }

  /**
   * Returns a {@link Stream} of all the bytes in the byte string.
   *
   * @return a new {@link Stream}.
   */
  Stream<Byte> stream() {
    return IntStream.range(0, bytes.length).mapToObj(i -> bytes[i]);
  }
}
