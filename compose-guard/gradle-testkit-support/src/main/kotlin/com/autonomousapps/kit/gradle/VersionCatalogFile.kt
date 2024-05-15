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

import org.intellij.lang.annotations.Language

/**
 * Represents a
 * [version catalog file](https://docs.gradle.org/current/userguide/platforms.html#sub::toml-dependencies-format).
 */
public class VersionCatalogFile(
    @Language("toml") public var content: String,
) {
    public companion object {
        public const val DEFAULT_PATH: String = "gradle/libs.versions.toml"
    }

    override fun toString(): String = content

    public class Builder {
        public var versions: MutableList<String> = mutableListOf()
        public var libraries: MutableList<String> = mutableListOf()
        public var bundles: MutableList<String> = mutableListOf()
        public var plugins: MutableList<String> = mutableListOf()

        public fun build(): VersionCatalogFile {
            val content =
                buildString {
                    var didWrite = maybeWriteBlock("versions", versions, false)
                    didWrite = maybeWriteBlock("libraries", libraries, didWrite)
                    didWrite = maybeWriteBlock("bundles", bundles, didWrite)
                    maybeWriteBlock("plugins", plugins, didWrite)
                }

            return VersionCatalogFile(content)
        }

        private fun StringBuilder.maybeWriteBlock(
            name: String,
            section: List<String>,
            prependLine: Boolean,
        ): Boolean {
            if (section.isNotEmpty()) {
                if (prependLine) appendLine()

                appendLine("[$name]")
                section.forEach { appendLine(it) }
                return true
            }

            return false
        }
    }
}
