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
import com.autonomousapps.kit.render.Scribe

public class SettingsScript
    @JvmOverloads
    constructor(
        public var pluginManagement: PluginManagement = PluginManagement.DEFAULT,
        public var dependencyResolutionManagement: DependencyResolutionManagement? = DependencyResolutionManagement.DEFAULT,
        public var rootProjectName: String = "the-project",
        public var plugins: Plugins = Plugins.EMPTY,
        public var subprojects: Set<String> = emptySet(),
        /** For random stuff, as-yet unmodeled. */
        public var additions: String = "",
    ) {
        public fun render(scribe: Scribe): String =
            buildString {
                appendLine(scribe.use { s -> pluginManagement.render(s) })

                if (!plugins.isEmpty) {
                    appendLine(scribe.use { s -> plugins.render(s) })
                }

                dependencyResolutionManagement?.let { d ->
                    appendLine(scribe.use { s -> d.render(s) })
                }

                appendLine(renderRootProjectName(scribe.dslKind))
                appendLine()
                appendLine(subprojects.joinToString("\n") { renderInclude(scribe.dslKind, it) })

                if (additions.isNotBlank()) {
                    appendLine()
                    appendLine(additions)
                }
            }

        private fun renderRootProjectName(dslKind: DslKind) =
            when (dslKind) {
                DslKind.GROOVY -> "rootProject.name = '$rootProjectName'"
                DslKind.KOTLIN -> "rootProject.name = \"$rootProjectName\""
            }

        private fun renderInclude(
            dslKind: DslKind,
            subproject: String,
        ) = when (dslKind) {
            DslKind.GROOVY -> "include ':$subproject'"
            DslKind.KOTLIN -> "include(\":$subproject\")"
        }
    }
