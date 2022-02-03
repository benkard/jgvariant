// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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
          ByteString.decoder().map(SignedDelta::decodeSuperblock),
          Decoder.ofDictionary(Decoder.ofString(StandardCharsets.US_ASCII), Decoder.ofVariant()));

  private static DeltaSuperblock decodeSuperblock(ByteString byteString) {
    return DeltaSuperblock.decoder().decode(ByteBuffer.wrap(byteString.bytes()));
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
