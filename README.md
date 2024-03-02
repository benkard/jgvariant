<!--
SPDX-FileCopyrightText: © 2021 Matthias Andreas Benkard <code@mail.matthias.benkard.de>

SPDX-License-Identifier: GFDL-1.3-or-later
-->

# GVariant for Java

This library provides a [GVariant][] parser in pure Java.


## Overview

`jgvariant-core` provides `Decoder<T>`, which read a given type of
GVariant-encoded value from a [ByteBuffer][].  The class also contains
factory methods to acquire those instances.

The various subclasses of `Decoder` together implement the [GVariant
serialization][] specification.

`jgvariant-ostree` provides instances of `Decoder<T>` for various
[GVariant][] types used in [OSTree][] repositories.


## Example

To parse a [GVariant][] value of type `"a(si)"`, which is an array of
pairs of [String][] and `int`, you can use the following code:

    record ExampleRecord(String s, int i) {}
    
    var decoder =
      Decoder.ofArray(
        Decoder.ofStructure(
          ExampleRecord.class,
          Decoder.ofString(StandardCharsets.UTF_8),
          Decoder.ofInt().withByteOrder(ByteOrder.LITTLE_ENDIAN)));
    
    byte[] bytes = ...;
    List<ExampleRecord> example = decoder.decode(ByteBuffer.wrap(bytes));


## Command line tool

The `jgvariant-tool` module contains a tool called `jgvariant` that can
be used to manipulate [GVariant][]-formatted files from the command line.
Its primary purpose is to enable the scripting of [OSTree][] repository
management tasks.

### Usage examples

#### Dumping the contents of an [OSTree][] summary file

    $ jgvariant ostree summary read ./jgvariant-ostree/src/test/resources/ostree/summary

Output:

    {
        "entries": [
            {
                "ref": "mulkos/1.x/amd64",
                "value": {
                    "checksum": "66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350",
                    "metadata": {
                        "fields": {
                            "ostree.commit.timestamp": 1640537170
                        }
                    },
                    "size": 214
                }
            }
        ],
        "metadata": {
            "fields": {
                "ostree.summary.last-modified": 1640537300,
                "ostree.summary.tombstone-commits": false,
                "ostree.static-deltas": {
                    "3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f-66ff167ff35ce87daac817447a9490a262ee75f095f017716a6eb1a9d9eb3350": "03738040e28e7662e9c9d2599c530ea974e642c9f87e6c00cbaa39a0cdac8d44",
                    "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52": "f481144629474bd88c106e45ac405ebd75b324b0655af1aec14b31786ae1fd61",
                    "31c8835d5c9d2c6687a50091c85142d1b2d853ff416a9fb81b4ee30754510d52-3d3b3329dca38871f29aeda1bf5854d76c707fa269759a899d0985c91815fe6f": "2c6a07bc1cf4d7ce7d00f82d7d2d6d156fd0e31d476851b46dc2306b181b064a"
                },
                "ostree.summary.mode": "bare",
                "ostree.summary.indexed-deltas": true
            }
        }
    }

#### Adding a static delta to an [OSTree][] summary file

Static delta <code>3...</code> (in hex), between commits <code>1...</code> and <code>2...</code>:

    $ jgvariant ostree summary add-static-delta ./jgvariant-ostree/src/test/resources/ostree/summary 3333333333333333333333333333333333333333333333333333333333333333 2222222222222222222222222222222222222222222222222222222222222222 1111111111111111111111111111111111111111111111111111111111111111

Static delta <code>3...</code> (in hex), between the empty commit and <code>2...</code>:

    $ jgvariant ostree summary add-static-delta ./jgvariant-ostree/src/test/resources/ostree/summary 4444444444444444444444444444444444444444444444444444444444444444 2222222222222222222222222222222222222222222222222222222222222222

### Building the tool

You can build the tool either as a shaded JAR or as a native executable.

To build and run a shaded JAR:

    $ mvn package -pl jgvariant-tool -am -Pshade
    $ java -jar jgvariant-tool/target/jgvariant-tool-*.jar ...

To build and run a native executable:

    $ mvn package -pl jgvariant-tool -am -Pnative
    $ ./jgvariant-tool/target/jgvariant ...

You can also run the tool directly with Maven using the `exec` profile:

    $ mvn verify -pl jgvariant-tool -am -Pexec -Dexec.args="..."

## Library installation

### Usage with Maven

    <project>
      ...
    
      <dependencyManagement>
        ...
    
        <dependencies>
          <dependency>
            <groupId>eu.mulk.jgvariant</groupId>
            <artifactId>jgvariant-bom</artifactId>
            <version>0.1.8</version>
            <type>pom</type>
            <scope>import</scope>
          </dependency>
        </dependencies>
    
        ...
      </dependencyManagement>
    
      <dependencies>
        ...
    
        <dependency>
          <groupId>eu.mulk.jgvariant</groupId>
          <artifactId>jgvariant-core</artifactId>
        </dependency>
        <dependency>
          <groupId>eu.mulk.jgvariant</groupId>
          <artifactId>jgvariant-ostree</artifactId>
        </dependency>
    
        ...
      </dependencies>
    
      ...
    </project>


### Usage with Gradle

    dependencies {
      ...
    
      implementation(platform("eu.mulk.jgvariant:jgvariant-bom:0.1.8")
      implementation("eu.mulk.jgvariant:jgvariant-core")
      implementation("eu.mulk.jgvariant:jgvariant-ostree")
    
      ...
    }


[ByteBuffer]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/ByteBuffer.html
[GVariant]: https://docs.gtk.org/glib/struct.Variant.html
[GVariant serialization]: https://people.gnome.org/~desrt/gvariant-serialisation.pdf
[OSTree]: https://ostreedev.github.io/ostree/
[String]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html
