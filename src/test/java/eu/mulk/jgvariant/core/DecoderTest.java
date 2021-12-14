package eu.mulk.jgvariant.core;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    var decoder = Decoder.ofString(UTF_8);
    assertEquals("hello world", decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testMaybe() {
    var data =
        new byte[] {0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x00, 0x00};
    var decoder = Decoder.ofMaybe(Decoder.ofString(UTF_8));
    assertEquals(Optional.of("hello world"), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testBooleanArray() {
    var data = new byte[] {0x01, 0x00, 0x00, 0x01, 0x01};
    var decoder = Decoder.ofArray(Decoder.ofBoolean());
    assertEquals(List.of(true, false, false, true, true), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testStructure() {
    var data =
        new byte[] {
          0x66, 0x6F, 0x6F, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x04
        };

    record TestRecord(String s, int i) {}

    var decoder = Decoder.ofStructure(TestRecord.class, Decoder.ofString(UTF_8), Decoder.ofInt());
    assertEquals(new TestRecord("foo", -1), decoder.decode(ByteBuffer.wrap(data)));
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

    record TestRecord(String s, int i) {}

    var decoder =
        Decoder.ofArray(
            Decoder.ofStructure(
                TestRecord.class,
                Decoder.ofString(UTF_8),
                Decoder.ofInt().withByteOrder(LITTLE_ENDIAN)));
    assertEquals(
        List.of(new TestRecord("hi", -2), new TestRecord("bye", -1)),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testStringArray() {
    var data =
        new byte[] {
          0x69, 0x00, 0x63, 0x61, 0x6E, 0x00, 0x68, 0x61, 0x73, 0x00, 0x73, 0x74, 0x72, 0x69, 0x6E,
          0x67, 0x73, 0x3F, 0x00, 0x02, 0x06, 0x0a, 0x13
        };
    var decoder = Decoder.ofArray(Decoder.ofString(UTF_8));
    assertEquals(List.of("i", "can", "has", "strings?"), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testNestedStructure() {
    var data =
        new byte[] {
          0x69, 0x63, 0x61, 0x6E, 0x00, 0x68, 0x61, 0x73, 0x00, 0x73, 0x74, 0x72, 0x69, 0x6E, 0x67,
          0x73, 0x3F, 0x00, 0x04, 0x0d, 0x05
        };

    record TestChild(byte b, String s) {}
    record TestParent(TestChild tc, List<String> as) {}

    var decoder =
        Decoder.ofStructure(
            TestParent.class,
            Decoder.ofStructure(TestChild.class, Decoder.ofByte(), Decoder.ofString(UTF_8)),
            Decoder.ofArray(Decoder.ofString(UTF_8)));

    assertEquals(
        new TestParent(new TestChild((byte) 0x69, "can"), List.of("has", "strings?")),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testSimpleStructure() {
    var data = new byte[] {0x60, 0x70};

    record TestRecord(byte b1, byte b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN));

    assertEquals(new TestRecord((byte) 0x60, (byte) 0x70), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testPaddedStructureRight() {
    var data = new byte[] {0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00};

    record TestRecord(int b1, byte b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofInt().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN));

    assertEquals(new TestRecord(0x60, (byte) 0x70), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testPaddedStructureLeft() {
    var data = new byte[] {0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00};

    record TestRecord(byte b1, int b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));

    assertEquals(new TestRecord((byte) 0x60, 0x70), decoder.decode(ByteBuffer.wrap(data)));
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

    record TestRecord(int b1, byte b2) {}

    var decoder =
        Decoder.ofArray(
            Decoder.ofStructure(
                TestRecord.class,
                Decoder.ofInt().withByteOrder(LITTLE_ENDIAN),
                Decoder.ofByte().withByteOrder(LITTLE_ENDIAN)));

    assertEquals(
        List.of(new TestRecord(96, (byte) 0x70), new TestRecord(648, (byte) 0xf7)),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testByteArray() {
    var data = new byte[] {0x04, 0x05, 0x06, 0x07};

    var decoder = Decoder.ofArray(Decoder.ofByte());

    assertEquals(
        List.of((byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07),
        decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testIntegerArray() {
    var data = new byte[] {0x04, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00};

    var decoder = Decoder.ofArray(Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));

    assertEquals(List.of(4, 258), decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void testDictionaryEntry() {
    var data =
        new byte[] {0x61, 0x20, 0x6B, 0x65, 0x79, 0x00, 0x00, 0x00, 0x02, 0x02, 0x00, 0x00, 0x06};

    record TestEntry(String key, int value) {}

    var decoder =
        Decoder.ofStructure(
            TestEntry.class, Decoder.ofString(UTF_8), Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));
    assertEquals(new TestEntry("a key", 514), decoder.decode(ByteBuffer.wrap(data)));
  }
}