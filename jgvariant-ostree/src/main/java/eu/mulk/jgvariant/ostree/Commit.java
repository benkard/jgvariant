package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A commit in an OSTree repository.
 *
 * <p>Has an optional parent, a root directory, and various metadata.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_COMMIT_GVARIANT_STRING}
 */
public record Commit(
    Metadata metadata,
    Checksum parentChecksum,
    List<RelatedObject> relatedObjects,
    String subject,
    String body,
    long timestamp,
    Checksum rootDirTreeChecksum,
    Checksum rootDirMetaChecksum) {

  public record RelatedObject(String ref, Checksum commitChecksum) {

    private static final Decoder<RelatedObject> DECODER =
        Decoder.ofStructure(
            RelatedObject.class, Decoder.ofString(StandardCharsets.UTF_8), Checksum.decoder());

    public static Decoder<RelatedObject> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<Commit> DECODER =
      Decoder.ofStructure(
          Commit.class,
          Metadata.decoder(),
          Checksum.decoder(),
          Decoder.ofArray(RelatedObject.decoder()),
          Decoder.ofString(StandardCharsets.UTF_8),
          Decoder.ofString(StandardCharsets.UTF_8),
          Decoder.ofLong().withByteOrder(ByteOrder.BIG_ENDIAN),
          Checksum.decoder(),
          Checksum.decoder());

  public static Decoder<Commit> decoder() {
    return DECODER;
  }
}
