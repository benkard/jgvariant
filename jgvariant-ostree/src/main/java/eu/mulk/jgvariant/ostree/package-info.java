/**
 * Provides record classes describing the elements of <a
 * href="https://ostreedev.github.io/ostree/">OSTree</a> repositories and factory methods to create
 * {@link eu.mulk.jgvariant.core.Decoder} instances for them.
 *
 * <h2>OStree repository structure</h2>
 *
 * <p>An OSTree repository has the following layout:
 *
 * <table>
 *   <caption>OSTree repository layout</caption>
 *
 *   <tr>
 *     <td>{@code config}</td>
 *     <td>
 *       <p>
 *         A plain text file that contains various settings.  Among other things, this defines
 *         the <a href="https://ostreedev.github.io/ostree/formats/#the-archive-format">archive
 *         format</a> of the repository and whether files are compressed ({@code mode=archive-z2})
 *         or plain ({@code mode=bare}, {@code mode=bare-user}).
 *       </p>
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@code summary}</td>
 *     <td>
 *       <p>
 *         A {@link eu.mulk.jgvariant.ostree.Summary} object containing information on what is
 *         available under {@code refs/}, {@code deltas/}, and {@code delta-indexes/}.
 *       </p>
 *
 *       <p>This file may or may not exist and, if it exists, may or may not be up to date.</p>
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@code refs/heads/{name...}}</td>
 *     <td>Plain-text files containing {@link eu.mulk.jgvariant.ostree.Checksum}s in hex format
 *     (see {@link eu.mulk.jgvariant.ostree.Checksum#ofHex}) referring to
 *     {@link eu.mulk.jgvariant.ostree.Commit} objects.  See below for the layout of the
 *     {@code objects/} directory that this refers to.</td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@code objects/{checksumHead}/{checksumRest}.{fileExtension}}</td>
 *     <td>
 *       <p>
 *         Objects of various types as described by {@link eu.mulk.jgvariant.ostree.ObjectType}
 *         and keyed by {@link eu.mulk.jgvariant.ostree.Checksum}.
 *       </p>
 *
 *       <p>
 *         Among other things, this is where you find {@link eu.mulk.jgvariant.ostree.Commit}
 *         ({@code .commit)}, {@link eu.mulk.jgvariant.ostree.DirTree} ({@code .dirtree}),
 *         and {@link eu.mulk.jgvariant.ostree.DirMeta} ({@code .dirmeta}) objects as well as
 *         plain ({@code .file}) or compressed files ({@code .filez}).
 *       </p>
 *
 *       <p>
 *         Static delta information is not stored here, but in the {@code deltas/} and
 *         {@code delta-indexes/} directories (if available).
 *       </p>
 *
 *       <p>
 *         The individual parts of the file path are defined as follows:
 *       </p>
 *
 *       <dl>
 *         <dt>{@code {checksumHead}}
 *         <dd>
 *           the first two characters of {@link eu.mulk.jgvariant.ostree.Checksum#hex()}
 *         <dt>{@code {checksumRest}}
 *         <dd>
 *           the substring of {@link eu.mulk.jgvariant.ostree.Checksum#hex()} starting from the
 *           3rd character
 *         <dt>{@code {fileExtension}}
 *         <dd>
 *           the {@link eu.mulk.jgvariant.ostree.ObjectType#fileExtension()} of the object type
 *       </dl>
 *     </td>
 *   </tr>
 *
 *   <tr id="delta-superblock">
 *     <td>{@code deltas/{targetChecksumMbase64Head}/{targetChecksumMbase64Rest}/superblock}</td>
 *     <td>
 *       <p>
 *         A {@link eu.mulk.jgvariant.ostree.DeltaSuperblock} to get from nothing (an empty commit)
 *         to the commit named by the checksum encoded in the path.
 *       </p>
 *
 *       <p>
 *         The individual parts of the file path are defined as follows:
 *       </p>
 *
 *       <dl>
 *         <dt>{@code {checksumHead}}
 *         <dd>
 *           the first two characters of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()}
 *         <dt>{@code {checksumRest}}
 *         <dd>
 *           the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} starting
 *           from the 3rd character
 *       </dl>
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@code deltas/{targetChecksumMbase64Head}/{targetChecksumMbase64Rest}/{digit...}}</td>
 *     <td>
 *       <p>
 *         A {@link eu.mulk.jgvariant.ostree.DeltaPartPayload} belonging to a delta that goes from
 *         nothing (an empty commit) to the commit named by the checksum encoded in the path.
 *       </p>
 *
 *       <p>
 *         The individual parts of the file path are defined as follows:
 *       </p>
 *
 *       <dl>
 *         <dt>{@code {checksumHead}}
 *         <dd>
 *           the first two characters of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()}
 *         <dt>{@code {checksumRest}}
 *         <dd>
 *           the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} starting
 *           from the 3rd character
 *       </dl>
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@code deltas/{sourceChecksumMbase64Head}/{sourceChecksumMbase64Rest}-{targetChecksumMbase64}/superblock}</td>
 *     <td>
 *       <p>
 *         A {@link eu.mulk.jgvariant.ostree.DeltaSuperblock} to get from the source commit
 *         referenced by the first checksum encoded in the path to the target commit referenced
 *         in the second checksum encoded in the path.
 *       </p>
 *
 *       <p>
 *         The individual parts of the file path are defined as follows:
 *       </p>
 *
 *       <dl>
 *         <dt>{@code {checksumHead}}
 *         <dd>
 *           the first two characters of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()}
 *         <dt>{@code {checksumRest}}
 *         <dd>
 *           the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} starting
 *           from the 3rd character
 *       </dl>
 *     </td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@code deltas/{sourceChecksumMbase64Head}/{sourceChecksumMbase64Rest}-{targetChecksumMbase64}/{digit...}}</td>
 *     <td>
 *       <p>
 *         A {@link eu.mulk.jgvariant.ostree.DeltaPartPayload} belonging to a delta that goes from
 *         the source commit referenced by the first checksum encoded in the path to the target
 *         commit referenced in the second checksum encoded in the path.
 *       </p>
 *
 *       <p>
 *         The individual parts of the file path are defined as follows:
 *       </p>
 *
 *       <dl>
 *         <dt>{@code {checksumHead}}
 *         <dd>
 *           the first two characters of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()}
 *         <dt>{@code {checksumRest}}
 *         <dd>
 *           the substring of {@link eu.mulk.jgvariant.ostree.Checksum#modifiedBase64()} starting
 *           from the 3rd character
 *       </dl>
 *     </td>
 *   </tr>
 * </table>
 */
@API(status = Status.EXPERIMENTAL)
package eu.mulk.jgvariant.ostree;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
