// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static java.nio.charset.StandardCharsets.UTF_8;

import eu.mulk.jgvariant.core.Decoder;
import java.util.List;

/**
 * Metadata describing files and directories of a file tree.
 *
 * <p>Often comes in a pair with {@link DirMeta}.
 *
 * <p>Referenced by {@link Commit#rootDirTreeChecksum()} and recursively by {@link
 * Directory#treeChecksum()}.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_TREE_GVARIANT_STRING}
 *
 * @param files a list of files in the directory.
 * @param directories a list of subdirectories of the directory.
 * @see DirMeta
 * @see ObjectType#DIR_TREE
 */
public record DirTree(List<File> files, List<Directory> directories) {

  /**
   * A file in a file tree.
   *
   * @param name the file name.
   * @param checksum the checksum of the {@link ObjectType#FILE} object.
   */
  public record File(String name, Checksum checksum) {

    private static final Decoder<File> DECODER =
        Decoder.ofStructure(File.class, Decoder.ofString(UTF_8), Checksum.decoder());

    /**
     * Acquires a {@link Decoder} for the enclosing type.
     *
     * @return a possibly shared {@link Decoder}.
     */
    public static Decoder<File> decoder() {
      return DECODER;
    }
  }

  /**
   * A subdirectory in a file tree.
   *
   * @param name the name of the subdirectory.
   * @param treeChecksum the checksum of the {@link DirTree} object.
   * @param dirChecksum the checksum of the {@link DirMeta} object.
   */
  public record Directory(String name, Checksum treeChecksum, Checksum dirChecksum) {

    private static final Decoder<Directory> DECODER =
        Decoder.ofStructure(
            Directory.class, Decoder.ofString(UTF_8), Checksum.decoder(), Checksum.decoder());

    /**
     * Acquires a {@link Decoder} for the enclosing type.
     *
     * @return a possibly shared {@link Decoder}.
     */
    public static Decoder<Directory> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<DirTree> DECODER =
      Decoder.ofStructure(
          DirTree.class, Decoder.ofArray(File.decoder()), Decoder.ofArray(Directory.decoder()));

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<DirTree> decoder() {
    return DECODER;
  }
}
