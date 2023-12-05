// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool.jsonb;

import eu.mulk.jgvariant.ostree.ByteString;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

@SuppressWarnings("java:S6548")
public final class ByteStringSerializer implements JsonbSerializer<ByteString> {

  public static final ByteStringSerializer INSTANCE = new ByteStringSerializer();

  private ByteStringSerializer() {}

  @Override
  public void serialize(
      ByteString o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
    jsonGenerator.write(o.hex());
  }
}
