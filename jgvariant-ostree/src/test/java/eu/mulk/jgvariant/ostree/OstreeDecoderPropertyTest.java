package eu.mulk.jgvariant.ostree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.mulk.jgvariant.core.Decoder;
import java.util.List;
import java.util.Map;
import net.jqwik.api.*;

@SuppressWarnings("java:S2187")
class OstreeDecoderPropertyTest {

  @Group
  class SummaryRoundtripLaw implements RoundtripLaw<Summary> {

    @Override
    public Decoder<Summary> decoder() {
      return Summary.decoder();
    }

    @Override
    public Arbitrary<Summary> anyT() {
      return anySummary();
    }
  }

  @Group
  class DeltaSuperblockRoundtripLaw implements RoundtripLaw<DeltaSuperblock> {

    @Override
    public Decoder<DeltaSuperblock> decoder() {
      return DeltaSuperblock.decoder();
    }

    @Override
    public Arbitrary<DeltaSuperblock> anyT() {
      return anyDeltaSuperblock();
    }
  }

  @Group
  @Disabled(
      "Not implemented correctly: Requires enough file entries to parse all the delta operations.")
  class DeltaPartPayloadRoundtripLaw implements RoundtripLaw<DeltaPartPayload> {

    @Override
    public Decoder<DeltaPartPayload> decoder() {
      // FIXME
      var deltaMetaEntry = new DeltaMetaEntry(0, Checksum.zero(), 0, 0, List.of());
      return DeltaPartPayload.decoder(deltaMetaEntry);
    }

    @Override
    public Arbitrary<DeltaPartPayload> anyT() {
      return anyDeltaPartPayload();
    }
  }

  @Group
  class DirTreeRoundtripLaw implements RoundtripLaw<DirTree> {

    @Override
    public Decoder<DirTree> decoder() {
      return DirTree.decoder();
    }

    @Override
    public Arbitrary<DirTree> anyT() {
      return anyDirTree();
    }
  }

  @Group
  class DirMetaRoundtripLaw implements RoundtripLaw<DirMeta> {

    @Override
    public Decoder<DirMeta> decoder() {
      return DirMeta.decoder();
    }

    @Override
    public Arbitrary<DirMeta> anyT() {
      return anyDirMeta();
    }
  }

  @Group
  class CommitRoundtripLaw implements RoundtripLaw<Commit> {

    @Override
    public Decoder<Commit> decoder() {
      return Commit.decoder();
    }

    @Override
    public Arbitrary<Commit> anyT() {
      return anyCommit();
    }
  }

  interface RoundtripLaw<T> {

    @Property
    default void roundtripsWell(@ForAll(value = "anyT") T entityLeft) {
      var decoder = decoder();
      var bytes = decoder.encode(entityLeft);
      var entityRight = decoder.decode(bytes);
      assertEquals(entityLeft, entityRight);
    }

    Decoder<T> decoder();

    @Provide
    Arbitrary<T> anyT();
  }

  @Provide
  Arbitrary<Summary> anySummary() {
    return Combinators.combine(anySummaryEntry().list(), anyMetadata()).as(Summary::new);
  }

  @Provide
  Arbitrary<Metadata> anyMetadata() {
    return Arbitraries.of(new Metadata(Map.of()));
  }

  @Provide
  Arbitrary<Summary.Entry> anySummaryEntry() {
    return Combinators.combine(Arbitraries.strings(), anySummaryEntryValue())
        .as(Summary.Entry::new);
  }

  @Provide
  Arbitrary<Summary.Entry.Value> anySummaryEntryValue() {
    return Combinators.combine(Arbitraries.integers(), anyChecksum(), anyMetadata())
        .as(Summary.Entry.Value::new);
  }

  @Provide
  Arbitrary<Checksum> anyChecksum() {
    return Arbitraries.bytes()
        .array(byte[].class)
        .ofSize(32)
        .map(ByteString::new)
        .map(Checksum::new);
  }

  @Provide
  Arbitrary<DeltaSuperblock> anyDeltaSuperblock() {
    return Combinators.combine(
            anyMetadata(),
            Arbitraries.longs(),
            anyChecksum(),
            anyChecksum(),
            anyCommit(),
            anyDeltaName().list(),
            anyDeltaMetaEntry().list(),
            anyDeltaFallback().list())
        .as(DeltaSuperblock::new);
  }

  @Provide
  Arbitrary<DeltaPartPayload> anyDeltaPartPayload() {
    return Combinators.combine(
            anyFileMode().list(),
            anyXattr().list().list(),
            anyByteString(),
            anyDeltaOperation().list())
        .as(DeltaPartPayload::new);
  }

