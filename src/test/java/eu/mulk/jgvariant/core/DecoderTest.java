package eu.mulk.jgvariant.core;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.mulk.jgvariant.core.Value.Array;
import eu.mulk.jgvariant.core.Value.Bool;
import eu.mulk.jgvariant.core.Value.Int32;
import eu.mulk.jgvariant.core.Value.Int8;
import eu.mulk.jgvariant.core.Value.Maybe;
import eu.mulk.jgvariant.core.Value.Str;
import eu.mulk.jgvariant.core.Value.Structure;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Tests based on the examples given in <a
 * href="https://people.gnome.org/~desrt/gvariant-serialisation.pdf">~desrt/gvariant-serialisation.pdf</a>.
 */
class DecoderTest {

  @Test
  void testString() {
    var data = new byte[] {0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x00};
    var decoder = Decoder.ofStr(UTF_8);
    assertEquals(new Str("hello world"), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testMaybe() {
    var data =
        new byte[] {0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x00, 0x00};
    var decoder = Decoder.ofMaybe(Decoder.ofStr(UTF_8));
    assertEquals(
        new Maybe<>(Optional.of(new Str("hello world"))), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testBooleanArray() {
    var data = new byte[] {0x01, 0x00, 0x00, 0x01, 0x01};
    var decoder = Decoder.ofArray(Decoder.ofBoolean());
    assertEquals(
        new Array<>(List.of(Bool.TRUE, Bool.FALSE, Bool.FALSE, Bool.TRUE, Bool.TRUE)),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testStructure() {
    var data =
        new byte[] {
          0x66, 0x6F, 0x6F, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x04
        };

    record TestRecord(Str s, Int32 i) {}

    var decoder = Decoder.ofStructure(TestRecord.class, Decoder.ofStr(UTF_8), Decoder.ofInt32());
    assertEquals(
        new Structure<>(new TestRecord(new Str("foo"), new Int32(-1))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testComplexStructureArray() {
    var data =
        new byte[] {
          0x68,
          0x69,
          0x00,
          0x00,
          (byte) 0xfe,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          0x03,
          0x00,
          0x00,
          0x00,
          0x62,
          0x79,
          0x65,
          0x00,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          (byte) 0xff,
          0x04,
          0x09,
          0x15
        };

    record TestRecord(Str s, Int32 i) {}

    var decoder =
        Decoder.ofArray(
            Decoder.ofStructure(
                TestRecord.class,
                Decoder.ofStr(UTF_8),
                Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN)));
    assertEquals(
        new Array<>(
            List.of(
                new Structure<>(new TestRecord(new Str("hi"), new Int32(-2))),
                new Structure<>(new TestRecord(new Str("bye"), new Int32(-1))))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testStringArray() {
    var data =
        new byte[] {
          0x69, 0x00, 0x63, 0x61, 0x6E, 0x00, 0x68, 0x61, 0x73, 0x00, 0x73, 0x74, 0x72, 0x69, 0x6E,
          0x67, 0x73, 0x3F, 0x00, 0x02, 0x06, 0x0a, 0x13
        };
    var decoder = Decoder.ofArray(Decoder.ofStr(UTF_8));
    assertEquals(
        new Array<>(List.of(new Str("i"), new Str("can"), new Str("has"), new Str("strings?"))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testNestedStructure() {
    var data =
        new byte[] {
          0x69, 0x63, 0x61, 0x6E, 0x00, 0x68, 0x61, 0x73, 0x00, 0x73, 0x74, 0x72, 0x69, 0x6E, 0x67,
          0x73, 0x3F, 0x00, 0x04, 0x0d, 0x05
        };

    record TestChild(Int8 b, Str s) {}
    record TestParent(Structure<TestChild> tc, Array<Str> as) {}

    var decoder =
        Decoder.ofStructure(
            TestParent.class,
            Decoder.ofStructure(TestChild.class, Decoder.ofInt8(), Decoder.ofStr(UTF_8)),
            Decoder.ofArray(Decoder.ofStr(UTF_8)));

    assertEquals(
        new Structure<>(
            new TestParent(
                new Structure<>(new TestChild(new Int8((byte) 0x69), new Str("can"))),
                new Array<>(List.of(new Str("has"), new Str("strings?"))))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testSimpleStructure() {
    var data = new byte[] {0x60, 0x70};

    record TestRecord(Int8 b1, Int8 b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofInt8().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofInt8().withByteOrder(LITTLE_ENDIAN));

    assertEquals(
        new Structure<>(new TestRecord(new Int8((byte) 0x60), new Int8((byte) 0x70))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testPaddedStructureRight() {
    var data = new byte[] {0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00};

    record TestRecord(Int32 b1, Int8 b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofInt8().withByteOrder(LITTLE_ENDIAN));

    assertEquals(
        new Structure<>(new TestRecord(new Int32(0x60), new Int8((byte) 0x70))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testPaddedStructureLeft() {
    var data = new byte[] {0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00};

    record TestRecord(Int8 b1, Int32 b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofInt8().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN));

    assertEquals(
        new Structure<>(new TestRecord(new Int8((byte) 0x60), new Int32(0x70))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testSimpleStructureArray() {
    var data =
        new byte[] {
          0x60,
          0x00,
          0x00,
          0x00,
          0x70,
          0x00,
          0x00,
          0x00,
          (byte) 0x88,
          0x02,
          0x00,
          0x00,
          (byte) 0xF7,
          0x00,
          0x00,
          0x00
        };

    record TestRecord(Int32 b1, Int8 b2) {}

    var decoder =
        Decoder.ofArray(
            Decoder.ofStructure(
                TestRecord.class,
                Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN),
                Decoder.ofInt8().withByteOrder(LITTLE_ENDIAN)));

    assertEquals(
        new Array<>(
            List.of(
                new Structure<>(new TestRecord(new Int32(96), new Int8((byte) 0x70))),
                new Structure<>(new TestRecord(new Int32(648), new Int8((byte) 0xf7))))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testByteArray() {
    var data = new byte[] {0x04, 0x05, 0x06, 0x07};

    var decoder = Decoder.ofArray(Decoder.ofInt8());

    assertEquals(
        new Array<>(
            List.of(
                new Int8((byte) 0x04),
                new Int8((byte) 0x05),
                new Int8((byte) 0x06),
                new Int8((byte) 0x07))),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testIntegerArray() {
    var data = new byte[] {0x04, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00};

    var decoder = Decoder.ofArray(Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN));

    assertEquals(
        new Array<>(List.of(new Int32(4), new Int32(258))), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testDictionaryEntry() {
    var data =
        new byte[] {0x61, 0x20, 0x6B, 0x65, 0x79, 0x00, 0x00, 0x00, 0x02, 0x02, 0x00, 0x00, 0x06};

    record TestEntry(Str key, Int32 value) {}

    var decoder =
        Decoder.ofStructure(
            TestEntry.class, Decoder.ofStr(UTF_8), Decoder.ofInt32().withByteOrder(LITTLE_ENDIAN));
    assertEquals(
        new Structure<>(new TestEntry(new Str("a key"), new Int32(514))),
        decoder.decode(ByteBuffer.wrap(data)));
  }
}
