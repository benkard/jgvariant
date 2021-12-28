package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A collection of cryptographic signatures for a {@link Summary}.
 *
 * <p>Stored as a file named {@code summary.sig} in the OSTree repository root.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_SUMMARY_SIG_GVARIANT_STRING}
 */
public record SummarySignature(Map<String, Variant> signatures) {

  private static final Decoder<SummarySignature> DECODER =
      Decoder.ofDictionary(Decoder.ofString(StandardCharsets.UTF_8), Decoder.ofVariant())
          .map(SummarySignature::new);

  public static Decoder<SummarySignature> decoder() {
    return DECODER;
  }
}
