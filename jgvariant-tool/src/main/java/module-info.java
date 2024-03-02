// SPDX-FileCopyrightText: © 2023 Matthias Andreas Benkard <code@mail.matthias.benkard.de>
//
// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The {@code jgvariant} command line tool.
 *
 * <p>The {@link eu.mulk.jgvariant.tool} module contains a tool called
 * {@code jgvariant} that can be used to manipulate GVariant-formatted
 * files from the command line.  Its primary purpose is to enable the
 * scripting of OSTree repository management tasks.
 *
 * <p>The {@link eu.mulk.jgvariant.tool.Main} class defines the entry point
 * of the tool.
 *
 * <p>Usage example (dumping the contents of an OSTree summary file):
 *
 * {@snippet lang="sh" :
 * $ jgvariant ostree summary read ./jgvariant-ostree/src/test/resources/ostree/summary
 * }
 *
 * <p>Output:
 *
 * {@snippet lang="json" :
 * {
 *     "entries": [
 *         {
 *             "ref": "mulkos/1.x/amd64",
 *             "value": {
 *                 "checksum": "66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350",
 *                 "metadata": {
 *                     "fields": {
 *                         "ostree.commit.timestamp": 1640537170
 *                     }
 *                 },
 *                 "size": 214
 *             }
 *         }
 *     ],
 *     "metadata": {
 *         "fields": {
 *             "ostree.summary.last-modified": 1640537300,
 *             "ostree.summary.tombstone-commits": false,
 *             "ostree.static-deltas": {
 *                 "3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f-66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350": "03738040e28e7662e9c9d2599c530ea974e642c9f87e6c00cbaa39a0cdac8d44",
 *                 "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52": "f481144629474bd88c106e45ac405ebd75b324b0655af1aec14b31786ae1fd61",
 *                 "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52-3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f": "2c6a07bc1cf4d7ce7d00f82d7d2d6d156fd0e31d476851b46dc2306b181b064a"
 *             },
 *             "ostree.summary.mode": "bare",
 *             "ostree.summary.indexed-deltas": true
 *         }
 *     }
 * }
 * }
 */
module eu.mulk.jgvariant.tool {
  requires transitive eu.mulk.jgvariant.ostree;
  requires info.picocli;
  requires jakarta.json;
  requires jakarta.json.bind;
  requires java.logging;
  requires static com.google.errorprone.annotations;
  requires static org.apiguardian.api;
  requires static org.jetbrains.annotations;

  opens eu.mulk.jgvariant.tool to
      info.picocli;

  exports eu.mulk.jgvariant.tool;
}
