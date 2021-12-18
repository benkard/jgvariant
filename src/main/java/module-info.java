/**
 * Provides a parser for the <a href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a>
 * serialization format.
 *
 * <p>The {@link eu.mulk.jgvariant.core} package contains the {@link eu.mulk.jgvariant.core.Decoder}
 * type, which forms the foundation of this library.
 */
module eu.mulk.jgvariant.core {
  requires com.google.errorprone.annotations;
  requires org.jetbrains.annotations;
  requires org.apiguardian.api;

  exports eu.mulk.jgvariant.core;
}
