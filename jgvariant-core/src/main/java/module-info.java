/**
 * Provides a parser for the <a href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a>
 * serialization format.
 *
 * <ul>
 *   <li><a href="#sect-overview">Overview</a>
 *   <li><a href="#sect-installation">Installation</a>
 * </ul>
 *
 * <h2 id="sect-overview">Overview</h2>
 *
 * <p>The {@link eu.mulk.jgvariant.core} package contains the {@link eu.mulk.jgvariant.core.Decoder}
 * type, which contains classes to parse and represent serialized <a
 * href="https://docs.gtk.org/glib/struct.Variant.html">GVariant</a> values.
 *
 * <h2 id="sect-installation">Installation</h2>
 *
 * <ul>
 *   <li><a href="#sect-installation-maven">Usage with Maven</a>
 *   <li><a href="#sect-installation-gradle">Usage with Gradle</a>
 * </ul>
 *
 * <h3 id="sect-installation-maven">Usage with Maven</h3>
 *
 * <pre>{@code
 * <project>
 *   ...
 *
 *   <dependencyManagement>
 *     ...
 *
 *     <dependencies>
 *       <dependency>
 *         <groupId>eu.mulk.jgvariant</groupId>
 *         <artifactId>jgvariant-bom</artifactId>
 *         <version>0.1.4</version>
 *         <type>pom</type>
 *         <scope>import</scope>
 *       </dependency>
 *     </dependencies>
 *
 *     ...
 *   </dependencyManagement>
 *
 *   <dependencies>
 *     ...
 *
 *     <dependency>
 *       <groupId>eu.mulk.jgvariant</groupId>
 *       <artifactId>jgvariant-core</artifactId>
 *     </dependency>
 *     <dependency>
 *       <groupId>eu.mulk.jgvariant</groupId>
 *       <artifactId>jgvariant-ostree</artifactId>
 *     </dependency>
 *
 *     ...
 *   </dependencies>
 *
 *   ...
 * </project>
 * }</pre>
 *
 * <h3 id="sect-installation-gradle">Usage with Gradle</h3>
 *
 * <pre>{@code
 * dependencies {
 *   ...
 *
 *   implementation(platform("eu.mulk.jgvariant:jgvariant-bom:0.1.4")
 *   implementation("eu.mulk.jgvariant:jgvariant-core")
 *   implementation("eu.mulk.jgvariant:jgvariant-ostree")
 *
 *   ...
 * }
 * }</pre>
 */
module eu.mulk.jgvariant.core {
  requires com.google.errorprone.annotations;
  requires org.jetbrains.annotations;
  requires org.apiguardian.api;

  exports eu.mulk.jgvariant.core;
}
