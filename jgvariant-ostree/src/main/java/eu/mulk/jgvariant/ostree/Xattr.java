package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;

/**
 * Reference: (embedded in other data types, e.g. {@code
 * ostree-core.h#OSTREE_DIRMETA_GVARIANT_STRING}, {@code
 * ostree-core.h#OSTREE_FILEMETA_GVARIANT_STRING})
 */
public record Xattr(ByteString name, ByteString value) {

  private static final Decoder<Xattr> DECODER =
      Decoder.ofStructure(Xattr.class, ByteString.decoder(), ByteString.decoder());

  public static Decoder<Xattr> decoder() {
    return DECODER;
  }
}
