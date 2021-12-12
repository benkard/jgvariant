package eu.mulk.jgvariant.core;

import java.util.List;
import java.util.Optional;

/** A value representable by the GVariant serialization format. */
public sealed interface Value {

  // Composite types
  record Array<T extends Value>(List<T> values) implements Value {}

  record Maybe<T extends Value>(Optional<T> value) implements Value {}

  record Structure<T extends Record>(T values) implements Value {}

  record Variant(Class<? extends Value> type, Value value) implements Value {}

  // Primitive types
  record Bool(boolean value) implements Value {
    static Bool TRUE = new Bool(true);
    static Bool FALSE = new Bool(false);
  }

  record Int8(byte value) implements Value {}

  record Int16(short value) implements Value {}

  record Int32(int value) implements Value {}

  record Int64(long value) implements Value {}

  record Float64(double value) implements Value {}

  record Str(String value) implements Value {}
}
