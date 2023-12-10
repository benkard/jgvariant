package eu.mulk.jgvariant.ostree;

import eu.mulk.jgvariant.core.Decoder;
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

  interface RoundtripLaw<T> {

    @Property
    default boolean roundtripsWell(@ForAll(value = "anyT") T entityLeft) {
      var decoder = decoder();
      var bytes = decoder.encode(entityLeft);
      var entityRight = decoder.decode(bytes);
      return entityLeft.equals(entityRight);
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
    return Arbitraries.of(new Checksum(new ByteString(new byte[32])));
  }
}
