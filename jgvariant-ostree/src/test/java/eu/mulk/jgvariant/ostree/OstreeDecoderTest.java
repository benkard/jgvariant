// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.adelean.inject.resources.junit.jupiter.GivenBinaryResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import eu.mulk.jgvariant.core.Signature;
import eu.mulk.jgvariant.core.Variant;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@TestWithResources
@SuppressWarnings({
  "ImmutableListOf1",
  "ImmutableMapOf1",
  "initialization.field.uninitialized",
  "NullAway"
})
class OstreeDecoderTest {

  @GivenBinaryResource("/ostree/summary")
  byte[] summaryBytes;

  @GivenBinaryResource(
      "/ostree/objects/3d/3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f.commit")
  byte[] commitBytes;

  @GivenBinaryResource(
      "/ostree/objects/14/c9b958ac59df4979095a3485b4da5a045fe8737ffdba8cfbfff24988b238f7.dirtree")
  byte[] dirTreeBytes;

  @GivenBinaryResource(
      "/ostree/objects/48/cc6a2ecdab284b9d1e5b0e875c905866ff32f65ee1e857df0e691285d6f14c.dirmeta")
  byte[] dirMetaBytes;

  @GivenBinaryResource("/ostree/deltas/Mc/iDXVydLGaHpQCRyFFC0bLYU_9Bap+4G07jB1RRDVI/superblock")
  byte[] deltaSuperblockBytes;

  @GivenBinaryResource("/ostree/deltas/Mc/iDXVydLGaHpQCRyFFC0bLYU_9Bap+4G07jB1RRDVI/0")
  byte[] deltaPartPayloadBytes;

  @Test
  void summaryDecoder() {
    var decoder = Summary.decoder();
    var summary = decoder.decode(ByteBuffer.wrap(summaryBytes));

    assertAll(
        () ->
            assertEquals(
                List.of(
                    new Summary.Entry(
                        "mulkos/1.x/amd64",
                        new Summary.Entry.Value(
                            214,
                            Checksum.ofHex(
                                "66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350"),
                            new Metadata(
                                Map.of(
                                    "ostree.commit.timestamp",
                                    new Variant(Signature.parse("t"), 1640537170L)))))),
                summary.entries()),
        () ->
            assertEquals(
                Map.of(
                    "ostree.summary.last-modified",
                    new Variant(Signature.parse("t"), 1640537300L),
                    "ostree.summary.tombstone-commits",
                    new Variant(Signature.parse("b"), false),
                    "ostree.static-deltas",
                    new Variant(
                        Signature.parse("a{sv}"),
                        Map.of(
                            "3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f-66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350",
                            new Variant(
                                Signature.parse("ay"),
                                bytesOfHex(
                                    "03738040e28e7662e9c9d2599c530ea974e642c9f87e6c00cbaa39a0cdac8d44")),
                            "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52",
                            new Variant(
                                Signature.parse("ay"),
                                bytesOfHex(
                                    "f481144629474bd88c106e45ac405ebd75b324b0655af1aec14b31786ae1fd61")),
                            "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52-3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f",
                            new Variant(
                                Signature.parse("ay"),
                                bytesOfHex(
                                    "2c6a07bc1cf4d7ce7d00f82d7d2d6d156fd0e31d476851b46dc2306b181b064a")))),
                    "ostree.summary.mode",
                    new Variant(Signature.parse("s"), "bare"),
                    "ostree.summary.indexed-deltas",
                    new Variant(Signature.parse("b"), true)),
                summary.metadata().fields()));

    System.out.println(summary);
  }

  @Test
  void commitDecoder() {
    var decoder = Commit.decoder();
    var commit = decoder.decode(ByteBuffer.wrap(commitBytes));
    System.out.println(commit);
  }

  @Test
  void dirTreeDecoder() {
    var decoder = DirTree.decoder();
    var dirTree = decoder.decode(ByteBuffer.wrap(dirTreeBytes));
    System.out.println(dirTree);
  }

  @Test
  void dirMetaDecoder() {
    var decoder = DirMeta.decoder();
    var dirMeta = decoder.decode(ByteBuffer.wrap(dirMetaBytes));
    System.out.println(dirMeta);
  }

  @Test
  void superblockDecoder() {
    var decoder = DeltaSuperblock.decoder();
    var deltaSuperblock = decoder.decode(ByteBuffer.wrap(deltaSuperblockBytes));
    System.out.println(deltaSuperblock);
  }

  @Test
  void partPayloadDecoder() {
    var superblockDecoder = DeltaSuperblock.decoder();
    var superblock = superblockDecoder.decode(ByteBuffer.wrap(deltaSuperblockBytes));

    var decoder = DeltaPartPayload.decoder(superblock.entries().get(0));
    var deltaPartPayload = decoder.decode(ByteBuffer.wrap(deltaPartPayloadBytes));

    System.out.println(deltaPartPayload);
  }

  private static List<Byte> bytesOfHex(String hex) {
    var bytes = HexFormat.of().parseHex(hex);
    var byteObjects = new Byte[bytes.length];
    Arrays.setAll(byteObjects, i -> bytes[i]);
    return Arrays.asList(byteObjects);
  }
}
