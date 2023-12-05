// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool.jsonb;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import java.util.HexFormat;

@SuppressWarnings("java:S6548")
public final class ByteArraySerializer implements JsonbSerializer<byte[]> {

  public static final ByteArraySerializer INSTANCE = new ByteArraySerializer();

  private ByteArraySerializer() {}

  @Override
  public void serialize(
      byte[] o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
    jsonGenerator.write(HexFormat.of().formatHex(o));
  }
}
