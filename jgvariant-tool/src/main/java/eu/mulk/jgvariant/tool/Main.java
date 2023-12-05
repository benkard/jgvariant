// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool;

import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;
import picocli.CommandLine;

/**
 * A command line tool to read and manipulate GVariant-formatted files.
 *
 * <p>Also provides ways to manipulate OSTree repositories.
 */
public final class Main {
  static {
    Logger.getGlobal().setLevel(WARNING);
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new MainCommand()).execute(args);
    System.exit(exitCode);
  }

  private Main() {}
}
