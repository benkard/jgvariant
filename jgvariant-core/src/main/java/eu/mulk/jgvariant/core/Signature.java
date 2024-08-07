// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.core;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * A GVariant signature string.
 *
 * <p>Describes a type in the GVariant type system. The type can be arbitrarily complex.
 *
 * <p><strong>Examples</strong>
 *
 * <dl>
 *   <dt>{@code "i"}
 *   <dd>a single 32-bit integer
 *   <dt>{@code "ai"}
 *   <dd>an array of 32-bit integers
 *   <dt>{@code "(bbb(sai))"}
 *   <dd>a record consisting of three booleans and a nested record, which consists of a string and
 *       an array of 32-bit integers
 * </dl>
 */
@API(status = Status.STABLE)
public final class Signature {

  private final String signatureString;
  private final Decoder<?> decoder;

  Signature(ByteBuffer signatureBytes) throws ParseException {
    this.decoder = parseSignature(signatureBytes);

    signatureBytes.rewind();
    this.signatureString = US_ASCII.decode(signatureBytes).toString();
  }

  static Signature parse(ByteBuffer signatureBytes) throws ParseException {
    return new Signature(signatureBytes);
  }

  public static Signature parse(String signatureString) throws ParseException {
    var signatureBytes = ByteBuffer.wrap(signatureString.getBytes(US_ASCII));
    return parse(signatureBytes);
  }

  /**
   * Returns a {@link Decoder} that can decode values conforming to this signature.
   *
   * @return a {@link Decoder} for this signature
   */
  @SuppressWarnings("unchecked")
  Decoder<Object> decoder() {
    return (Decoder<Object>) decoder;
  }

  /**
   * Returns the signature formatted as a GVariant signature string.
   *
   * @return a GVariant signature string.
   */
  @Override
  public String toString() {
    return signatureString;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return (o instanceof Signature signature)
        && Objects.equals(signatureString, signature.signatureString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(signatureString);
  }

  private static Decoder<?> parseSignature(ByteBuffer signature) throws ParseException {
    char c = (char) signature.get();
    return switch (c) {
      case 'b' -> Decoder.ofBoolean();
      case 'y' -> Decoder.ofByte();
      case 'n', 'q' -> Decoder.ofShort();
      case 'i', 'u' -> Decoder.ofInt();
      case 'x', 't' -> Decoder.ofLong();
      case 'd' -> Decoder.ofDouble();
      case 's', 'o', 'g' -> Decoder.ofString(UTF_8);
      case 'v' -> Decoder.ofVariant();
      case 'm' -> Decoder.ofMaybe(parseSignature(signature));
      case '(' -> Decoder.ofStructure(parseTupleTypes(signature).toArray(new Decoder<?>[0]));
      case 'a' -> {
        char elementC = (char) signature.get(signature.position());
        if (elementC == '{') {
          signature.get();
          List<Decoder<?>> entryTypes = parseDictionaryEntryTypes(signature);
          yield Decoder.ofDictionary(entryTypes.get(0), entryTypes.get(1));
        } else {
          yield Decoder.ofArray(parseSignature(signature));
        }
      }
      case '{' -> {
        List<Decoder<?>> entryTypes = parseDictionaryEntryTypes(signature);
        yield Decoder.ofDictionaryEntry(entryTypes.get(0), entryTypes.get(1));
      }
      default ->
          throw new ParseException(
              String.format("encountered unknown signature byte '%c'", c), signature.position());
    };
  }

  private static List<Decoder<?>> parseDictionaryEntryTypes(ByteBuffer signature)
      throws ParseException {
    var tupleTypes = parseTupleTypes(signature);
    if (tupleTypes.size() != 2) {
      throw new ParseException(
          String.format("dictionary entry type with %d components, expected 2", tupleTypes.size()),
          signature.position());
    }
    return tupleTypes;
  }

  private static List<Decoder<?>> parseTupleTypes(ByteBuffer signature) throws ParseException {
    List<Decoder<?>> decoders = new ArrayList<>();

    while (true) {
      char c = (char) signature.get(signature.position());
      if (c == ')' || c == '}') {
        signature.get();
        break;
      }

      decoders.add(parseSignature(signature));
    }

    return decoders;
  }
}
