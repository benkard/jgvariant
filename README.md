# GVariant for Java

This library provides a [GVariant][] parser in pure Java.


## Overview

Instances of `Decoder<T>` read a given type of GVariant-encoded value
from a [ByteBuffer][].  The class also contains factory methods to
acquire those instances.

The various subclasses of `Decoder` together implement the [GVariant
serialization][] specification.


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


## Installation

### Usage with Maven

    <project>
      ...
    
      <dependencyManagement>
        ...
    
        <dependencies>
          <dependency>
            <groupId>eu.mulk.jgvariant</groupId>
            <artifactId>jgvariant-bom</artifactId>
            <version>0.1.4</version>
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
    
      implementation(platform("eu.mulk.jgvariant:jgvariant-bom:0.1.4")
      implementation("eu.mulk.jgvariant:jgvariant-core")
      implementation("eu.mulk.jgvariant:jgvariant-ostree")
    
      ...
    }


[ByteBuffer]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/ByteBuffer.html
[GVariant]: https://docs.gtk.org/glib/struct.Variant.html
[GVariant serialization]: https://people.gnome.org/~desrt/gvariant-serialisation.pdf
[String]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html
