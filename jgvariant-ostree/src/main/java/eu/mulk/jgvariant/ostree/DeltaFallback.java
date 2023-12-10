// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

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
          Decoder.ofByte().map(ObjectType::valueOf, ObjectType::byteValue),
          Checksum.decoder(),
          Decoder.ofLong(),
          Decoder.ofLong());

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * <p><strong>Note:</strong> This decoder has an unspecified {@link ByteOrder}.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<DeltaFallback> decoder() {
    return DECODER;
  }

  DeltaFallback byteSwapped() {
    return new DeltaFallback(objectType, checksum, compressedSize, uncompressedSize);
  }
}
