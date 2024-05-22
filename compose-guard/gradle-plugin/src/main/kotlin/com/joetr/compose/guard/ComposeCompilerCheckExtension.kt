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
package com.joetr.compose.guard

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import java.io.File

public interface ComposeCompilerCheckExtension {
    public val outputDirectory: Property<File>

    public val errorOnNewUnstableClasses: Property<Boolean>
    public val errorOnNewUnstableParams: Property<Boolean>
    public val errorOnNewDynamicProperties: Property<Boolean>
    public val errorOnNewRestartableButNotSkippableComposables: Property<Boolean>

    public companion object {
        private const val NAME = "composeGuardCheck"

        /**
         * Creates an extension of type [ComposeCompilerCheckExtension] and returns
         */
        public fun create(target: Project): ComposeCompilerCheckExtension =
            target.extensions.create<ComposeCompilerCheckExtension>(NAME).apply {
                outputDirectory.convention(target.layout.buildDirectory.asFile.get().resolve("compose_reports"))
                errorOnNewUnstableClasses.convention(true)
                errorOnNewUnstableParams.convention(true)
                errorOnNewDynamicProperties.convention(true)
                errorOnNewRestartableButNotSkippableComposables.convention(true)
            }

        /**
         * Get extensions applied to the [target] project.
         */
        public fun get(target: Project): ComposeCompilerCheckExtension = target.extensions.getByType<ComposeCompilerCheckExtension>()
    }
}
