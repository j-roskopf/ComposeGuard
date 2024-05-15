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
package com.autonomousapps.kit.render

import com.autonomousapps.kit.GradleProject

public class Scribe
    @JvmOverloads
    constructor(
        /** Which Gradle DSL to use for rendering. */
        public val dslKind: GradleProject.DslKind = GradleProject.DslKind.GROOVY,
        /** Indent level when entering a block. */
        public val indent: Int = 2,
    ) : AutoCloseable {
        private val buffer = StringBuilder()

        /** Starting indent for any block. */
        private var start: Int = 0

        /** Enter a block, increase the indent. */
        private fun enter() {
            start += indent
        }

        /** Exit a block, decrease the indent. */
        private fun exit() {
            start -= indent
        }

        override fun close() {
            buffer.clear()
            start = 0
        }

        internal fun block(
            element: Element.Block,
            block: (Scribe) -> Unit,
        ): String {
            // e.g., "plugins {"
            indent()
            buffer.append(element.name)
            buffer.appendLine(" {")

            // increase the indent
            enter()

            // write the block inside the {}
            block(this)

            // decrease the indent
            exit()

            // closing brace
            indent()
            buffer.appendLine("}")

            // return the string
            return buffer.toString()
        }

        internal fun line(block: (Scribe) -> Unit): String {
            indent()
            block(this)
            buffer.appendLine()

            return buffer.toString()
        }

        internal fun append(obj: Any?) {
            buffer.append(obj.toString())
        }

        internal fun appendLine() {
            buffer.appendLine()
        }

        private fun indent() {
            buffer.append(" ".repeat(start))
        }

        internal fun appendQuoted(obj: Any?) {
            append(quote())
            append(obj.toString())
            append(quote())
        }

        private fun quote(): String = if (dslKind == GradleProject.DslKind.GROOVY) "'" else "\""
    }
