// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package eu.mulk.jgvariant.tool.jsonb;

import eu.mulk.jgvariant.ostree.ByteString;
import eu.mulk.jgvariant.ostree.Checksum;
import jakarta.json.bind.adapter.JsonbAdapter;

@SuppressWarnings("java:S6548")
public final class ChecksumAdapter implements JsonbAdapter<Checksum, ByteString> {

  public static final ChecksumAdapter INSTANCE = new ChecksumAdapter();

  private ChecksumAdapter() {}

  @Override
  public ByteString adaptToJson(Checksum obj) throws Exception {
    return obj.byteString();
  }

  @Override
  public Checksum adaptFromJson(ByteString obj) throws Exception {
    return new Checksum(obj);
  }
}
