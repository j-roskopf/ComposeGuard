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

import com.autonomousapps.kit.GradleProject.DslKind
import com.autonomousapps.kit.render.Element
import com.autonomousapps.kit.render.Scribe

public class Plugin
    @JvmOverloads
    constructor(
        public val id: String,
        public val version: String? = null,
        public val apply: Boolean = true,
    ) : Element.Line {
        override fun render(scribe: Scribe): String =
            when (scribe.dslKind) {
                DslKind.GROOVY -> renderGroovy(scribe)
                DslKind.KOTLIN -> renderKotlin(scribe)
            }

        private fun renderGroovy(scribe: Scribe): String =
            scribe.line { s ->
                s.append("id ")
                s.appendQuoted(id)
                version?.let { v ->
                    s.append(" version ")
                    s.appendQuoted(v)
                }
                if (!apply) {
                    s.append(" apply false")
                }
            }

        private fun renderKotlin(scribe: Scribe): String =
            scribe.line { s ->
                s.append("id(")
                s.appendQuoted(id)
                s.append(")")
                version?.let { v ->
                    s.append(" version ")
                    s.appendQuoted(v)
                }
                if (!apply) {
                    s.append(" apply false")
                }
            }

        public companion object {
            @JvmOverloads
            @JvmStatic
            public fun of(
                id: String,
                version: String? = null,
                apply: Boolean = true,
            ): Plugin = Plugin(id, version, apply)

    /*
     * Gradle core plugins.
     */

            @JvmStatic public val antlr: Plugin = Plugin("antlr")

            @JvmStatic public val application: Plugin = Plugin("application")

            @JvmStatic public val groovy: Plugin = Plugin("groovy")

            @JvmStatic public val groovyGradle: Plugin = Plugin("groovy-gradle-plugin")

            @JvmStatic public val jacocoReportAggregation: Plugin = Plugin("jacoco-report-aggregation")

            @JvmStatic public val java: Plugin = Plugin("java")

            @JvmStatic public val javaGradle: Plugin = Plugin("java-gradle-plugin")

            @JvmStatic public val javaLibrary: Plugin = Plugin("java-library")

            @JvmStatic public val javaTestFixtures: Plugin = Plugin("java-test-fixtures")

            @JvmStatic public val jvmTestSuite: Plugin = Plugin("jvm-test-suite")

            @JvmStatic public val scala: Plugin = Plugin("scala")

            @JvmStatic public val war: Plugin = Plugin("war")
        }
    }
