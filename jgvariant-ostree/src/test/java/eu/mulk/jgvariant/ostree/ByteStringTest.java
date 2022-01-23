package eu.mulk.jgvariant.ostree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ByteStringTest {

  @Test
  void testToModifiedBase64() {
    assertEquals("MciDXVydLGaHpQCRyFFC0bLYU_9Bap+4G07jB1RRDVI", testByteString1.modifiedBase64());
  }

  @Test
  void testOfModifiedBase64() {
    assertEquals(
        testByteString1,
        ByteString.ofModifiedBase64("MciDXVydLGaHpQCRyFFC0bLYU_9Bap+4G07jB1RRDVI"));
  }

  @Test
  void testToHex() {
    assertEquals(
        "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52", testByteString1.hex());
  }

  @Test
  void testOfHex() {
    assertEquals(
        testByteString1,
        ByteString.ofHex("31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52"));
  }

  private static final ByteString testByteString1 =
      new ByteString(
          new byte[] {
            (byte) 0x31,
            (byte) 0xc8,
            (byte) 0x83,
            (byte) 0x5d,
            (byte) 0x5c,
            (byte) 0x9d,
            (byte) 0x2c,
            (byte) 0x66,
            (byte) 0x87,
            (byte) 0xa5,
            (byte) 0x00,
            (byte) 0x91,
            (byte) 0xc8,
            (byte) 0x51,
            (byte) 0x42,
            (byte) 0xd1,
            (byte) 0xb2,
            (byte) 0xd8,
            (byte) 0x53,
            (byte) 0xff,
            (byte) 0x41,
            (byte) 0x6a,
            (byte) 0x9f,
            (byte) 0xb8,
            (byte) 0x1b,
            (byte) 0x4e,
            (byte) 0xe3,
            (byte) 0x07,
            (byte) 0x54,
            (byte) 0x51,
            (byte) 0x0d,
            (byte) 0x52
          });
}
