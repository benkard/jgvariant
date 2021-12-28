package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A {@link DeltaSuperblock} signed with some sort of key.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_SIGNED_FORMAT}
 */
public record SignedDelta(
    long magicNumber, ByteString superblock, Map<String, Variant> signatures) {

  private static final Decoder<SignedDelta> DECODER =
      Decoder.ofStructure(
          SignedDelta.class,
          Decoder.ofLong().withByteOrder(ByteOrder.BIG_ENDIAN),
          ByteString.decoder(),
          Decoder.ofDictionary(Decoder.ofString(StandardCharsets.US_ASCII), Decoder.ofVariant()));

  public static Decoder<SignedDelta> decoder() {
    return DECODER;
  }
}
