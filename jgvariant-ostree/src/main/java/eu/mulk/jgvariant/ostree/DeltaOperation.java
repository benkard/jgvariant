// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import java.io.ByteArrayOutputStream;
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

  default void writeTo(ByteArrayOutputStream output) {
    if (this instanceof OpenSpliceAndCloseReal openSpliceAndCloseReal) {
      output.write(DeltaOperationType.OPEN_SPLICE_AND_CLOSE.byteValue());
      writeVarint64(output, openSpliceAndCloseReal.modeOffset);
      writeVarint64(output, openSpliceAndCloseReal.xattrOffset);
      writeVarint64(output, openSpliceAndCloseReal.size);
      writeVarint64(output, openSpliceAndCloseReal.offset);
    } else if (this instanceof OpenSpliceAndCloseMeta openSpliceAndCloseMeta) {
      output.write(DeltaOperationType.OPEN_SPLICE_AND_CLOSE.byteValue());
      writeVarint64(output, openSpliceAndCloseMeta.size);
      writeVarint64(output, openSpliceAndCloseMeta.offset);
    } else if (this instanceof Open open) {
      output.write(DeltaOperationType.OPEN.byteValue());
      writeVarint64(output, open.modeOffset);
      writeVarint64(output, open.xattrOffset);
      writeVarint64(output, open.size);
    } else if (this instanceof Write write) {
      output.write(DeltaOperationType.WRITE.byteValue());
      writeVarint64(output, write.size);
      writeVarint64(output, write.offset);
    } else if (this instanceof SetReadSource setReadSource) {
      output.write(DeltaOperationType.SET_READ_SOURCE.byteValue());
      writeVarint64(output, setReadSource.offset);
    } else if (this instanceof UnsetReadSource) {
      output.write(DeltaOperationType.UNSET_READ_SOURCE.byteValue());
    } else if (this instanceof Close) {
      output.write(DeltaOperationType.CLOSE.byteValue());
    } else if (this instanceof BsPatch bsPatch) {
      output.write(DeltaOperationType.BSPATCH.byteValue());
      writeVarint64(output, bsPatch.offset);
      writeVarint64(output, bsPatch.size);
    } else {
      throw new IllegalStateException("unrecognized delta operation: %s".formatted(this));
    }
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

  /**
   * Writes a Protobuf varint to an output stream.
   *
   * @see #readVarint64
   */
  private static void writeVarint64(ByteArrayOutputStream output, long value) {
    int n = 0;
    do {
      byte b = (byte) (value & 0x7F);
      value >>= 7;
      if (value != 0) {
        b |= (byte) 0x80;
      }
      output.write(b);
      ++n;
    } while (value != 0 && n < 10);
  }
}
