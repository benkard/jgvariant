// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static java.nio.charset.StandardCharsets.UTF_8;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.util.Map;

/**
 * A wrapper for a set of metadata fields.
 *
 * <p>Reference: (embedded in other data types)
 *
 * @param fields a set of metadata fields indexed by name.
 */
public record Metadata(Map<String, Variant> fields) {

  private static final Decoder<Metadata> DECODER =
      Decoder.ofDictionary(Decoder.ofString(UTF_8), Decoder.ofVariant()).map(Metadata::new);

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<Metadata> decoder() {
    return DECODER;
  }
}
