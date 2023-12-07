// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

module eu.mulk.jgvariant.tool {
  requires transitive eu.mulk.jgvariant.ostree;
  requires info.picocli;
  requires jakarta.json;
  requires jakarta.json.bind;
  requires java.logging;
  requires static com.google.errorprone.annotations;
  requires static org.apiguardian.api;
  requires static org.jetbrains.annotations;

  opens eu.mulk.jgvariant.tool to
      info.picocli;

  exports eu.mulk.jgvariant.tool;
}
