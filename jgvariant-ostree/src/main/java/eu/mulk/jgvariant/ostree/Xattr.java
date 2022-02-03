// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;

/**
 * A POSIX extended attribute of a file or directory.
 *
 * <p>Reference: (embedded in other data types, e.g. {@code
 * ostree-core.h#OSTREE_DIRMETA_GVARIANT_STRING}, {@code
 * ostree-core.h#OSTREE_FILEMETA_GVARIANT_STRING})
 *
 * @param name the name part of the extended attribute.
 * @param value the value part of the extended attribute.
 * @see DirMeta
 * @see FileMeta
 */
public record Xattr(ByteString name, ByteString value) {

  private static final Decoder<Xattr> DECODER =
      Decoder.ofStructure(Xattr.class, ByteString.decoder(), ByteString.decoder());

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<Xattr> decoder() {
    return DECODER;
  }
}
