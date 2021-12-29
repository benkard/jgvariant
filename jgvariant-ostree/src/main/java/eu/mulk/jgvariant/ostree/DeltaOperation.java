package eu.mulk.jgvariant.ostree;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/** An operation in a static delta. */
@API(status = STABLE)
public enum DeltaOperation {
  OPEN_SPLICE_AND_CLOSE((byte) 'S'),
  OPEN((byte) 'o'),
  WRITE((byte) 'w'),
  SET_READ_SOURCE((byte) 'r'),
  UNSET_READ_SOURCE((byte) 'R'),
  CLOSE((byte) 'c'),
  BSPATCH((byte) 'B');

  private final byte byteValue;

  DeltaOperation(byte byteValue) {
    this.byteValue = byteValue;
  }

  /**
   * The serialized byte value.
   *
   * @return a serialized byte value for use in GVariant structures.
   */
  public byte byteValue() {
    return byteValue;
  }

  /**
   * Returns the {@link DeltaOperation} corresponding to a serialized GVariant value.
   *
   * @param byteValue a serialized value as used in GVariant.
   * @return the {@link DeltaOperation} corresponding to the serialized value.
   * @throws IllegalArgumentException if the byte value is invalid.
   */
  public static DeltaOperation valueOf(byte byteValue) {
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
