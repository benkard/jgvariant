// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package eu.mulk.jgvariant.core;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A dynamically typed GVariant value carrying a {@link Signature} describing its type.
 *
 * <p>{@link #value()} can be of one of the following types:
 *
 * <ul>
 *   <li>{@link Boolean}
 *   <li>{@link Byte}
 *   <li>{@link Short}
 *   <li>{@link Integer}
 *   <li>{@link Long}
 *   <li>{@link String}
 *   <li>{@link java.util.Optional} (a GVariant {@code Maybe} type)
 *   <li>{@link java.util.List} (a GVariant array)
 *   <li>{@code Object[]} (a GVariant structure)
 *   <li>{@link java.util.Map} (a dictionary)
 *   <li>{@link java.util.Map.Entry} (a dictionary entry)
 *   <li>{@link Variant} (a nested variant)
 * </ul>
 *
 * @param signature the signature describing the type of the value.
 * @param value the value itself; one of {@link Boolean}, {@link Byte}, {@link Short}, {@link
 *     Integer}, {@link Long}, {@link String}, {@link java.util.Optional}, {@link java.util.List},
 *     {@code Object[]}, {@link Variant}.
 */
@API(status = Status.EXPERIMENTAL)
public record Variant(Signature signature, Object value) {}
