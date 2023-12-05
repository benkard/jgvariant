// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool.jsonb;

import eu.mulk.jgvariant.core.Signature;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

@SuppressWarnings("java:S6548")
public final class SignatureSerializer implements JsonbSerializer<Signature> {

  public static final SignatureSerializer INSTANCE = new SignatureSerializer();

  private SignatureSerializer() {}

  @Override
  public void serialize(
      Signature o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
    jsonGenerator.write(o.toString());
  }
}
