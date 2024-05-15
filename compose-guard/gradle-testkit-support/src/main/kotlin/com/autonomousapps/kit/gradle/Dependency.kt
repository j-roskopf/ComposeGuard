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
import com.autonomousapps.kit.internal.ensurePrefix
import com.autonomousapps.kit.render.Element
import com.autonomousapps.kit.render.Scribe

public class Dependency
    @JvmOverloads
    constructor(
        public val configuration: String,
        private val dependency: String,
        private val ext: String? = null,
        private val capability: String? = null,
        private val isVersionCatalog: Boolean = false,
    ) : Element.Line {
        private val isProject = dependency.startsWith(":")

        public val identifier: String = if (isProject) dependency else dependency.substringBeforeLast(":")
        public val version: String? = if (isProject) null else dependency.substringAfterLast(":")

        // TODO(tsr): model this
        override fun render(scribe: Scribe): String =
            when (scribe.dslKind) {
                DslKind.GROOVY -> renderGroovy(scribe)
                DslKind.KOTLIN -> renderKotlin(scribe)
            }

        private fun renderGroovy(scribe: Scribe): String =
            scribe.line { s ->
                val text =
                    when {
                        // project dependency
                        dependency.startsWith(':') -> "$configuration project('$dependency')"
                        // function call
                        dependency.endsWith("()") -> "$configuration $dependency"
                        // Some kind of custom notation
                        !dependency.contains(":") -> "$configuration $dependency"
                        // version catalog reference
                        isVersionCatalog -> "$configuration $dependency"

                        // normal dependency
                        else -> {
                            if (ext == null) {
                                // normal external dependencies
                                "$configuration '$dependency'"
                            } else {
                                // flat dependencies, e.g. in a libs/ dir
                                "$configuration(name: '$dependency', ext: '$ext')"
                            }
                        }
                    }.let {
                        when {
                            // Note: 'testFixtures("...")' is a shorthand for 'requireCapabilities("...-test-fixtures")'
                            capability == "test-fixtures" -> {
                                it.replace("$configuration ", "$configuration testFixtures(") + ")"
                            }

                            capability == "platform" -> {
                                it.replace("$configuration ", "$configuration platform(") + ")"
                            }

                            capability == "enforcedPlatform" -> {
                                it.replace("$configuration ", "$configuration enforcedPlatform(") + ")"
                            }

                            capability != null -> {
                                if (it.startsWith("$configuration ")) {
                                    it.replace("$configuration ", "$configuration(") +
                                        ") { capabilities { requireCapabilities('$capability') } }"
                                } else {
                                    "$it { capabilities { requireCapabilities('$capability') } }"
                                }
                            }

                            else -> it
                        }
                    }

                s.append(text)
            }

        private fun renderKotlin(scribe: Scribe): String =
            scribe.line { s ->
                val text =
                    when {
                        // project dependency
                        dependency.startsWith(':') -> "$configuration(project(\"$dependency\"))"
                        // function call
                        dependency.endsWith("()") -> "$configuration($dependency)"
                        // Some kind of custom notation
                        !dependency.contains(":") -> "$configuration($dependency)"
                        // version catalog reference
                        isVersionCatalog -> "$configuration($dependency)"

                        // normal dependency
                        else -> {
                            if (ext == null) {
                                // normal external dependencies
                                "$configuration(\"$dependency\")"
                            } else {
                                // flat dependencies, e.g. in a libs/ dir
                                "$configuration(name = \"$dependency\", ext = \"$ext\")"
                            }
                        }
                    }.let {
                        when {
                            // Note: 'testFixtures("...")' is a shorthand for 'requireCapabilities("...-test-fixtures")'
                            capability == "test-fixtures" -> {
                                it.replace("$configuration(", "$configuration(testFixtures(") + ")"
                            }

                            capability == "platform" -> {
                                it.replace("$configuration(", "$configuration(platform(") + ")"
                            }

                            capability == "enforcedPlatform" -> {
                                it.replace("$configuration(", "$configuration(enforcedPlatform(") + ")"
                            }

                            capability != null -> "$it { capabilities { requireCapabilities(\"$capability\") } }"

                            else -> it
                        }
                    }

                s.append(text)
            }

        override fun toString(): String {
            error("don't call toString()")
        }

        public companion object {
            @JvmStatic
            public fun annotationProcessor(dependency: String): Dependency {
                return Dependency("annotationProcessor", dependency)
            }

            @JvmStatic
            public fun api(dependency: String): Dependency {
                return Dependency("api", dependency)
            }

            @JvmStatic
            public fun classpath(dependency: String): Dependency {
                return Dependency("classpath", dependency)
            }

            @JvmStatic
            public fun compileOnly(dependency: String): Dependency {
                return Dependency("compileOnly", dependency)
            }

            @JvmStatic
            public fun compileOnlyApi(dependency: String): Dependency {
                return Dependency("compileOnlyApi", dependency)
            }

            @JvmStatic
            public fun implementation(dependency: String): Dependency {
                return Dependency("implementation", dependency)
            }

            @JvmStatic
            public fun runtimeOnly(dependency: String): Dependency {
                return Dependency("runtimeOnly", dependency)
            }

            @JvmStatic
            public fun testCompileOnly(dependency: String): Dependency {
                return Dependency("testCompileOnly", dependency)
            }

            @JvmStatic
            public fun testImplementation(dependency: String): Dependency {
                return Dependency("testImplementation", dependency)
            }

            @JvmStatic
            public fun testRuntimeOnly(dependency: String): Dependency {
                return Dependency("testRuntimeOnly", dependency)
            }

            @JvmStatic
            public fun project(
                configuration: String,
                path: String,
            ): Dependency {
                return Dependency(configuration, path.ensurePrefix())
            }

            @JvmStatic
            public fun project(
                configuration: String,
                path: String,
                capability: String,
            ): Dependency {
                return Dependency(configuration, path.ensurePrefix(), capability = capability)
            }

            @JvmStatic
            public fun raw(
                configuration: String,
                dependency: String,
            ): Dependency {
                check(!dependency.contains(":")) { "Not meant for normal dependencies. Was '$dependency'." }
                return Dependency(configuration, dependency)
            }

            @JvmStatic
            public fun versionCatalog(
                configuration: String,
                ref: String,
            ): Dependency {
                return Dependency(
                    configuration = configuration,
                    dependency = ref,
                    isVersionCatalog = true,
                )
            }

    /*
     * Plugin classpaths
     */

            @JvmStatic
            public fun androidPlugin(version: String = "3.6.3"): Dependency {
                return Dependency("classpath", "com.android.tools.build:gradle:$version")
            }
        }
    }
