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
import com.joetr.compose.guard.KEY_CHECK_GEN
import com.joetr.compose.guard.core.ComposeCompilerMetricsProvider
import com.joetr.compose.guard.core.ComposeCompilerRawReportProvider
import com.joetr.compose.guard.core.utils.ensureFileExists
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import java.io.File

internal abstract class ComposeCompilerReportCheckTask : DefaultTask() {
    @get:Input
    abstract val compileKotlinTasks: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val genOutputDirectoryPath: Property<String>

    @get:Input
    abstract val checkOutputDirectoryPath: Property<String>

    @get:OutputDirectory
    abstract val projectDirectory: DirectoryProperty

    /** This input only exists to signal task dependencies */
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    abstract val kotlinSources: ConfigurableFileCollection

    @TaskAction
    fun check() {
        generateRawMetricsAndReport()

        val genOutputDirectory = File(genOutputDirectoryPath.get())
        val checkOutputDirectory = File(checkOutputDirectoryPath.get())

        ensureFileExists(genOutputDirectory) {
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

        ComposeChecks.check(checkedMetrics, goldenMetrics)
    }

    private fun generateRawMetricsAndReport() {
        GradleConnector.newConnector().forProjectDirectory(projectDirectory.asFile.get())
            .connect()
            .use {
                it.newBuild()
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .setStandardInput(System.`in`)
                    .forTasks(compileKotlinTasks.get())
                    .withArguments(
                        // Re-running is necessary. In case devs deleted raw files and if task uses cache
                        // then this task will explode 💣
                        "--rerun-tasks",
                        // Signal for enabling report generation in `kotlinOptions{}` block.
                        "-P$KEY_CHECK_GEN=true",
                    )
                    .run()
            }
    }
}
