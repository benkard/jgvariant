package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Permission bits and extended attributes for a directory.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_DIRMETA_GVARIANT_STRING}
 */
public record DirMeta(int uid, int gid, int mode, List<Xattr> xattrs) {

  private static final Decoder<DirMeta> DECODER =
      Decoder.ofStructure(
          DirMeta.class,
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofArray(Xattr.decoder()));

  public static Decoder<DirMeta> decoder() {
    return DECODER;
  }
}
