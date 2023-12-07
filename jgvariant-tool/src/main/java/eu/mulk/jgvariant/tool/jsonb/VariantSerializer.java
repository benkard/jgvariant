// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool.jsonb;

import eu.mulk.jgvariant.core.Signature;
import eu.mulk.jgvariant.core.Variant;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("java:S6548")
public final class VariantSerializer implements JsonbSerializer<Variant> {

  public static final VariantSerializer INSTANCE = new VariantSerializer();

  private final ByteArraySerializer byteArraySerializer = ByteArraySerializer.INSTANCE;

  private final Signature byteArraySignature;

  private VariantSerializer() {
    try {
      byteArraySignature = Signature.parse("ay");
    } catch (ParseException e) {
      // impossible
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void serialize(Variant obj, JsonGenerator generator, SerializationContext ctx) {
    if (obj.signature().equals(byteArraySignature)) {
      byteArraySerializer.serialize(byteArrayOf((List<Byte>) obj.value()), generator, ctx);
    } else {
      ctx.serialize(obj.value(), generator);
    }
  }

  private static byte[] byteArrayOf(Collection<Byte> bytes) {
    byte[] result = new byte[bytes.size()];
    int i = 0;
    for (byte b : bytes) {
      result[i++] = b;
    }
    return result;
  }
}
