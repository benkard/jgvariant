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
import org.jspecify.annotations.Nullable;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(
    name = "jgvariant",
    mixinStandardHelpOptions = true,
    header = "Manipulate files in GVariant format.",
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
      header = "Manipulate OSTree files.",
      subcommands = {OstreeCommand.SummaryCommand.class})
  static final class OstreeCommand {

    @Command(
        name = "summary",
        mixinStandardHelpOptions = true,
        header = "Manipulate OSTree summary files.")
    static final class SummaryCommand extends BaseDecoderCommand<Summary> {

      @Command(
          name = "read",
          mixinStandardHelpOptions = true,
          header = "Dump an OSTree summary file as human-readable JSON.")
      void read(@Parameters(paramLabel = "<file>", description = "Summary file to read") File file)
          throws IOException {
        read(file, Summary.decoder());
      }

      @Command(
          name = "add-static-delta",
          mixinStandardHelpOptions = true,
          header = "Add a static delta to an OSTree summary file.",
          description =
              """
              Checksums can be given in either hex (64 digits) or a variant of Base64 (43
              digits) where '/' is replaced by '_'.

              In the OSTree repository, static deltas are named based on the <from> and <to>
              checksums in modified Base64 when stored in the deltas/ directory.  The naming
              scheme is either <from[0..1]>/<from[2..42]>-<to> if <from> is an actual commit
              or <to[0..1]>/<to[2..42]> if <from> is the empty commit.

              <superblock-csum> is the SHA256 checksum of the file called 'superblock' that
              is part of the static delta and contains its metadata.
              """)
      void addStaticDelta(
          @Parameters(paramLabel = "<file>", description = "Summary file to manipulate.")
              File summaryFile,
          @Parameters(
                  paramLabel = "<superblock-csum>",
                  description = "Checksum of the static delta superblock (hex/mbase64).")
              String deltaName,
          @Parameters(
                  paramLabel = "<to>",
                  description = "Commit checksum the delta ends at (hex/mbase64).")
              String toCommitName,
          @Parameters(
                  paramLabel = "<from>",
                  arity = "0..1",
                  description =
                      "Commit checksum the delta starts from (hex/mbase64).  Defaults to the empty commit.")
              @Nullable
              String fromCommitName)
          throws IOException, ParseException {
        var summaryDecoder = Summary.decoder();

        var summary = decodeFile(summaryFile, summaryDecoder);

        var staticDeltaMapSignature = Signature.parse("a{sv}");
        var checksumSignature = Signature.parse("ay");

        var delta = parseChecksum(deltaName);
        var toCommit = parseChecksum(toCommitName);
        var fromCommit = fromCommitName != null ? parseChecksum(fromCommitName) : null;

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
                  fromCommit != null ? fromCommit.hex() + "-" + toCommit.hex() : toCommit.hex(),
                  new Variant(checksumSignature, toByteList(delta.bytes())));
              return new Variant(staticDeltaMapSignature, staticDeltas);
            });
        metadata = new Metadata(metadataFields);
        summary = new Summary(summary.entries(), metadata);

        encodeFile(summaryFile, summaryDecoder, summary);
      }

      SummaryCommand() {}
    }

    OstreeCommand() {}
  }

  @Command
  abstract static class BaseCommand {

    @Spec CommandLine.Model.CommandSpec spec;

    private final FileSystem fs = FileSystems.getDefault();

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

    protected static ByteString parseChecksum(String name) {
      var bytes =
          switch (name.length()) {
            case 64 -> ByteString.ofHex(name);
            case 43 -> ByteString.ofModifiedBase64(name);
            default ->
                throw new IllegalArgumentException(
                    "Checksums must be either 64 hex digits or 43 mbase64 digits.");
          };

      if (bytes.size() != 32) {
        throw new IllegalArgumentException("Checksums must be 32 bytes long.");
      }

      return bytes;
    }

    protected static List<Byte> toByteList(byte[] bytes) {
      return IntStream.range(0, bytes.length).mapToObj(i -> bytes[i]).toList();
    }
  }

  MainCommand() {}
}
