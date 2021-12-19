package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Permission bits and extended attributes for a file.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_FILEMETA_GVARIANT_STRING}
 */
public record FileMeta(int uid, int gid, int mode, List<Xattr> xattrs) {

  private static final Decoder<FileMeta> DECODER =
      Decoder.ofStructure(
          FileMeta.class,
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofArray(Xattr.decoder()));

  public static Decoder<FileMeta> decoder() {
    return DECODER;
  }
}
