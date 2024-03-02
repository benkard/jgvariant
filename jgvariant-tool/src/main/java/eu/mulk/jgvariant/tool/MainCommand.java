// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool;

import static java.util.logging.Level.*;

import eu.mulk.jgvariant.core.Decoder;
import eu.mulk.jgvariant.core.Signature;
import eu.mulk.jgvariant.core.Variant;
import eu.mulk.jgvariant.ostree.ByteString;
import eu.mulk.jgvariant.ostree.Metadata;
import eu.mulk.jgvariant.ostree.Summary;
import eu.mulk.jgvariant.tool.jsonb.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;
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
      void read(@Parameters(paramLabel = "<file>", description = "Summary file to read") File file)
          throws IOException {
        read(file, Summary.decoder());
      }

      @Command(name = "add-static-delta", mixinStandardHelpOptions = true)
      void addStaticDelta(
          @Parameters(paramLabel = "<file>", description = "Summary file to manipulate.")
              File summaryFile,
          @Parameters(paramLabel = "<delta>", description = "Checksum of the static delta (hex).")
              String delta,
          @Parameters(paramLabel = "<to>", description = "Commit checksum the delta ends at (hex).")
              String toCommit,
          @Parameters(
                  paramLabel = "<from>",
                  arity = "0..1",
                  description = "Commit checksum the delta starts from (hex).")
              String fromCommit)
          throws IOException, ParseException {
        var summaryDecoder = Summary.decoder();

        var summary = decodeFile(summaryFile, summaryDecoder);

        var staticDeltaMapSignature = Signature.parse("a{sv}");
        var checksumSignature = Signature.parse("ay");

        var metadata = summary.metadata();
        var metadataFields = new LinkedHashMap<>(metadata.fields());
        metadataFields.compute(
            "ostree.static-deltas",
            (k, v) -> {
              Map<String, Variant> staticDeltas =
                  v != null
                      ? new LinkedHashMap<>((Map<String, Variant>) v.value())
                      : new LinkedHashMap<>();
              staticDeltas.put(
                  fromCommit != null ? fromCommit + "-" + toCommit : toCommit,
                  new Variant(checksumSignature, toByteList(ByteString.ofHex(delta).bytes())));
              return new Variant(staticDeltaMapSignature, staticDeltas);
            });
        metadata = new Metadata(metadataFields);
        summary = new Summary(summary.entries(), metadata);

        encodeFile(summaryFile, summaryDecoder, summary);
      }

      private List<Byte> toByteList(byte[] bytes) {
        return IntStream.range(0, bytes.length).mapToObj(i -> bytes[i]).toList();
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
      var thing = decodeFile(file, decoder);
      out().println(jsonb.toJson(thing));
    }

    protected final T decodeFile(File file, Decoder<T> decoder) throws IOException {
      LOG.fine(() -> "Reading file %s".formatted(file));
      var fileBytes = ByteBuffer.wrap(Files.readAllBytes(fs().getPath(file.getPath())));
      return decoder.decode(fileBytes);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected final void encodeFile(File file, Decoder<T> decoder, T thing) throws IOException {
      var thingBytes = decoder.encode(thing);

      LOG.fine(() -> "Writing file %s".formatted(file));
      try (var out = FileChannel.open(fs().getPath(file.getPath()), StandardOpenOption.WRITE)) {
        out.write(thingBytes);
      }
    }
  }

  MainCommand() {}
}
