package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The summary file of an OSTree repository.
 *
 * <p>Stored as a file named {@code summary} in the OSTree repository root.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_SUMMARY_GVARIANT_STRING}
 */
public record Summary(List<Entry> entries, Metadata metadata) {

  public record Entry(String ref, Value value) {

    public record Value(long size, Checksum checksum, Metadata metadata) {

      private static final Decoder<Value> DECODER =
          Decoder.ofStructure(
              Value.class,
              Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN),
              Checksum.decoder(),
              Metadata.decoder());

      public static Decoder<Value> decoder() {
        return DECODER;
      }
    }

    private static final Decoder<Entry> DECODER =
        Decoder.ofStructure(Entry.class, Decoder.ofString(StandardCharsets.UTF_8), Value.decoder());

    public static Decoder<Entry> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<Summary> DECODER =
      Decoder.ofStructure(Summary.class, Decoder.ofArray(Entry.decoder()), Metadata.decoder());

  public static Decoder<Summary> decoder() {
    return DECODER;
  }
}