  @Provide
  Arbitrary<DeltaOperation> anyDeltaOperation() {
    return Arbitraries.oneOf(
        Combinators.combine(Arbitraries.longs(), Arbitraries.longs())
            .as(DeltaOperation.OpenSpliceAndCloseMeta::new),
        Combinators.combine(
                Arbitraries.longs(), Arbitraries.longs(), Arbitraries.longs(), Arbitraries.longs())
            .as(DeltaOperation.OpenSpliceAndCloseReal::new),
        Combinators.combine(Arbitraries.longs(), Arbitraries.longs(), Arbitraries.longs())
            .as(DeltaOperation.Open::new),
        Combinators.combine(Arbitraries.longs(), Arbitraries.longs()).as(DeltaOperation.Write::new),
        Arbitraries.longs().map(DeltaOperation.SetReadSource::new),
        Arbitraries.of(new DeltaOperation.UnsetReadSource()),
        Arbitraries.of(new DeltaOperation.Close()),
        Combinators.combine(Arbitraries.longs(), Arbitraries.longs())
            .as(DeltaOperation.BsPatch::new));
  }

  @Provide
  Arbitrary<DeltaPartPayload.FileMode> anyFileMode() {
    return Combinators.combine(
            Arbitraries.integers(), Arbitraries.integers(), Arbitraries.integers())
        .as(DeltaPartPayload.FileMode::new);
  }

  @Provide
  Arbitrary<Xattr> anyXattr() {
    return Combinators.combine(anyByteString(), anyByteString()).as(Xattr::new);
  }

  @Provide
  Arbitrary<ByteString> anyByteString() {
    return Arbitraries.bytes().array(byte[].class).map(ByteString::new);
  }

  @Provide
  Arbitrary<DirTree> anyDirTree() {
    return Combinators.combine(anyDirTreeFile().list(), anyDirTreeDirectory().list())
        .as(DirTree::new);
  }

  @Provide
  Arbitrary<DirMeta> anyDirMeta() {
    return Combinators.combine(
            Arbitraries.integers(),
            Arbitraries.integers(),
            Arbitraries.integers(),
            anyXattr().list())
        .as(DirMeta::new);
  }

  @Provide
  Arbitrary<Commit> anyCommit() {
    return Combinators.combine(
            anyMetadata(),
            anyChecksum(),
            anyRelatedObject().list(),
            Arbitraries.strings(),
            Arbitraries.strings(),
            Arbitraries.longs(),
            anyChecksum(),
            anyChecksum())
        .as(Commit::new);
  }

  @Provide
  Arbitrary<Commit.RelatedObject> anyRelatedObject() {
    return Combinators.combine(Arbitraries.strings(), anyChecksum()).as(Commit.RelatedObject::new);
  }

  @Provide
  Arbitrary<DeltaSuperblock.DeltaName> anyDeltaName() {
    return Combinators.combine(anyChecksum(), anyChecksum()).as(DeltaSuperblock.DeltaName::new);
  }

  @Provide
  Arbitrary<DeltaMetaEntry> anyDeltaMetaEntry() {
    return Combinators.combine(
            Arbitraries.integers(),
            anyChecksum(),
            Arbitraries.longs(),
            Arbitraries.longs(),
            anyDeltaObject().list())
        .as(DeltaMetaEntry::new);
  }

  @Provide
  Arbitrary<DeltaMetaEntry.DeltaObject> anyDeltaObject() {
    return Combinators.combine(anyObjectType(), anyChecksum()).as(DeltaMetaEntry.DeltaObject::new);
  }

  @Provide
  Arbitrary<ObjectType> anyObjectType() {
    return Arbitraries.of(ObjectType.values());
  }

  @Provide
  Arbitrary<DeltaFallback> anyDeltaFallback() {
    return Combinators.combine(
            anyObjectType(), anyChecksum(), Arbitraries.longs(), Arbitraries.longs())
        .as(DeltaFallback::new);
  }

  @Provide
  Arbitrary<DirTree.Directory> anyDirTreeDirectory() {
    return Combinators.combine(Arbitraries.strings(), anyChecksum(), anyChecksum())
        .as(DirTree.Directory::new);
  }

  @Provide
  Arbitrary<DirTree.File> anyDirTreeFile() {
    return Combinators.combine(Arbitraries.strings(), anyChecksum()).as(DirTree.File::new);
  }
}
