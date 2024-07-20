// SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * Provides record classes describing the elements of <a
 * href="https://ostreedev.github.io/ostree/">OSTree</a> repositories and factory methods to create
 * {@link eu.mulk.jgvariant.core.Decoder} instances for them.
 *
 * <h2>OStree repository structure</h2>
 *
 * <p>An OSTree repository has the following layout:
 *
 * <dl>
 *   <dt>{@code config}
 *   <dd>
 *       <p>A plain text file that contains various settings. Among other things, this defines the
 *       <a href="https://ostreedev.github.io/ostree/formats/#the-archive-format">archive format</a>
 *       of the repository and whether files are compressed ({@code mode=archive-z2}) or plain
 *       ({@code mode=bare}, {@code mode=bare-user}).
 *   <dt>{@code summary}
 *   <dd>
 *       <p>A {@link eu.mulk.jgvariant.ostree.Summary} object containing information on what is
 *       available under {@code refs/}, {@code deltas/}, and {@code delta-indexes/}.
 *       <p>This file may or may not exist and, if it exists, may or may not be up to date.
 *   <dt>{@code refs/heads{/name...}}
 *   <dd>
 *       <p>Plain-text files containing {@link eu.mulk.jgvariant.ostree.Checksum}s in hex format
 *       (see {@link eu.mulk.jgvariant.ostree.Checksum#ofHex}) referring to {@link
 *       eu.mulk.jgvariant.ostree.Commit} objects. See below for the layout of the {@code objects/}
 *       directory that this refers to.
 *   <dt>{@code objects/{ξ₀ξ₁}/{ξ₂ξ₃ξ₄ξ₅...}.{ext}}
 *   <dd>
 *       <p>Objects of various types as described by {@link eu.mulk.jgvariant.ostree.ObjectType} and
 *       keyed by {@link eu.mulk.jgvariant.ostree.Checksum}.
 *       <p>Among other things, this is where you find {@link eu.mulk.jgvariant.ostree.Commit}
 *       ({@code .commit)}, {@link eu.mulk.jgvariant.ostree.DirTree} ({@code .dirtree}), and {@link
 *       eu.mulk.jgvariant.ostree.DirMeta} ({@code .dirmeta}) objects as well as plain ({@code
 *       .file}) or compressed files ({@code .filez}).
 *       <p>Static delta information is not stored here, but in the {@code deltas/} and {@code
 *       delta-indexes/} directories (if available).
 *       <p>The individual parts of the file path are defined as follows:
 *       <dl>
 *         <dt>{@code {ξ₀ξ₁}}
 *         <dd>the first two characters of {@link eu.mulk.jgvariant.ostree.Checksum#hex()}
 *         <dt>{@code {ξ₂ξ₃ξ₄ξ₅...}}
 *         <dd>the substring of {@link eu.mulk.jgvariant.ostree.Checksum#hex()} starting from the
 *             3rd character
 *         <dt>{@code {ext}}
 *         <dd>the {@link eu.mulk.jgvariant.ostree.ObjectType#fileExtension()} of the object type
 *       </dl>
 *   <dt id="delta-superblock">{@code deltas/{ν₀ν₁}/{ν₂ν₃ν₄ν₅...}/superblock}
 *   <dd>
 *       <p>A {@link eu.mulk.jgvariant.ostree.DeltaSuperblock} to get from nothing (an empty commit)
 *       to the commit named by the checksum encoded in the path.
 *       <p>The individual parts of the file path are defined as follows:
 *       <dl>
 *         <dt>{@code {ν₀ν₁}}
 *         <dd>the first two characters of {@link
 *             eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the target commit the delta
 *             ends at
 *         <dt>{@code {ν₂ν₃ν₄ν₅...}}
 *         <dd>the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the
 *             target commit the delta ends at starting from the 3rd character
 *       </dl>
 *   <dt>{@code deltas/{ν₀ν₁}/{ν₂ν₃ν₄ν₅...}/{digit...}}
 *   <dd>
 *       <p>A {@link eu.mulk.jgvariant.ostree.DeltaPartPayload} belonging to a delta that goes from
 *       nothing (an empty commit) to the commit named by the checksum encoded in the path.
 *       <p>The individual parts of the file path are defined as follows:
 *       <dl>
 *         <dt>{@code {ν₀ν₁}}
 *         <dd>the first two characters of {@link
 *             eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the target commit the delta
 *             ends at
 *         <dt>{@code {ν₂ν₃ν₄ν₅...}}
 *         <dd>the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the
 *             target commit the delta ends at starting from the 3rd character
 *       </dl>
 *   <dt>{@code deltas/{μ₀μ₁}/{μ₂μ₃μ₄μ₅...}-{ν₀ν₁ν₂ν₃ν₄ν₅...}/superblock}
 *   <dd>
 *       <p>A {@link eu.mulk.jgvariant.ostree.DeltaSuperblock} to get from the source commit
 *       referenced by the first checksum encoded in the path to the target commit referenced in the
 *       second checksum encoded in the path.
 *       <p>The individual parts of the file path are defined as follows:
 *       <dl>
 *         <dt>{@code {μ₀μ₁}}
 *         <dd>the first two characters of {@link
 *             eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the source commit the delta
 *             starts from
 *         <dt>{@code {μ₂μ₃μ₄μ₅...}}
 *         <dd>the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the
 *             source commit the delta starts from starting from the 3rd character
 *         <dt>{@code {ν₀ν₁ν₂ν₃ν₄ν₅...}}
 *         <dd>the full {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the target
 *             commit the delta ends at
 *       </dl>
 *   <dt>{@code deltas/{μ₀μ₁}/{μ₂μ₃μ₄μ₅...}-{ν₀ν₁ν₂ν₃ν₄ν₅...}/{digit...}}
 *   <dd>
 *       <p>A {@link eu.mulk.jgvariant.ostree.DeltaPartPayload} belonging to a delta that goes from
 *       the source commit referenced by the first checksum encoded in the path to the target commit
 *       referenced in the second checksum encoded in the path.
 *       <p>The individual parts of the file path are defined as follows:
 *       <dl>
 *         <dt>{@code {μ₀μ₁}}
 *         <dd>the first two characters of {@link
 *             eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the source commit the delta
 *             starts from
 *         <dt>{@code {μ₂μ₃μ₄μ₅...}}
 *         <dd>the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the
 *             source commit the delta starts from starting from the 3rd character
 *         <dt>{@code {ν₀ν₁ν₂ν₃ν₄ν₅...}}
 *         <dd>the full {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} of the target
 *             commit the delta ends at
 *       </dl>
 * </dl>
 */
@API(status = Status.EXPERIMENTAL)
@NullMarked
package eu.mulk.jgvariant.ostree;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.NullMarked;
