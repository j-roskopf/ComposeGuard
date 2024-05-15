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

/**
 * ```
 * // Groovy DSL
 * repositories {
 *   maven { url 'https://repo.spring.io/release' }
 * }
 *
 * // Kotlin DSL
 * repositories {
 *   // 1
 *   maven { url = uri("https://repo.spring.io/release") }
 *
 *   // 2
 *   maven(url = "https://repo.spring.io/release")
 * }
 * ```
 */
public class Repositories
    @JvmOverloads
    constructor(
        private val repositories: MutableList<Repository> = mutableListOf(),
    ) : Element.Block {
        public constructor(vararg repositories: Repository) : this(repositories.toMutableList())

        public val isEmpty: Boolean = repositories.isEmpty()

        override val name: String = "repositories"

        override fun render(scribe: Scribe): String =
            scribe.block(this) { s ->
                repositories.forEach { it.render(s) }
            }

        public operator fun plus(other: Repositories): Repositories {
            return Repositories((repositories + other.repositories).distinct().toMutableList())
        }

        public operator fun plus(other: Iterable<Repository>): Repositories {
            return Repositories((repositories + other).distinct().toMutableList())
        }

        public companion object {
            @JvmField
            public val EMPTY: Repositories = Repositories(mutableListOf())

            @JvmField
            public val DEFAULT_DEPENDENCIES: Repositories = Repositories(Repository.DEFAULT.toMutableList())

            @JvmField
            public val DEFAULT_PLUGINS: Repositories = Repositories(Repository.DEFAULT_PLUGINS.toMutableList())
        }
    }
