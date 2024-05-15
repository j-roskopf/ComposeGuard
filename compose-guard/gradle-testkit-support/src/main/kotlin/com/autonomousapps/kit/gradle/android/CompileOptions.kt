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
import org.gradle.api.JavaVersion

/**
 * ```
 * compileOptions {
 *   sourceCompatibility JavaVersion.VERSION_1_8
 *   targetCompatibility JavaVersion.VERSION_1_8
 * }
 * ```
 */
public class CompileOptions
    @JvmOverloads
    constructor(
        private val sourceCompatibility: JavaVersion = JavaVersion.VERSION_1_8,
        private val targetCompatibility: JavaVersion = JavaVersion.VERSION_1_8,
    ) : Element.Block {
        override val name: String = "compileOptions"

        override fun render(scribe: Scribe): String =
            when (scribe.dslKind) {
                DslKind.GROOVY -> renderGroovy(scribe)
                DslKind.KOTLIN -> renderKotlin(scribe)
            }

        private fun renderGroovy(scribe: Scribe): String =
            scribe.block(this) { s ->
                s.line {
                    it.append("sourceCompatibility ")
                    it.append("JavaVersion.")
                    it.append(sourceCompatibility.name)
                }
                s.line {
                    it.append("targetCompatibility ")
                    it.append("JavaVersion.")
                    it.append(targetCompatibility.name)
                }
            }

        private fun renderKotlin(scribe: Scribe): String =
            scribe.block(this) { s ->
                s.line {
                    it.append("sourceCompatibility = ")
                    it.append("JavaVersion.")
                    it.append(sourceCompatibility.name)
                }
                s.line {
                    it.append("targetCompatibility = ")
                    it.append("JavaVersion.")
                    it.append(targetCompatibility.name)
                }
            }

        public companion object {
            @JvmField public val DEFAULT: CompileOptions = CompileOptions()
        }
    }
