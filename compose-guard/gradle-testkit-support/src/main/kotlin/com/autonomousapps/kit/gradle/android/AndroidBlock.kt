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
package com.autonomousapps.kit.gradle.android

import com.autonomousapps.kit.GradleProject.DslKind
import com.autonomousapps.kit.render.Element
import com.autonomousapps.kit.render.Scribe

/**
 * The `android` block, for use by projects build with the Android Gradle Plugin.
 * ```
 * // build.gradle[.kts]
 * android {
 *   ...
 * }
 * ```
 */
public class AndroidBlock
    @JvmOverloads
    constructor(
        public var namespace: String? = null,
        public var compileSdkVersion: Int = 34,
        public var defaultConfig: DefaultConfig = DefaultConfig.DEFAULT_APP,
        public var compileOptions: CompileOptions = CompileOptions.DEFAULT,
        public var kotlinOptions: KotlinOptions? = null,
    ) : Element.Block {
        override val name: String = "android"

        override fun render(scribe: Scribe): String =
            when (scribe.dslKind) {
                DslKind.GROOVY -> renderGroovy(scribe)
                DslKind.KOTLIN -> renderKotlin(scribe)
            }

        private fun renderGroovy(scribe: Scribe): String =
            scribe.block(this) { s ->
                if (namespace != null) {
                    s.line {
                        it.append("namespace '")
                        it.append(namespace)
                        it.append("'")
                    }
                }
                s.line {
                    it.append("compileSdkVersion ")
                    it.append(compileSdkVersion)
                }
                defaultConfig.render(s)
                compileOptions.render(s)
                kotlinOptions?.render(s)
            }

        private fun renderKotlin(scribe: Scribe): String =
            scribe.block(this) { s ->
                if (namespace != null) {
                    s.line {
                        it.append("namespace = \"")
                        it.append(namespace)
                        it.append("\"")
                    }
                }
                s.line {
                    it.append("compileSdk = ")
                    it.append(compileSdkVersion)
                }
                defaultConfig.render(s)
                compileOptions.render(s)
                kotlinOptions?.render(s)
            }

        public class Builder {
            public var namespace: String? = null
            public var compileSdkVersion: Int = 34
            public var defaultConfig: DefaultConfig = DefaultConfig.DEFAULT_APP
            public var compileOptions: CompileOptions = CompileOptions.DEFAULT
            public var kotlinOptions: KotlinOptions? = null

            public fun build(): AndroidBlock {
                return AndroidBlock(
                    namespace = namespace,
                    compileSdkVersion = compileSdkVersion,
                    defaultConfig = defaultConfig,
                    compileOptions = compileOptions,
                    kotlinOptions = kotlinOptions,
                )
            }
        }

        public companion object {
            @JvmOverloads
            @JvmStatic
            public fun defaultAndroidAppBlock(
                isKotlinApplied: Boolean = false,
                namespace: String? = null,
            ): AndroidBlock =
                AndroidBlock(
                    namespace = namespace,
                    defaultConfig = DefaultConfig.DEFAULT_APP,
                    kotlinOptions = if (isKotlinApplied) KotlinOptions.DEFAULT else null,
                )

            @JvmOverloads
            @JvmStatic
            public fun defaultAndroidLibBlock(
                isKotlinApplied: Boolean = false,
                namespace: String? = null,
            ): AndroidBlock =
                AndroidBlock(
                    namespace = namespace,
                    defaultConfig = DefaultConfig.DEFAULT_LIB,
                    kotlinOptions = if (isKotlinApplied) KotlinOptions.DEFAULT else null,
                )
        }
    }
