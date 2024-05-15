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
 * The `buildscript` block:
 * ```
 * // build.gradle[.kts]
 * buildscript {
 *   repositories { ... }
 *   dependencies { ... }
 * }
 * ```
 */
public class BuildscriptBlock(
    private val repositories: Repositories,
    private val dependencies: Dependencies,
) : Element.Block {
    public constructor(
        repositories: MutableList<Repository>,
        dependencies: MutableList<Dependency>,
    ) : this(
        Repositories(repositories),
        Dependencies(dependencies),
    )

    override val name: String = "buildscript"

    override fun render(scribe: Scribe): String =
        scribe.block(this) { s ->
            repositories.render(s)
            dependencies.render(s)
        }

    override fun toString(): String {
        error("don't call toString()")
    }

    public companion object {
        /**
         * This is a `buildscript {}` block that includes AGP in `dependencies.classpath`.
         */
        @JvmStatic
        public fun defaultAndroidBuildscriptBlock(agpVersion: String): BuildscriptBlock {
            return BuildscriptBlock(
                Repository.DEFAULT.toMutableList(),
                mutableListOf(Dependency.androidPlugin(agpVersion)),
            )
        }
    }
}
