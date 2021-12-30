package eu.mulk.jgvariant.ostree;

import java.nio.ByteBuffer;

/** An operation in a static delta. */
public sealed interface DeltaOperation {

  record OpenSpliceAndCloseMeta(long offset, long size) implements DeltaOperation {}

  record OpenSpliceAndCloseReal(long offset, long size, long modeOffset, long xattrOffset)
      implements DeltaOperation {}

  record Open(long size, long modeOffset, long xattrOffset) implements DeltaOperation {}

  record Write(long offset, long size) implements DeltaOperation {}

  record SetReadSource(long offset) implements DeltaOperation {}

  record UnsetReadSource() implements DeltaOperation {}

  record Close() implements DeltaOperation {}

  record BsPatch(long offset, long size) implements DeltaOperation {}

  static DeltaOperation readFrom(ByteBuffer byteBuffer, ObjectType objectType) {
    byte opcode = byteBuffer.get();
    return switch (DeltaOperationType.valueOf(opcode)) {
      case OPEN_SPLICE_AND_CLOSE -> {
        if (objectType == ObjectType.FILE || objectType == ObjectType.PAYLOAD_LINK) {
          long modeOffset = readVarint64(byteBuffer);
          long xattrOffset = readVarint64(byteBuffer);
          long size = readVarint64(byteBuffer);
          long offset = readVarint64(byteBuffer);
          yield new OpenSpliceAndCloseReal(offset, size, modeOffset, xattrOffset);
        } else {
          long size = readVarint64(byteBuffer);
          long offset = readVarint64(byteBuffer);
          yield new OpenSpliceAndCloseMeta(offset, size);
        }
      }
      case OPEN -> {
        long modeOffset = readVarint64(byteBuffer);
        long xattrOffset = readVarint64(byteBuffer);
        long size = readVarint64(byteBuffer);
        yield new Open(size, modeOffset, xattrOffset);
      }
      case WRITE -> {
        long size = readVarint64(byteBuffer);
        long offset = readVarint64(byteBuffer);
        yield new Write(offset, size);
      }
      case SET_READ_SOURCE -> {
        long offset = readVarint64(byteBuffer);
        yield new SetReadSource(offset);
      }
      case UNSET_READ_SOURCE -> new UnsetReadSource();
      case CLOSE -> new Close();
      case BSPATCH -> {
        long offset = readVarint64(byteBuffer);
        long size = readVarint64(byteBuffer);
        yield new BsPatch(offset, size);
      }
    };
  }

  /**
   * Reads a Protobuf varint from a byte buffer.
   *
   * <p>Varint64 encoding is little-endian and works by using the lower 7 bits of each byte as the
   * payload and the 0x80 bit as an indicator of whether the varint continues.
   */
  private static long readVarint64(ByteBuffer byteBuffer) {
    long acc = 0L;

    for (int i = 0; i < 10; ++i) {
      long b = byteBuffer.get();
      acc |= (b & 0x7F) << (i * 7);
      if ((b & 0x80) == 0) {
        break;
      }
    }

    return acc;
  }
}
