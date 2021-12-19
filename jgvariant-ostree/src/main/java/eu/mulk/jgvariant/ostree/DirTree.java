package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Metadata describing files and directories of a file tree.
 *
 * <p>Referenced by {@link Commit#rootDirTreeChecksum()} and recursively by {@link
 * Directory#treeChecksum()}.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_TREE_GVARIANT_STRING}
 */
public record DirTree(List<File> files, List<Directory> directories) {

  public record File(String name, Checksum checksum) {

    private static final Decoder<File> DECODER =
        Decoder.ofStructure(
            File.class, Decoder.ofString(StandardCharsets.UTF_8), Checksum.decoder());

    public static Decoder<File> decoder() {
      return DECODER;
    }
  }

  public record Directory(String name, Checksum treeChecksum, Checksum dirChecksum) {

    private static final Decoder<Directory> DECODER =
        Decoder.ofStructure(
            Directory.class,
            Decoder.ofString(StandardCharsets.UTF_8),
            Checksum.decoder(),
            Checksum.decoder());

    public static Decoder<Directory> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<DirTree> DECODER =
      Decoder.ofStructure(
          DirTree.class, Decoder.ofArray(File.decoder()), Decoder.ofArray(Directory.decoder()));

  public static Decoder<DirTree> decoder() {
    return DECODER;
  }
}
