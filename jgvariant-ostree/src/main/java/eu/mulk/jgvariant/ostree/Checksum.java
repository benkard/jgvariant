package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;

/**
 * A wrapper for {@link ByteString} that refers to a content-addressed object in an OSTree
 * repository.
 */
public record Checksum(ByteString bytes) {

  private static final Decoder<Checksum> DECODER = ByteString.decoder().map(Checksum::new);

  public static Decoder<Checksum> decoder() {
    return DECODER;
  }

  public String hex() {
    return bytes.hex();
  }

  public static Checksum ofHex(String hex) {
    return new Checksum(ByteString.ofHex(hex));
  }
}
