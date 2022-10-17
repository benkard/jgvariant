// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static java.nio.charset.StandardCharsets.UTF_8;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Variant;
import java.util.Map;

/**
 * A collection of cryptographic signatures for a {@link Summary}.
 *
 * <p>Stored as a file named {@code summary.sig} in the OSTree repository root.
 *
 * <p>Reference: {@code ostree-repo-static-delta-private.h#OSTREE_SUMMARY_SIG_GVARIANT_STRING}
 *
 * @param signatures a list of signatures, indexed by type.
 */
public record SummarySignature(Map<String, Variant> signatures) {

  private static final Decoder<SummarySignature> DECODER =
      Decoder.ofDictionary(Decoder.ofString(UTF_8), Decoder.ofVariant()).map(SummarySignature::new);

  /**
   * Acquires a {@link Decoder} for the enclosing type.
   *
   * @return a possibly shared {@link Decoder}.
   */
  public static Decoder<SummarySignature> decoder() {
    return DECODER;
  }
}
