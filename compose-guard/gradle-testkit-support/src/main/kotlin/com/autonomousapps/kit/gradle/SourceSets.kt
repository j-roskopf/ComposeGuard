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

import com.autonomousapps.kit.render.Element
import com.autonomousapps.kit.render.Scribe

public class SourceSets
    @JvmOverloads
    constructor(
        public val sourceSets: MutableList<SourceSet> = mutableListOf(),
    ) : Element.Block {
        public fun isEmpty(): Boolean = sourceSets.isEmpty()

        override val name: String = "sourceSets"

        override fun render(scribe: Scribe): String =
            scribe.block(this) { s ->
                sourceSets.forEach { it.render(s) }
            }

        public operator fun plus(other: SourceSets): SourceSets {
            val newSourceSets =
                ArrayList(sourceSets).apply {
                    addAll(other.sourceSets)
                }
            return SourceSets(newSourceSets)
        }

        public companion object {
            @JvmField
            public val EMPTY: SourceSets = SourceSets()

            @JvmStatic
            public fun ofNames(names: Iterable<String>): SourceSets {
                return SourceSets(names.mapTo(mutableListOf()) { SourceSet(it) })
            }

            @JvmStatic
            public fun ofNames(vararg names: String): SourceSets = ofNames(names.toList())
        }
    }
