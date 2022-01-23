package eu.mulk.jgvariant.ostree;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.adelean.inject.resources.junit.jupiter.GivenBinaryResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import eu.mulk.jgvariant.core.Signature;
import eu.mulk.jgvariant.core.Variant;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@TestWithResources
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
  void testSummaryDecoder() {
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
                summary.entries()));
    // FIXME: check metadata field
    System.out.println(summary);
  }

  @Test
  void testCommitDecoder() {
    var decoder = Commit.decoder();
    var commit = decoder.decode(ByteBuffer.wrap(commitBytes));
    System.out.println(commit);
  }

  @Test
  void testDirTreeDecoder() {
    var decoder = DirTree.decoder();
    var dirTree = decoder.decode(ByteBuffer.wrap(dirTreeBytes));
    System.out.println(dirTree);
  }

  @Test
  void testDirMetaDecoder() {
    var decoder = DirMeta.decoder();
    var dirMeta = decoder.decode(ByteBuffer.wrap(dirMetaBytes));
    System.out.println(dirMeta);
  }

  @Test
  void testSuperblockDecoder() {
    var decoder = DeltaSuperblock.decoder();
    var deltaSuperblock = decoder.decode(ByteBuffer.wrap(deltaSuperblockBytes));
    System.out.println(deltaSuperblock);
  }

  @Test
  void testPartPayloadDecoder() {
    var superblockDecoder = DeltaSuperblock.decoder();
    var superblock = superblockDecoder.decode(ByteBuffer.wrap(deltaSuperblockBytes));

    var decoder = DeltaPartPayload.decoder(superblock.entries().get(0));
    var deltaPartPayload = decoder.decode(ByteBuffer.wrap(deltaPartPayloadBytes));

    System.out.println(deltaPartPayload);
  }
}
