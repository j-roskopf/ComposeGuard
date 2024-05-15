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

import com.autonomousapps.kit.AbstractGradleProject.Companion.FUNC_TEST_INCLUDED_BUILD_REPOS
import com.autonomousapps.kit.AbstractGradleProject.Companion.FUNC_TEST_REPO
import com.autonomousapps.kit.GradleProject.DslKind
import com.autonomousapps.kit.render.Element
import com.autonomousapps.kit.render.Scribe
import com.autonomousapps.kit.render.escape

/**
 * ```
 * // Groovy DSL
 * maven { url 'https://repo.spring.io/release' }
 * mavenCentral()
 *
 * // Kotlin DSL
 * // 1
 * maven { url = uri("https://repo.spring.io/release") }
 * mavenCentral()
 *
 * // 2
 * maven(url = "https://repo.spring.io/release")
 * mavenCentral()
 * ```
 */
public sealed class Repository : Element.Line {
    private data class Method(private val repoCall: String) : Repository() {
        override fun render(scribe: Scribe): String =
            scribe.line { s ->
                s.append(repoCall)
            }
    }

    private data class Url(private val repoUrl: String) : Repository() {
        override fun render(scribe: Scribe): String =
            scribe.line { s ->
                s.append("maven { url = ")

                // TODO(tsr): model
                if (scribe.dslKind == DslKind.KOTLIN) {
                    s.append("uri(")
                }
                s.appendQuoted(repoUrl)
                if (scribe.dslKind == DslKind.KOTLIN) {
                    s.append(")")
                }

                s.append(" }")
            }
    }

    private data class FlatDir(private val repoUrl: String) : Repository() {
        override fun render(scribe: Scribe): String =
            scribe.line { s ->
                s.append("flatDir { ")
                s.appendQuoted(repoUrl)
                s.append(" }")
            }
    }

    public companion object {
        @JvmField public val GOOGLE: Repository = Method("google()")

        @JvmField public val GRADLE_PLUGIN_PORTAL: Repository = Method("gradlePluginPortal()")

        @JvmField public val MAVEN_CENTRAL: Repository = Method("mavenCentral()")

        @JvmField public val MAVEN_LOCAL: Repository = Method("mavenLocal()")

        @JvmField public val SNAPSHOTS: Repository = ofMaven("https://oss.sonatype.org/content/repositories/snapshots/")

        @JvmField public val LIBS: Repository = FlatDir("libs")

        /**
         * The repository for local projects if you're using the plugin `com.autonomousapps.testkit`. If not, a broken,
         * unusable repo.
         */
        @JvmField public val FUNC_TEST: Repository = ofMaven(FUNC_TEST_REPO)

        /**
         * The repository for local projects if you're using the plugin `com.autonomousapps.testkit` in your included
         * builds. If not, an empty list. See documentation on that plugin for how to configure.
         */
        @JvmField public val FUNC_TEST_INCLUDED_BUILDS: List<Repository> =
            FUNC_TEST_INCLUDED_BUILD_REPOS
                .filterNot { it.isEmpty() }
                .map { ofMaven(it) }

        @JvmField
        public val DEFAULT: List<Repository> =
            listOf(
                FUNC_TEST,
                MAVEN_CENTRAL,
                GOOGLE,
            )

        @JvmField
        public val DEFAULT_PLUGINS: List<Repository> =
            listOf(
                FUNC_TEST,
                GRADLE_PLUGIN_PORTAL,
                MAVEN_CENTRAL,
                GOOGLE,
            )

        @JvmStatic
        public fun ofMaven(repoUrl: String): Repository = Url(repoUrl.escape())
    }
}
