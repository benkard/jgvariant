// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteBuffer;

/**
 * A wrapper for {@link ByteString} that refers to a content-addressed object in an OSTree
 * repository.
 *
 * @param byteString the bytes that make up this {@link Checksum}.
 */
public record Checksum(ByteString byteString) {

  private static final int SIZE = 32;

  private static final Decoder<Checksum> DECODER =
      ByteString.decoder().map(Checksum::new, Checksum::optimizedByteString);

  private static final ByteString NULL_BYTESTRING = new ByteString(new byte[0]);

  private static final Checksum ZERO = new Checksum(new ByteString(new byte[SIZE]));

  public Checksum {
    if (byteString.size() == 0) {
      byteString = zero().byteString;
    }

    if (byteString.size() != SIZE) {
      throw new IllegalArgumentException(
          "attempted to construct Checksum of length %d (expected: %d)"
              .formatted(byteString.size(), SIZE));
    }
  }

  /**
   * A decoder for a {@code byte[]} that wraps the result in a {@link Checksum}.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<Checksum> decoder() {
    return DECODER;
  }

  /**
   * Returns an empty checksum.
   *
   * @return a checksum whose bits are all zero.
   */
  public static Checksum zero() {
    return ZERO;
  }

  public ByteString optimizedByteString() {
    return isEmpty() ? NULL_BYTESTRING : byteString;
  }

  /**
   * Checks whether the checksum contains only zero bits.
   *
   * @return {@code true} if the byte string is equal to {@link #zero()}, {@code false} otherwise.
   */
  public boolean isEmpty() {
    return equals(ZERO);
  }

  /**
   * Converts the contained byte array into a hex string.
   *
   * <p>Useful for printing.
   *
   * @return a hex string representation of the bytes making up this checksum.
   */
  public String hex() {
    return byteString.hex();
  }

  /**
   * Parses a hex string into a {@link Checksum}.
   *
   * @param hex a hex string.
   * @return a {@link Checksum} corresponding to the given hex string.
   */
  public static Checksum ofHex(String hex) {
    return new Checksum(ByteString.ofHex(hex));
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
    return byteString.modifiedBase64();
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
  public static Checksum ofModifiedBase64(String mbase64) {
    return new Checksum(ByteString.ofModifiedBase64(mbase64));
  }

  /**
   * Reads a Checksum for a {@link ByteBuffer}.
   *
   * @param byteBuffer the byte buffer to read from.
   * @return a checksum.
   */
  public static Checksum readFrom(ByteBuffer byteBuffer) {
    var bytes = new byte[SIZE];
    byteBuffer.get(bytes);
    return new Checksum(new ByteString(bytes));
  }
}
