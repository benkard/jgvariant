package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Permission bits and extended attributes for a directory.
 *
 * <p>Often comes in a pair with {@link DirTree}.
 *
 * <p>Referenced by {@link Commit#rootDirMetaChecksum()} and {@link
 * DirTree.Directory#dirChecksum()}.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_DIRMETA_GVARIANT_STRING}
 *
 * @param uid the user ID that owns the directory.
 * @param gid the group ID that owns the directory.
 * @param mode the POSIX permission bits.
 * @param xattrs POSIX extended attributes.
 * @see DirTree
 * @see ObjectType#DIR_META
 */
public record DirMeta(int uid, int gid, int mode, List<Xattr> xattrs) {

  private static final Decoder<DirMeta> DECODER =
      Decoder.ofStructure(
          DirMeta.class,
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofArray(Xattr.decoder()));

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<DirMeta> decoder() {
    return DECODER;
  }
}
