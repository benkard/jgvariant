package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;

/**
 * A fallback entry in a {@link DeltaSuperblock}.
 *
 * <p>References a file in the OSTree repository.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_FALLBACK_FORMAT}
 *
 * @param objectType the object type of the file represented by this fallback entry.
 * @param checksum the checksum of the file represented by this fallback entry.
 * @param compressedSize the compressed size of the file represented by this fallback entry.
 * @param uncompressedSize the uncompressed size of the file represented by this fallback entry.
 */
public record DeltaFallback(
    ObjectType objectType, Checksum checksum, long compressedSize, long uncompressedSize) {

  private static final Decoder<DeltaFallback> DECODER =
      Decoder.ofStructure(
          DeltaFallback.class,
          Decoder.ofByte().map(ObjectType::valueOf),
          Checksum.decoder(),
          Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN), // FIXME: non-canonical
          Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN)); // FIXME: non-canonical

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<DeltaFallback> decoder() {
    return DECODER;
  }
}
