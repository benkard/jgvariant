// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.ostree;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * An object type as found in an OSTree repository.
 *
 * <p>Each object type has its own file extension.
 *
 * <p>In an OSTree repository, objects are located in a subfolder of the {@code /objects} folder
 * based on their {@link Checksum}. The schema for looking up objects is {@code
 * /objects/{checksumHead}/{checksumRest}.{fileExtension}} where:
 *
 * <dl>
 *   <dt>{@code {checksumHead}}
 *   <dd>the first two characters of {@link Checksum#hex()}
 *   <dt>{@code {checksumRest}}
 *   <dd>the substring of {@link Checksum#hex()} starting from the 3rd character
 *   <dt>{@code {fileExtension}}
 *   <dd>the {@link #fileExtension()} of the object type
 * </dl>
 */
@API(status = STABLE)
public enum ObjectType {

  /**
   * A regular file.
   *
   * <p>File ending: {@code .file}
   */
  FILE((byte) 1, "file"),

  /**
   * A serialized {@link DirTree} object.
   *
   * <p>File ending: {@code .dirtree}
   */
  DIR_TREE((byte) 2, "dirtree"),

  /**
   * A serialized {@link DirMeta} object.
   *
   * <p>File ending: {@code .dirmeta}
   */
  DIR_META((byte) 3, "dirmeta"),

  /**
   * A serialized {@link Commit} object.
   *
   * <p>File ending: {@code .commit}
   */
  COMMIT((byte) 4, "commit"),

  /**
   * A tombstone file standing in for a commit that was deleted.
   *
   * <p>File ending: {@code .commit-tombstone}
   */
  TOMBSTONE_COMMIT((byte) 5, "commit-tombstone"),

  /**
   * Detached metadata for a {@link Commit}.
   *
   * <p>Often goes together with a {@link #TOMBSTONE_COMMIT}.
   *
   * <p>File ending: {@code .commitmeta}
   */
  COMMIT_META((byte) 6, "commitmeta"),

  /**
   * A symlink to a {@link #FILE} that lives somewhere else.
   *
   * <p>File ending: {@code .payload-link}
   */
  PAYLOAD_LINK((byte) 7, "payload-link");

  private final byte byteValue;
  private final String fileExtension;

  /**
   * The serialized byte value.
   *
   * @return a byte representing this value in serialized GVariant structures.
   */
  public byte byteValue() {
    return byteValue;
  }

  /**
   * The file extension carried by files of this type.
   *
   * @return a file extension.
   */
  public String fileExtension() {
    return fileExtension;
  }

  ObjectType(byte byteValue, String fileExtension) {
    this.byteValue = byteValue;
    this.fileExtension = fileExtension;
  }

  /**
   * Returns the {@link ObjectType} corresponding to a serialized GVariant value.
   *
   * @param byteValue a serialized value as used in GVariant.
   * @return the {@link ObjectType} corresponding to the serialized value.
   * @throws IllegalArgumentException if the byte value is invalid.
   */
  public static ObjectType valueOf(byte byteValue) {
    return switch (byteValue) {
      case 1 -> FILE;
      case 2 -> DIR_TREE;
      case 3 -> DIR_META;
      case 4 -> COMMIT;
      case 5 -> TOMBSTONE_COMMIT;
      case 6 -> COMMIT_META;
      case 7 -> PAYLOAD_LINK;
      default -> throw new IllegalArgumentException("invalid ObjectType: %d".formatted(byteValue));
    };
  }
}
