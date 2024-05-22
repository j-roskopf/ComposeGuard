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
package com.joetr.compose.guard.task

import com.joetr.compose.guard.ComposeChecks
import com.joetr.compose.guard.ComposeCompilerCheckExtension
import com.joetr.compose.guard.core.ComposeCompilerMetricsProvider
import com.joetr.compose.guard.core.ComposeCompilerRawReportProvider
import com.joetr.compose.guard.core.utils.ensureDirectoryIsNotEmpty
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class ComposeCompilerReportCheckTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputDirectory
    @get:Optional // not present on first run
    abstract val inputDirectory: DirectoryProperty

    @get:Input
    abstract val genOutputDirectoryPath: Property<String>

    @get:Input
    abstract val checkOutputDirectoryPath: Property<String>

    @get:Input
    abstract val composeCompilerCheckExtension: Property<ComposeCompilerCheckExtension>

    @TaskAction
    fun check() {
        val genOutputDirectory = File(genOutputDirectoryPath.get())
        val checkOutputDirectory = File(checkOutputDirectoryPath.get())

        ensureDirectoryIsNotEmpty(genOutputDirectory) {
            "Golden metrics do not exist! Please generate them using the <variant>ComposeCompilerGenerate task"
        }

        val goldenMetrics =
            ComposeCompilerMetricsProvider(
                ComposeCompilerRawReportProvider.FromDirectory(
                    directory = genOutputDirectory,
                ),
            )

        val checkedMetrics =
            ComposeCompilerMetricsProvider(
                ComposeCompilerRawReportProvider.FromDirectory(
                    directory = checkOutputDirectory,
                ),
            )

        ComposeChecks.check(
            checkedMetrics = checkedMetrics,
            goldenMetrics = goldenMetrics,
            composeCompilerCheckExtension = composeCompilerCheckExtension,
        )
    }
}
