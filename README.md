# GVariant for Java

This library provides a [GVariant][] parser in pure Java.


## Overview

The foundational class is `Decoder`.

Instances of `Decoder` read a given concrete subtype of `Value` from a
[ByteBuffer][].  The class also contains factory methods to create
those instances.

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


[ByteBuffer]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/ByteBuffer.html
[GVariant]: https://docs.gtk.org/glib/struct.Variant.html
[GVariant serialization]: https://people.gnome.org/~desrt/gvariant-serialisation.pdf
[String]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html
