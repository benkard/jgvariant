// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool;

import static java.util.logging.Level.*;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.ostree.Summary;
import eu.mulk.jgvariant.tool.jsonb.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.logging.Logger;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(
    name = "jgvariant",
    mixinStandardHelpOptions = true,
    description = "Manipulate files in GVariant format.",
    subcommands = {MainCommand.OstreeCommand.class, AutoComplete.GenerateCompletion.class})
final class MainCommand {

  private static final Logger LOG = Logger.getLogger("eu.mulk.jgvariant.tool");

  private static final Jsonb jsonb =
      JsonbBuilder.newBuilder()
          .withConfig(
              new JsonbConfig()
                  .withFormatting(true)
                  .withAdapters(ChecksumAdapter.INSTANCE)
                  .withSerializers(
                      ByteStringSerializer.INSTANCE,
                      ByteArraySerializer.INSTANCE,
                      SignatureSerializer.INSTANCE,
                      VariantSerializer.INSTANCE))
          .build();

  @Option(
      names = {"-v", "--verbose"},
      description = "Enable verbose logging.",
      scope = CommandLine.ScopeType.INHERIT)
  void setVerbose(boolean[] verbose) {
    Logger.getGlobal()
        .setLevel(
            switch (verbose.length) {
              case 0 -> WARNING;
              case 1 -> INFO;
              case 2 -> FINE;
              default -> ALL;
            });
  }

  @Command(
      name = "ostree",
      mixinStandardHelpOptions = true,
      description = "Manipulate OSTree files.",
      subcommands = {OstreeCommand.SummaryCommand.class})
  static final class OstreeCommand {

    @Command(
        name = "summary",
        mixinStandardHelpOptions = true,
        description = "Manipulate OSTree summary files.")
    static final class SummaryCommand extends BaseDecoderCommand<Summary> {

      @Command(mixinStandardHelpOptions = true)
      void read(@Parameters(paramLabel = "<file>") File file) throws IOException {
        read(file, Summary.decoder());
      }

      SummaryCommand() {}
    }

    OstreeCommand() {}
  }

  @Command
  abstract static class BaseCommand {

    @Spec CommandLine.Model.CommandSpec spec;

    @VisibleForTesting FileSystem fs = FileSystems.getDefault();

    protected BaseCommand() {}

    protected PrintWriter out() {
      return spec.commandLine().getOut();
    }

    protected PrintWriter err() {
      return spec.commandLine().getErr();
    }

    protected FileSystem fs() {
      return fs;
    }
  }

  abstract static class BaseDecoderCommand<T> extends BaseCommand {

    protected final void read(File file, Decoder<T> decoder) throws IOException {
      LOG.fine(() -> "Reading file %s".formatted(file));
      var fileBytes = ByteBuffer.wrap(Files.readAllBytes(fs().getPath(file.getPath())));
      var thing = decoder.decode(fileBytes);
      out().println(jsonb.toJson(thing));
    }
  }

  MainCommand() {}
}
