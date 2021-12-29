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

  private static final Decoder<Checksum> DECODER = ByteString.decoder().map(Checksum::new);

  public Checksum {
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
    return new Checksum(new ByteString(new byte[SIZE]));
  }

  /**
   * Checks whether the checksum contains only zero bits.
   *
   * @return {@code true} if the byte string is equal to {@link #zero()}, {@code false} otherwise.
   */
  public boolean isEmpty() {
    return equals(zero());
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
