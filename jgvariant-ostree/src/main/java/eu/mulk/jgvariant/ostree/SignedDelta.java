// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static java.nio.charset.StandardCharsets.US_ASCII;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * A {@link DeltaSuperblock} signed with some sort of key.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_SIGNED_FORMAT}
 *
 * @param magicNumber the value {@link #MAGIC}.
 * @param superblock the {@link DeltaSuperblock}.
 * @param signatures a list of signatures, indexed by type.
 */
public record SignedDelta(
    long magicNumber, DeltaSuperblock superblock, Map<String, Variant> signatures) {

  /** The value of {@link #magicNumber()}. */
  public static final long MAGIC = 0x4F53_5453_474E_4454L;

  private static final Decoder<SignedDelta> DECODER =
      Decoder.ofStructure(
          SignedDelta.class,
          Decoder.ofLong().withByteOrder(ByteOrder.BIG_ENDIAN),
          Decoder.ofByteArray().map(SignedDelta::decodeSuperblock, SignedDelta::encodeSuperblock),
          Decoder.ofDictionary(Decoder.ofString(US_ASCII), Decoder.ofVariant()));

  private static DeltaSuperblock decodeSuperblock(byte[] bytes) {
    return DeltaSuperblock.decoder().decode(ByteBuffer.wrap(bytes));
  }

  private static byte[] encodeSuperblock(DeltaSuperblock deltaSuperblock) {
    var byteBuffer = DeltaSuperblock.decoder().encode(deltaSuperblock);
    byte[] bytes = new byte[byteBuffer.remaining()];
    byteBuffer.get(bytes);
    return bytes;
  }

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<SignedDelta> decoder() {
    return DECODER;
  }
}
