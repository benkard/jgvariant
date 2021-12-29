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
 *
 * @param entries an entry in the summary file.
 * @param metadata additional keys and values contained in the summary.
 */
public record Summary(List<Entry> entries, Metadata metadata) {

  /**
   * An entry in the summary file of an OSTree repository, describing a ref.
   *
   * @param ref a ref name.
   * @param value data describing the ref.
   */
  public record Entry(String ref, Value value) {

    /**
     * The value part of an entry in the summary file of an OSTree repository.
     *
     * <p>Describes the {@link Commit} currently named by the corresponding ref.
     *
     * @param size the size of the commit.
     * @param checksum the checksum of the commit.
     * @param metadata additional metadata describing the commit.
     */
    public record Value(long size, Checksum checksum, Metadata metadata) {

      private static final Decoder<Value> DECODER =
          Decoder.ofStructure(
              Value.class,
              Decoder.ofLong().withByteOrder(ByteOrder.LITTLE_ENDIAN),
              Checksum.decoder(),
              Metadata.decoder());

      /**
       * Acquires a {@link Decoder} for the enclosing type.
       *
       * @return a possibly shared {@link Decoder}.
       */
      public static Decoder<Value> decoder() {
        return DECODER;
      }
    }

    private static final Decoder<Entry> DECODER =
        Decoder.ofStructure(Entry.class, Decoder.ofString(StandardCharsets.UTF_8), Value.decoder());

    /**
     * Acquires a {@link Decoder} for the enclosing type.
     *
     * @return a possibly shared {@link Decoder}.
     */
    public static Decoder<Entry> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<Summary> DECODER =
      Decoder.ofStructure(Summary.class, Decoder.ofArray(Entry.decoder()), Metadata.decoder());

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<Summary> decoder() {
    return DECODER;
  }
}
