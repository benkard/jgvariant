package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A wrapper for a list of metadata fields.
 *
 * <p>Reference: (embedded in other data types)
 */
public record Metadata(List<Field> fields) {

  /** A metadata field with a key and a value. */
  public record Field(String key, Variant value) {

    private static final Decoder<Field> DECODER =
        Decoder.ofStructure(
            Field.class, Decoder.ofString(StandardCharsets.UTF_8), Decoder.ofVariant());

    public static Decoder<Field> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<Metadata> DECODER =
      Decoder.ofArray(Field.decoder()).map(Metadata::new);

  public static Decoder<Metadata> decoder() {
    return DECODER;
  }
}
