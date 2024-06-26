/**
 * MIT License
 *
 * Copyright (c) 2024 Joe Roskopf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.autonomousapps.kit.gradle

public class GradleProperties(private val lines: MutableList<String>) {
    public operator fun plus(other: CharSequence): GradleProperties {
        return GradleProperties(
            (lines + other).mutDistinct(),
        )
    }

    public operator fun plus(other: Iterable<CharSequence>): GradleProperties {
        return GradleProperties(
            (lines + other).mutDistinct(),
        )
    }

    public operator fun plus(other: GradleProperties): GradleProperties {
        return GradleProperties(
            (lines + other.lines).mutDistinct(),
        )
    }

    private fun <T> Iterable<T>.mutDistinct(): MutableList<String> {
        return toMutableSet().map { it.toString() }.toMutableList()
    }

    public companion object {
        public val JVM_ARGS: String =
            """
            # Try to prevent OOMs (Metaspace) in test daemons spawned by testkit tests
            org.gradle.jvmargs=-Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError -XX:MaxMetaspaceSize=1024m
            """.trimIndent()

        public val USE_ANDROID_X: String =
            """
            # Necessary for AGP 3.6+
            android.useAndroidX=true
            """.trimIndent()

        public const val NON_TRANSITIVE_R: String = "android.nonTransitiveRClass=true"

        /** Enable the configuration cache, pre-Gradle 8. */
        public const val CONFIGURATION_CACHE_UNSTABLE: String = "org.gradle.unsafe.configuration-cache=true"

        /** Enable the configuration cache, Gradle 8+. */
        public const val CONFIGURATION_CACHE_STABLE: String = "org.gradle.configuration-cache=true"

        /**
         * Enable isolated projects, pre-Gradle 9.
         *
         * @see <a href="https://docs.gradle.org/nightly/userguide/isolated_projects.html">Isolated Projects</a>
         */
        public const val ISOLATED_PROJECTS_UNSTABLE: String = "org.gradle.unsafe.isolated-projects=true"

        /**
         * Disable the behavior of the Kotlin Gradle Plugin that adds the stdlib as an `api` dependency by default.
         *
         * @see <a href="https://kotlinlang.org/docs/gradle-configure-project.html#dependency-on-the-standard-library">Dependency on the standard library</a>
         */
        public const val KOTLIN_STDLIB_NO_DEFAULT_DEPS: String = "kotlin.stdlib.default.dependency=false"

        @JvmStatic
        public fun of(vararg lines: CharSequence): GradleProperties {
            // normalize
            val theLines =
                lines.asSequence()
                    .flatMap { it.split('\n') }
                    .map { it.trim() }
                    .toMutableList()

            return GradleProperties(theLines)
        }

        @JvmStatic
        public fun minimalJvmProperties(): GradleProperties = of(JVM_ARGS)

        @JvmStatic
        public fun minimalAndroidProperties(): GradleProperties = of(JVM_ARGS, USE_ANDROID_X, NON_TRANSITIVE_R)

        @JvmStatic
        public fun enableConfigurationCache(): GradleProperties =
            of(
                CONFIGURATION_CACHE_STABLE,
                CONFIGURATION_CACHE_UNSTABLE,
            )

        @JvmStatic
        public fun enableIsolatedProjects(): GradleProperties = of(ISOLATED_PROJECTS_UNSTABLE)

        /**
         * Disable the behavior of the Kotlin Gradle Plugin that adds the stdlib as an `api` dependency by default.
         *
         * @see <a href="https://kotlinlang.org/docs/gradle-configure-project.html#dependency-on-the-standard-library">Dependency on the standard library</a>
         */
        @JvmStatic
        public fun kotlinStdlibNoDefaultDeps(): GradleProperties = of(KOTLIN_STDLIB_NO_DEFAULT_DEPS)
    }

    override fun toString(): String {
        return if (lines.isEmpty()) {
            ""
        } else {
            lines.joinToString("\n")
        }
    }
}
