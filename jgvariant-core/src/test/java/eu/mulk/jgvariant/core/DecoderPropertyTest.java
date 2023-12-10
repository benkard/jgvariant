package eu.mulk.jgvariant.core;

import java.text.ParseException;
import java.util.Optional;
import net.jqwik.api.*;

@SuppressWarnings("java:S2187")
class DecoderPropertyTest {

  @Group
  class VariantRoundtripLaw implements RoundtripLaw<Variant> {

    @Override
    public Decoder<Variant> decoder() {
      return Decoder.ofVariant();
    }

    @Override
    public Arbitrary<Variant> anyT() {
      return anyVariant();
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
  Arbitrary<Variant> anyVariant() {
    var anyString = Arbitraries.strings().map(s -> new Variant(parseSignature("s"), s));
    var anyInt = Arbitraries.integers().map(i -> new Variant(parseSignature("i"), i));
    var anyLong = Arbitraries.longs().map(l -> new Variant(parseSignature("x"), l));
    var anyDouble = Arbitraries.doubles().map(d -> new Variant(parseSignature("d"), d));
    var anyBoolean =
        Arbitraries.of(Boolean.TRUE, Boolean.FALSE).map(b -> new Variant(parseSignature("b"), b));
    var anyByte = Arbitraries.bytes().map(b -> new Variant(parseSignature("y"), b));
    var anyShort = Arbitraries.shorts().map(s -> new Variant(parseSignature("n"), s));
    var anyByteArray = Arbitraries.bytes().list().map(b -> new Variant(parseSignature("ay"), b));
    var anySome =
        Arbitraries.lazyOf(
            () ->
                anyVariant()
                    .map(
                        x ->
                            new Variant(
                                parseSignature("m" + x.signature().toString()),
                                Optional.of(x.value()))));
    var anyNone =
        Arbitraries.lazyOf(
            () ->
                anyVariant()
                    .map(
                        x ->
                            new Variant(
                                parseSignature("m" + x.signature().toString()), Optional.empty())));
    // FIXME missing: list, tuple, dictionary, variant
    return Arbitraries.oneOf(
        anyString,
        anyInt,
        anyLong,
        anyDouble,
        anyBoolean,
        anyByte,
        anyShort,
        anyByteArray,
        anySome,
        anyNone);
  }

  private Signature parseSignature(String s) {
    try {
      return Signature.parse(s);
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }
}
