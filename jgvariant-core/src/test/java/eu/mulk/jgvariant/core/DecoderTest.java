// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.core;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.*;
import org.junit.jupiter.api.Test;

/**
 * Tests based on the examples given in <a
 * href="https://people.gnome.org/~desrt/gvariant-serialisation.pdf">~desrt/gvariant-serialisation.pdf</a>.
 */
@SuppressWarnings({
  "ByteBufferBackingArray",
  "ImmutableListOf",
  "ImmutableListOf1",
  "ImmutableListOf2",
  "ImmutableListOf3",
  "ImmutableListOf4",
  "ImmutableListOf5",
  "ImmutableMapOf2",
})
class DecoderTest {

  @Test
  void string() {
    var data = new byte[] {0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x00};
    var decoder = Decoder.ofString(UTF_8);
    assertEquals("hello world", decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode("hello world");
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void maybe() {
    var data =
        new byte[] {0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x00, 0x00};
    var decoder = Decoder.ofMaybe(Decoder.ofString(UTF_8));
    assertEquals(Optional.of("hello world"), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(Optional.of("hello world"));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void booleanArray() {
    var data = new byte[] {0x01, 0x00, 0x00, 0x01, 0x01};
    var decoder = Decoder.ofArray(Decoder.ofBoolean());
    assertEquals(List.of(true, false, false, true, true), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of(true, false, false, true, true));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void structure() {
    var data =
        new byte[] {
          0x66, 0x6F, 0x6F, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x04
        };

    record TestRecord(String s, int i) {}

    var decoder = Decoder.ofStructure(TestRecord.class, Decoder.ofString(UTF_8), Decoder.ofInt());
    assertEquals(new TestRecord("foo", -1), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord("foo", -1));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void complexStructureArray() {
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

    var roundtripData =
        decoder.encode(List.of(new TestRecord("hi", -2), new TestRecord("bye", -1)));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void dictionary() {
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

    var decoder =
        Decoder.ofDictionary(Decoder.ofString(UTF_8), Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));
    assertEquals(Map.of("hi", -2, "bye", -1), decoder.decode(ByteBuffer.wrap(data)));

    var entity = new LinkedHashMap<String, Integer>();
    entity.put("hi", -2);
    entity.put("bye", -1);
    var roundtripData = decoder.encode(entity);
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void stringArray() {
    var data =
        new byte[] {
          0x69, 0x00, 0x63, 0x61, 0x6E, 0x00, 0x68, 0x61, 0x73, 0x00, 0x73, 0x74, 0x72, 0x69, 0x6E,
          0x67, 0x73, 0x3F, 0x00, 0x02, 0x06, 0x0a, 0x13
        };
    var decoder = Decoder.ofArray(Decoder.ofString(UTF_8));
    assertEquals(List.of("i", "can", "has", "strings?"), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of("i", "can", "has", "strings?"));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void nestedStructure() {
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

    var roundtripData =
        decoder.encode(
            new TestParent(new TestChild((byte) 0x69, "can"), List.of("has", "strings?")));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void nestedStructureVariant() {
    var data =
        new byte[] {
          0x69, 0x63, 0x61, 0x6E, 0x00, 0x68, 0x61, 0x73, 0x00, 0x73, 0x74, 0x72, 0x69, 0x6E, 0x67,
          0x73, 0x3F, 0x00, 0x04, 0x0d, 0x05, 0x00, 0x28, 0x28, 0x79, 0x73, 0x29, 0x61, 0x73, 0x29
        };

    var decoder = Decoder.ofVariant();
    var variant = decoder.decode(ByteBuffer.wrap(data));
    var result = (Object[]) variant.value();

    assertAll(
        () -> assertEquals(Signature.parse("((ys)as)"), variant.signature()),
        () -> assertEquals(2, result.length),
        () -> assertArrayEquals(new Object[] {(byte) 0x69, "can"}, (Object[]) result[0]),
        () -> assertEquals(List.of("has", "strings?"), result[1]));

    var roundtripData = decoder.encode(variant);
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void simpleStructure() {
    var data = new byte[] {0x60, 0x70};

    record TestRecord(byte b1, byte b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN));

    assertEquals(new TestRecord((byte) 0x60, (byte) 0x70), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord((byte) 0x60, (byte) 0x70));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void paddedStructureRight() {
    var data = new byte[] {0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00};

    record TestRecord(int b1, byte b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofInt().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN));

    assertEquals(new TestRecord(0x60, (byte) 0x70), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord(0x60, (byte) 0x70));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void paddedStructureLeft() {
    var data = new byte[] {0x60, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00};

    record TestRecord(byte b1, int b2) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofByte().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));

    assertEquals(new TestRecord((byte) 0x60, 0x70), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord((byte) 0x60, 0x70));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void simpleStructureArray() {
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

    var roundtripData =
        decoder.encode(List.of(new TestRecord(96, (byte) 0x70), new TestRecord(648, (byte) 0xf7)));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void byteArray() {
    var data = new byte[] {0x04, 0x05, 0x06, 0x07};

    var decoder = Decoder.ofArray(Decoder.ofByte());

    assertEquals(
        List.of((byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07),
        decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of((byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void primitiveByteArray() {
    var data = new byte[] {0x04, 0x05, 0x06, 0x07};

    var decoder = Decoder.ofByteArray();

    assertArrayEquals(data, decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(data);
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void primitiveByteArrayRecord() {
    var data = new byte[] {0x04, 0x05, 0x06, 0x07};

    record TestRecord(byte[] bytes) {}

    var decoder = Decoder.ofStructure(TestRecord.class, Decoder.ofByteArray());

    assertArrayEquals(data, decoder.decode(ByteBuffer.wrap(data)).bytes());

    var roundtripData = decoder.encode(new TestRecord(data));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void integerArray() {
    var data = new byte[] {0x04, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00};

    var decoder = Decoder.ofArray(Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));

    assertEquals(List.of(4, 258), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of(4, 258));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void dictionaryEntryAsMapEntry() {
    var data =
        new byte[] {0x61, 0x20, 0x6B, 0x65, 0x79, 0x00, 0x00, 0x00, 0x02, 0x02, 0x00, 0x00, 0x06};

    var decoder =
        Decoder.ofDictionaryEntry(
            Decoder.ofString(UTF_8), Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));
    assertEquals(Map.entry("a key", 514), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(Map.entry("a key", 514));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void dictionaryEntryAsRecord() {
    var data =
        new byte[] {0x61, 0x20, 0x6B, 0x65, 0x79, 0x00, 0x00, 0x00, 0x02, 0x02, 0x00, 0x00, 0x06};

    record TestEntry(String key, int value) {}

    var decoder =
        Decoder.ofStructure(
            TestEntry.class, Decoder.ofString(UTF_8), Decoder.ofInt().withByteOrder(LITTLE_ENDIAN));
    assertEquals(new TestEntry("a key", 514), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestEntry("a key", 514));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void paddedPrimitives() {
    var data =
        new byte[] {
          0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
          0x00, 0x40, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };

    record TestRecord(short s, long l, double d) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class,
            Decoder.ofShort().withByteOrder(BIG_ENDIAN),
            Decoder.ofLong().withByteOrder(LITTLE_ENDIAN),
            Decoder.ofDouble());
    assertEquals(new TestRecord((short) 1, 2, 3.25), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord((short) 1, 2, 3.25));

    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void embeddedMaybe() {
    var data = new byte[] {0x01, 0x01};

    record TestRecord(Optional<Byte> set, Optional<Byte> unset) {}

    var decoder =
        Decoder.ofStructure(
            TestRecord.class, Decoder.ofMaybe(Decoder.ofByte()), Decoder.ofMaybe(Decoder.ofByte()));
    assertEquals(
        new TestRecord(Optional.of((byte) 1), Optional.empty()),
        decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord(Optional.of((byte) 1), Optional.empty()));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void recordComponentMismatch() {
    record TestRecord(Optional<Byte> set) {}

    var maybeDecoder = Decoder.ofMaybe(Decoder.ofByte());
    assertThrows(
        IllegalArgumentException.class,
        () -> Decoder.ofStructure(TestRecord.class, maybeDecoder, maybeDecoder));
  }

  @Test
  void trivialRecord() {
    var data = new byte[] {0x00};

    record TestRecord() {}

    var decoder = Decoder.ofStructure(TestRecord.class);
    assertEquals(new TestRecord(), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(new TestRecord());
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void twoElementTrivialRecordArray() {
    var data = new byte[] {0x00, 0x00};

    record TestRecord() {}

    var decoder = Decoder.ofArray(Decoder.ofStructure(TestRecord.class));
    assertEquals(
        List.of(new TestRecord(), new TestRecord()), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of(new TestRecord(), new TestRecord()));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void singletonTrivialRecordArray() {
    var data = new byte[] {0x00};

    record TestRecord() {}

    var decoder = Decoder.ofArray(Decoder.ofStructure(TestRecord.class));
    assertEquals(List.of(new TestRecord()), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of(new TestRecord()));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void emptyTrivialRecordArray() {
    var data = new byte[] {};

    record TestRecord() {}

    var decoder = Decoder.ofArray(Decoder.ofStructure(TestRecord.class));
    assertEquals(List.of(), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of());
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void variantArray() {
    var data = new byte[] {};

    record TestRecord() {}

    var decoder = Decoder.ofArray(Decoder.ofStructure(TestRecord.class));
    assertEquals(List.of(), decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData = decoder.encode(List.of());
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void invalidVariantSignature() {
    var data = new byte[] {0x00, 0x00, 0x2E};

    var decoder = Decoder.ofVariant();
    assertThrows(IllegalArgumentException.class, () -> decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void missingVariantSignature() {
    var data = new byte[] {0x01};

    var decoder = Decoder.ofVariant();
    assertThrows(IllegalArgumentException.class, () -> decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void simpleVariantRecord() throws ParseException {
    // signature: "(bynqiuxtdsogvmiai)"
    var data =
        new byte[] {
          0x01, // b
          0x02, // y
          0x00, 0x03, // n
          0x00, 0x04, // q
          0x00, 0x00, // (padding)
          0x00, 0x00, 0x00, 0x05, // i
          0x00, 0x00, 0x00, 0x06, // u
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, // x
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, // t
          0x40, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // d
          0x68, 0x69, 0x00, // s
          0x68, 0x69, 0x00, // o
          0x68, 0x69, 0x00, // g
          0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // (padding)
          0x00, 0x00, 0x00, 0x09, 0x00, 0x69, // v
          0x00, 0x00, // (padding)
          0x00, 0x00, 0x00, 0x0a, // mi
          0x00, 0x00, 0x00, 0x0b, 0x00, 0x00, 0x00, 0x0c, // ai
          68, 62, 49, 46, 43, // framing offsets
          0x00, 0x28, 0x62, 0x79, 0x6E, 0x71, 0x69, 0x75, 0x78, 0x74, 0x64, 0x73, 0x6F, 0x67, 0x76,
          0x6D, 0x69, 0x61, 0x69, 0x29
        };

    var decoder = Decoder.ofVariant();
    assertArrayEquals(
        new Object[] {
          true,
          (byte) 2,
          (short) 3,
          (short) 4,
          (int) 5,
          (int) 6,
          (long) 7,
          (long) 8,
          (double) 3.25,
          "hi",
          "hi",
          "hi",
          new Variant(Signature.parse("i"), 9),
          Optional.of(10),
          List.of(11, 12)
        },
        (Object[]) decoder.decode(ByteBuffer.wrap(data)).value());

    var roundtripData = decoder.encode(decoder.decode(ByteBuffer.wrap(data)));
    assertArrayEquals(data, roundtripData.array());
  }

  @Test
  void signatureString() throws ParseException {
    var data =
        new byte[] {
          0x28, 0x62, 0x79, 0x6E, 0x71, 0x69, 0x75, 0x78, 0x74, 0x64, 0x73, 0x6F, 0x67, 0x76, 0x6D,
          0x69, 0x61, 0x69, 0x29
        };

    var signature = Signature.parse(ByteBuffer.wrap(data));
    assertEquals("(bynqiuxtdsogvmiai)", signature.toString());
  }

  @Test
  void map() {
    var data = new byte[] {0x0A, 0x0B, 0x0C};
    var decoder =
        Decoder.ofByteArray()
            .map(
                bytes -> bytes.length,
                len -> {
                  throw new UnsupportedOperationException();
                });
    assertEquals(3, decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void contramap() {
    var data = new byte[] {0x0A, 0x0B, 0x0C};
    var decoder =
        Decoder.ofByteArray()
            .contramap(
                bytes -> bytes.slice(1, 1),
                bytes -> {
                  throw new UnsupportedOperationException();
                });
    assertArrayEquals(new byte[] {0x0B}, decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void predicateTrue() {
    var data = new byte[] {0x00, 0x01, 0x00};
    var innerDecoder =
        Decoder.ofShort()
            .contramap(
                bytes -> bytes.slice(1, 2).order(bytes.order()),
                bytes -> {
                  throw new UnsupportedOperationException();
                });
    var decoder =
        Decoder.ofPredicate(
            byteBuffer -> byteBuffer.get(0) == 0,
            innerDecoder.withByteOrder(LITTLE_ENDIAN),
            innerDecoder.withByteOrder(BIG_ENDIAN));
    assertEquals((short) 1, decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void predicateFalse() {
    var data = new byte[] {0x01, 0x01, 0x00};
    var innerDecoder =
        Decoder.ofShort()
            .contramap(
                bytes -> bytes.slice(1, 2).order(bytes.order()),
                bytes -> {
                  throw new UnsupportedOperationException();
                });
    var decoder =
        Decoder.ofPredicate(
            byteBuffer -> byteBuffer.get(0) == 0,
            innerDecoder.withByteOrder(LITTLE_ENDIAN),
            innerDecoder.withByteOrder(BIG_ENDIAN));
    assertEquals((short) 256, decoder.decode(ByteBuffer.wrap(data)));
  }

  @Test
  void byteOrder() {
    var data =
        new byte[] {
          0x01, 0x00, 0x02, 0x00, 0x00, 0x03, 0x00, 0x04, 0x05, 0x00, 0x00, 0x06, 0x00, 0x07, 0x08,
          0x00
        };

    record TestChild(short s1, short s2) {}
    record TestParent(TestChild tc1, TestChild tc2, TestChild tc3, TestChild tc4) {}

    var decoder =
        Decoder.ofStructure(
            TestParent.class,
            Decoder.ofStructure(TestChild.class, Decoder.ofShort(), Decoder.ofShort())
                .withByteOrder(LITTLE_ENDIAN),
            Decoder.ofStructure(TestChild.class, Decoder.ofShort(), Decoder.ofShort())
                .withByteOrder(BIG_ENDIAN),
            Decoder.ofStructure(
                    TestChild.class,
                    Decoder.ofShort().withByteOrder(LITTLE_ENDIAN),
                    Decoder.ofShort())
                .withByteOrder(BIG_ENDIAN),
            Decoder.ofStructure(
                    TestChild.class, Decoder.ofShort().withByteOrder(BIG_ENDIAN), Decoder.ofShort())
                .withByteOrder(LITTLE_ENDIAN));

    assertEquals(
        new TestParent(
            new TestChild((short) 1, (short) 2),
            new TestChild((short) 3, (short) 4),
            new TestChild((short) 5, (short) 6),
            new TestChild((short) 7, (short) 8)),
        decoder.decode(ByteBuffer.wrap(data)));

    var roundtripData =
        decoder.encode(
            new TestParent(
                new TestChild((short) 1, (short) 2),
                new TestChild((short) 3, (short) 4),
                new TestChild((short) 5, (short) 6),
                new TestChild((short) 7, (short) 8)));
    assertArrayEquals(data, roundtripData.array());
  }
}
