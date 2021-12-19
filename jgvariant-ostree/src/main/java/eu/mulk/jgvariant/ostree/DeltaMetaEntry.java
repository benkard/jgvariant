package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * An entry in a {@link DeltaSuperblock}.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_META_ENTRY_FORMAT}
 */
public record DeltaMetaEntry(
    int version, Checksum checksum, long size, long usize, List<DeltaObject> objects) {

  record DeltaObject(byte objectType, Checksum checksum) {

    private static final Decoder<DeltaObject> DECODER =
        Decoder.ofStructure(DeltaObject.class, Decoder.ofByte(), Checksum.decoder());

    public static Decoder<DeltaObject> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<DeltaMetaEntry> DECODER =
      Decoder.ofStructure(
          DeltaMetaEntry.class,
          Decoder.ofInt().withByteOrder(ByteOrder.LITTLE_ENDIAN), // FIXME: non-canonical
          Checksum.decoder(),
          Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN), // FIXME: non-canonical
          Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN), // FIXME: non-canonical
          Decoder.ofByteArray().map(x -> List.of()) // FIXME
          );

  public static Decoder<DeltaMetaEntry> decoder() {
    return DECODER;
  }
}
