package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A {@link DeltaSuperblock} signed with some sort of key.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_SIGNED_FORMAT}
 */
public record SignedDelta(
    long magicNumber, ByteString superblock, List<SignedDelta.Signature> signatures) {

  /** A cryptographic signature. */
  public record Signature(String key, Variant data) {
    private static final Decoder<Signature> DECODER =
        Decoder.ofStructure(
            Signature.class, Decoder.ofString(StandardCharsets.US_ASCII), Decoder.ofVariant());

    public static Decoder<Signature> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<SignedDelta> DECODER =
      Decoder.ofStructure(
          SignedDelta.class,
          Decoder.ofLong().withByteOrder(ByteOrder.BIG_ENDIAN),
          ByteString.decoder(),
          Decoder.ofArray(Signature.decoder()));

  public static Decoder<SignedDelta> decoder() {
    return DECODER;
  }
}
