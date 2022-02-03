// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

enum DeltaOperationType {
  OPEN_SPLICE_AND_CLOSE((byte) 'S'),
  OPEN((byte) 'o'),
  WRITE((byte) 'w'),
  SET_READ_SOURCE((byte) 'r'),
  UNSET_READ_SOURCE((byte) 'R'),
  CLOSE((byte) 'c'),
  BSPATCH((byte) 'B');

  private final byte byteValue;

  DeltaOperationType(byte byteValue) {
    this.byteValue = byteValue;
  }

  /**
   * The serialized byte value.
   *
   * @return a serialized byte value for use in GVariant structures.
   */
  byte byteValue() {
    return byteValue;
  }

  /**
   * Returns the {@link DeltaOperationType} corresponding to a serialized GVariant value.
   *
   * @param byteValue a serialized value as used in GVariant.
   * @return the {@link DeltaOperationType} corresponding to the serialized value.
   * @throws IllegalArgumentException if the byte value is invalid.
   */
  static DeltaOperationType valueOf(byte byteValue) {
    return switch (byteValue) {
      case (byte) 'S' -> OPEN_SPLICE_AND_CLOSE;
      case (byte) 'o' -> OPEN;
      case (byte) 'w' -> WRITE;
      case (byte) 'r' -> SET_READ_SOURCE;
      case (byte) 'R' -> UNSET_READ_SOURCE;
      case (byte) 'c' -> CLOSE;
      case (byte) 'B' -> BSPATCH;
      default -> throw new IllegalArgumentException(
          "invalid DeltaOperation: %d".formatted(byteValue));
    };
  }
}
