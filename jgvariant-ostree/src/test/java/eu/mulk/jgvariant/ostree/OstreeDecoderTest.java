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
import java.util.*;
import org.junit.jupiter.api.Test;

@TestWithResources
@SuppressWarnings({
  "DoubleBraceInitialization",
  "ImmutableListOf1",
  "ImmutableMapOf1",
  "initialization.field.uninitialized",
  "NullAway",
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
    var input = ByteBuffer.wrap(summaryBytes);
    var summary = decoder.decode(input);

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
                new LinkedHashMap<String, Variant>() {
                  {
                    put("ostree.summary.mode", new Variant(Signature.parse("s"), "bare"));
                    put(
                        "ostree.summary.last-modified",
                        new Variant(Signature.parse("t"), 1640537300L));
                    put(
                        "ostree.summary.tombstone-commits",
                        new Variant(Signature.parse("b"), false));
                    put(
                        "ostree.static-deltas",
                        new Variant(
                            Signature.parse("a{sv}"),
                            new LinkedHashMap<String, Variant>() {
                              {
                                put(
                                    "3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f-66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350",
                                    new Variant(
                                        Signature.parse("ay"),
                                        bytesOfHex(
                                            "03738040e28e7662e9c9d2599c530ea974e642c9f87e6c00cbaa39a0cdac8d44")));
                                put(
                                    "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52",
                                    new Variant(
                                        Signature.parse("ay"),
                                        bytesOfHex(
                                            "f481144629474bd88c106e45ac405ebd75b324b0655af1aec14b31786ae1fd61")));
                                put(
                                    "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52-3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f",
                                    new Variant(
                                        Signature.parse("ay"),
                                        bytesOfHex(
                                            "2c6a07bc1cf4d7ce7d00f82d7d2d6d156fd0e31d476851b46dc2306b181b064a")));
                              }
                            }));
                    put("ostree.summary.indexed-deltas", new Variant(Signature.parse("b"), true));
                  }
                },
                summary.metadata().fields()));

    var encoded = decoder.encode(summary);
    input.rewind();
    assertEquals(input, encoded);

    System.out.println(summary);
  }

  @Test
  void commitDecoder() {
    var decoder = Commit.decoder();
    var input = ByteBuffer.wrap(commitBytes);
    var commit = decoder.decode(input);

    var encoded = decoder.encode(commit);
    input.rewind();
    assertEquals(input, encoded);

    System.out.println(commit);
  }

  @Test
  void dirTreeDecoder() {
    var decoder = DirTree.decoder();
    var input = ByteBuffer.wrap(dirTreeBytes);
    var dirTree = decoder.decode(input);

    var encoded = decoder.encode(dirTree);
    input.rewind();
    assertEquals(input, encoded);

    System.out.println(dirTree);
  }

  @Test
  void dirMetaDecoder() {
    var decoder = DirMeta.decoder();
    var input = ByteBuffer.wrap(dirMetaBytes);
    var dirMeta = decoder.decode(ByteBuffer.wrap(dirMetaBytes));

    var encoded = decoder.encode(dirMeta);
    input.rewind();
    assertEquals(input, encoded);

    System.out.println(dirMeta);
  }

  @Test
  void superblockDecoder() {
    var decoder = DeltaSuperblock.decoder();
    var input = ByteBuffer.wrap(deltaSuperblockBytes);
    var deltaSuperblock = decoder.decode(input);
    System.out.println(deltaSuperblock);

    var encoded = decoder.encode(deltaSuperblock);
    input.rewind();
    assertEquals(input, encoded);
  }

  @Test
  void partPayloadDecoder() {
    var superblockDecoder = DeltaSuperblock.decoder();
    var superblock = superblockDecoder.decode(ByteBuffer.wrap(deltaSuperblockBytes));

    var decoder = DeltaPartPayload.decoder(superblock.entries().get(0));
    var input = ByteBuffer.wrap(deltaPartPayloadBytes);
    var deltaPartPayload = decoder.decode(input);

    var encoded = decoder.encode(deltaPartPayload);
    var decodedAgain = decoder.decode(encoded);
    assertEquals(deltaPartPayload, decodedAgain);
  }

  private static List<Byte> bytesOfHex(String hex) {
    var bytes = HexFormat.of().parseHex(hex);
    var byteObjects = new Byte[bytes.length];
    Arrays.setAll(byteObjects, i -> bytes[i]);
    return Arrays.asList(byteObjects);
  }
}
