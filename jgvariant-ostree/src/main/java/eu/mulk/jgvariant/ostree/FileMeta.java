package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Permission bits and extended attributes for a file.
 *
 * <p>Stored in a POSIX extended attribute on the corresponding {@link ObjectType#FILE} object in
 * repositories in “bare-user” format.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_FILEMETA_GVARIANT_STRING}
 *
 * @param uid the user ID that owns the file.
 * @param gid the group ID that owns the file.
 * @param mode the POSIX permission bits.
 * @param xattrs POSIX extended attributes.
 */
public record FileMeta(int uid, int gid, int mode, List<Xattr> xattrs) {

  private static final Decoder<FileMeta> DECODER =
      Decoder.ofStructure(
          FileMeta.class,
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofInt().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofArray(Xattr.decoder()));

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<FileMeta> decoder() {
    return DECODER;
  }
}
