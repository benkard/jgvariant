// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static java.nio.charset.StandardCharsets.UTF_8;

import eu.mulk.jgvariant.core.Decoder;
import java.nio.ByteOrder;
import java.util.List;

/**
 * A commit in an OSTree repository.
 *
 * <p>Has an optional parent, a root directory, and various metadata.
 *
 * <p>Reference: {@code ostree-core.h#OSTREE_COMMIT_GVARIANT_STRING}
 *
 * @param metadata arbitrary metadata supplied by the user who made the commit.
 * @param parentChecksum a (possibly {@link Checksum#isEmpty()}) reference to this commit's parent
 *     commit.
 * @param relatedObjects references to related commits.
 * @param subject the subject line part of the commit message.
 * @param body the body part of the commit message.
 * @param timestamp UNIX epoch seconds of when the commit was done.
 * @param rootDirTreeChecksum the checksum of the {@link DirTree} file describing the root
 *     directory.
 * @param rootDirMetaChecksum the checksum of the {@link DirMeta} file describing the root
 *     directory.
 * @see ObjectType#COMMIT
 */
public record Commit(
    Metadata metadata,
    Checksum parentChecksum,
    List<RelatedObject> relatedObjects,
    String subject,
    String body,
    long timestamp,
    Checksum rootDirTreeChecksum,
    Checksum rootDirMetaChecksum) {

  /**
   * A reference to a related commit.
   *
   * @param ref the name of the reference.
   * @param commitChecksum the checksum of the related commit.
   */
  public record RelatedObject(String ref, Checksum commitChecksum) {

    private static final Decoder<RelatedObject> DECODER =
        Decoder.ofStructure(RelatedObject.class, Decoder.ofString(UTF_8), Checksum.decoder());

    public static Decoder<RelatedObject> decoder() {
      return DECODER;
    }
  }

  private static final Decoder<Commit> DECODER =
      Decoder.ofStructure(
          Commit.class,
          Metadata.decoder(),
          Checksum.decoder(),
          Decoder.ofArray(RelatedObject.decoder()),
          Decoder.ofString(UTF_8),
          Decoder.ofString(UTF_8),
          Decoder.ofLong().withByteOrder(ByteOrder.BIG_ENDIAN),
          Checksum.decoder(),
          Checksum.decoder());

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<Commit> decoder() {
    return DECODER;
  }
}
