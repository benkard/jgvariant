package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A collection of cryptographic signatures for a {@link Summary}.
 *
 * <p>Stored as a file named {@code summary.sig} in the OSTree repository root.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_SUMMARY_SIG_GVARIANT_STRING}
 */
public record SummarySignature(List<Signature> signatures) {

  /** A cryptographic signature. */
  public record Signature(String key, Variant data) {

    private static final Decoder<Signature> DECODER =
        Decoder.ofStructure(
            Signature.class, Decoder.ofString(StandardCharsets.UTF_8), Decoder.ofVariant());

    public static Decoder<Signature> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<SummarySignature> DECODER =
      Decoder.ofArray(Signature.decoder()).map(SummarySignature::new);

  public static Decoder<SummarySignature> decoder() {
    return DECODER;
  }
}
