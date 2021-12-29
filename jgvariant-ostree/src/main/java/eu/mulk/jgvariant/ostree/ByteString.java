package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.util.Arrays;
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
