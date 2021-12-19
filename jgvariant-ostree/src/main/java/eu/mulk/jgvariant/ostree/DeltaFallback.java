package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;

/**
 * A fallback entry in a {@link DeltaSuperblock}.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_FALLBACK_FORMAT}
 */
public record DeltaFallback(
    byte objectType, Checksum checksum, long compressedSize, long uncompressedSize) {

  private static final Decoder<DeltaFallback> DECODER =
      Decoder.ofStructure(
          DeltaFallback.class,
          Decoder.ofByte(),
          Checksum.decoder(),
          Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN), // FIXME: non-canonical
          Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN)); // FIXME: non-canonical

  public static Decoder<DeltaFallback> decoder() {
    return DECODER;
  }
}
