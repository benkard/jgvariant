package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Reference: {@code ostree-repo-static-delta-private.h#OSTREE_STATIC_DELTA_PART_PAYLOAD_FORMAT_V0}
 */
public record DeltaPartPayload(
    List<FileMode> fileModes,
    List<List<Xattr>> xattrs,
    ByteString rawDataSource,
    ByteString operations) {

  private static final Decoder<DeltaPartPayload> DECODER =
      Decoder.ofStructure(
          DeltaPartPayload.class,
          Decoder.ofArray(FileMode.decoder()),
          Decoder.ofArray(Decoder.ofArray(Xattr.decoder())),
          ByteString.decoder(),
          ByteString.decoder());

  public record FileMode(int uid, int gid, int mode) {

    private static final Decoder<FileMode> DECODER =
        Decoder.ofStructure(
            FileMode.class,
            Decoder.ofInt().withByteOrder(ByteOrder.LITTLE_ENDIAN),
            Decoder.ofInt().withByteOrder(ByteOrder.LITTLE_ENDIAN),
            Decoder.ofInt().withByteOrder(ByteOrder.LITTLE_ENDIAN));

    public static Decoder<FileMode> decoder() {
      return DECODER;
    }
  }

  public static Decoder<DeltaPartPayload> decoder() {
    return DECODER;
  }
}
