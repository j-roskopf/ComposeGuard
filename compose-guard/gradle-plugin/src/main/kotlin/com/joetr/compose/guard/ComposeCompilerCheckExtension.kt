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

interface ComposeCompilerCheckExtension {
    val outputDirectory: Property<File>

    val composeRawMetricsOutputDirectory: File
        get() = outputDirectory.get().resolve("raw")

    companion object {
        private const val NAME = "composeCompilerCheck"

        /**
         * Creates an extension of type [ComposeCompilerCheckExtension] and returns
         */
        fun create(target: Project) =
            target.extensions.create<ComposeCompilerCheckExtension>(NAME).apply {
                outputDirectory.convention(target.layout.buildDirectory.asFile.get().resolve("compose_reports"))
            }

        /**
         * Get extensions applied to the [target] project.
         */
        fun get(target: Project) = target.extensions.getByType<ComposeCompilerCheckExtension>()
    }
}
