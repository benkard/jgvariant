package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A wrapper for a list of metadata fields.
 *
 * <p>Reference: (embedded in other data types)
 */
public record Metadata(Map<String, Variant> fields) {

  private static final Decoder<Metadata> DECODER =
      Decoder.ofDictionary(Decoder.ofString(StandardCharsets.UTF_8), Decoder.ofVariant())
          .map(Metadata::new);

  public static Decoder<Metadata> decoder() {
    return DECODER;
  }
}
