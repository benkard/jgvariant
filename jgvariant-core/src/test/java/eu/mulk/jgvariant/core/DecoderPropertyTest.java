package eu.mulk.jgvariant.core;

import java.text.ParseException;
import java.util.*;
import java.util.function.Supplier;
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
      return Objects.equals(entityLeft, entityRight);
    }

    Decoder<T> decoder();

    @Provide
    Arbitrary<T> anyT();
  }

  @Provide
  Arbitrary<Variant> anyVariant() {
    // Base cases
    var anyString = Arbitraries.strings().map(s -> new Variant(parseSignature("s"), s));
    var anyInt = Arbitraries.integers().map(i -> new Variant(parseSignature("i"), i));
    var anyLong = Arbitraries.longs().map(l -> new Variant(parseSignature("x"), l));
    var anyDouble = Arbitraries.doubles().map(d -> new Variant(parseSignature("d"), d));
    var anyBoolean =
        Arbitraries.of(Boolean.TRUE, Boolean.FALSE).map(b -> new Variant(parseSignature("b"), b));
    var anyByte = Arbitraries.bytes().map(b -> new Variant(parseSignature("y"), b));
    var anyShort = Arbitraries.shorts().map(s -> new Variant(parseSignature("n"), s));
    var anyByteArray = Arbitraries.bytes().list().map(b -> new Variant(parseSignature("ay"), b));

    // Singly recursive cases
    Supplier<Arbitrary<Variant>> anySome =
        () ->
            anyVariant()
                .map(
                    x ->
                        new Variant(
                            parseSignature("m" + x.signature().toString()),
                            Optional.of(x.value())));
    Supplier<Arbitrary<Variant>> anyNone =
        () ->
            anyVariant()
                .map(
                    x ->
                        new Variant(
                            parseSignature("m" + x.signature().toString()), Optional.empty()));
    Supplier<Arbitrary<Variant>> anyNestedVariant =
        () -> anyVariant().map(v -> new Variant(parseSignature("v"), v));

    // Fixed-multiplicity recursive cases
    /* fixme: Object[] does not work for comparison by Object#equals() */
    Supplier<Arbitrary<Variant>> anyPair =
        () ->
            Combinators.combine(anyVariant(), anyVariant())
                .as(
                    (v1, v2) ->
                        new Variant(
                            parseSignature("(%s%s)".formatted(v1.signature(), v2.signature())),
                            new Object[] {v1.value(), v2.value()}));
    Supplier<Arbitrary<Variant>> anyTriple =
        () ->
            Combinators.combine(anyVariant(), anyVariant(), anyVariant())
                .as(
                    (v1, v2, v3) ->
                        new Variant(
                            parseSignature(
                                "(%s%s%s)"
                                    .formatted(v1.signature(), v2.signature(), v3.signature())),
                            new Object[] {v1.value(), v2.value(), v3.value()}));

    // Indefinite-multiplicity recursive cases
    Supplier<Arbitrary<Variant>> anyVariantList =
        () ->
            anyVariant().list().map(ArrayList::new).map(l -> new Variant(parseSignature("av"), l));
    Supplier<Arbitrary<Variant>> anyStringVariantMap =
        () ->
            Arbitraries.maps(Arbitraries.strings(), anyVariant())
                .map(LinkedHashMap::new)
                .map(m -> new Variant(parseSignature("a{sv}"), m));

    // fixme: anyPair, anyTriple (see above)
    return Arbitraries.frequencyOf(
        Tuple.of(10, anyString),
        Tuple.of(10, anyInt),
        Tuple.of(10, anyLong),
        Tuple.of(10, anyDouble),
        Tuple.of(10, anyBoolean),
        Tuple.of(10, anyByte),
        Tuple.of(10, anyShort),
        Tuple.of(10, anyByteArray),
        Tuple.of(10, Arbitraries.lazy(anySome)),
        Tuple.of(10, Arbitraries.lazy(anyNone)),
        Tuple.of(10, Arbitraries.lazy(anyNestedVariant)),
        Tuple.of(1, Arbitraries.lazy(anyStringVariantMap)),
        Tuple.of(1, Arbitraries.lazy(anyVariantList))
        /*,
        anyPair,
        anyTriple
        */ );
  }

  private Signature parseSignature(String s) {
    try {
      return Signature.parse(s);
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }
}
