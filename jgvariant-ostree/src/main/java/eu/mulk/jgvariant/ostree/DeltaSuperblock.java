package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/** Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_SUPERBLOCK_FORMAT} */
public record DeltaSuperblock(
    Metadata metadata,
    long timestamp,
    Checksum fromChecksum,
    Checksum toChecksum,
    Commit commit,
    List<DeltaName> dependencies,
    List<DeltaMetaEntry> entries,
    List<DeltaFallback> fallbacks) {

  public record DeltaName(Checksum fromChecksum, Checksum toChecksum) {

    private static final Decoder<DeltaName> DECODER =
        Decoder.ofStructure(DeltaName.class, Checksum.decoder(), Checksum.decoder());

    public static Decoder<DeltaName> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<DeltaSuperblock> DECODER =
      Decoder.ofStructure(
          DeltaSuperblock.class,
          Metadata.decoder(),
          Decoder.ofLong().withByteOrder(ByteOrder.BIG_ENDIAN),
          Checksum.decoder(),
          Checksum.decoder(),
          Commit.decoder(),
          Decoder.ofByteArray().map(x -> List.of()), // FIXME
          Decoder.ofArray(DeltaMetaEntry.decoder()),
          Decoder.ofArray(DeltaFallback.decoder()));

  public static Decoder<DeltaSuperblock> decoder() {
    return DECODER;
  }
}
