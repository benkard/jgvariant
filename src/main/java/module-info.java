import eu.mulk.jgvariant.core.Variant;

/**
 * Provides a parser for the <a href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a>
 * serialization format.
 *
 * <p>The {@link eu.mulk.jgvariant.core} package contains the {@link Variant} and {@link
 * eu.mulk.jgvariant.core.Decoder} types. which form the foundation of this library.
 */
module eu.mulk.jgvariant.core {
  requires com.google.errorprone.annotations;
  requires org.jetbrains.annotations;

  exports eu.mulk.jgvariant.core;
}
