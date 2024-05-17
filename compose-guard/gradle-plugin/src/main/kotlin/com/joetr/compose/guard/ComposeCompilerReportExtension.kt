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

public interface ComposeCompilerReportExtension {
    /**
     * The directory where report will be stored.
     */
    public val outputDirectory: Property<File>

    public val composeRawMetricsOutputDirectory: File
        get() = outputDirectory.get().resolve("raw")

    public companion object {
        private const val NAME = "composeGuard"

        /**
         * Creates an extension of type [ComposeCompilerReportExtension] and returns
         */
        public fun create(target: Project): ComposeCompilerReportExtension =
            target.extensions.create<ComposeCompilerReportExtension>(NAME).apply {
                outputDirectory.convention(target.projectDir.resolve("compose_reports"))
            }

        /**
         * Get extensions applied to the [target] project.
         */
        public fun get(target: Project): ComposeCompilerReportExtension = target.extensions.getByType<ComposeCompilerReportExtension>()
    }
}
