package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * A wrapper for a {@code byte[]} that implements {@link #equals(Object)}, {@link #hashCode()}, and
 * {@link #toString()} according to value semantics.
 */
public record ByteString(byte[] bytes) {

  private static final Decoder<ByteString> DECODER = Decoder.ofByteArray().map(ByteString::new);

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

  public String hex() {
    return HexFormat.of().formatHex(bytes);
  }

  public static ByteString ofHex(String hex) {
    return new ByteString(HexFormat.of().parseHex(hex));
  }
}
