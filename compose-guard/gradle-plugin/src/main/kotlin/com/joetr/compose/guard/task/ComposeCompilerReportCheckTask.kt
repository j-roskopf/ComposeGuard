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
import com.joetr.compose.guard.core.utils.ensureVariantsExistsInDirectory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.tooling.GradleConnector
import java.io.File
import kotlin.io.path.Path

internal abstract class ComposeCompilerReportCheckTask : DefaultTask() {
    /**
     * The output directory of this task - by default build/compose_reports
     */
    @get:OutputDirectory
    @get:Optional // not present on first run or on modules with empty source sets
    abstract val outputDirectory: DirectoryProperty

    /**
     * The output directory of the generate task - by default module/compose_reports
     */
    @get:Input
    @get:Optional // not present on first run or on modules with empty source sets
    abstract val genOutputDirectoryPath: Property<String>

    /**
     * Multiplatform compilation target. Used in a multiplatform project
     * to detect location of the compose reports. In a multiplatform project,
     * they live under compose_report/<multiplatformCompilationTarget>/<files>
     */
    @get:Input
    abstract val multiplatformCompilationTarget: Property<String>

    /**
     * Variant (blank if no variant) for android / multiplatform targets
     *
     * Debug / Release etc
     */
    @get:Input
    abstract val compilationVariant: Property<String>

    /**
     * The kotlin compilation task name
     *
     * compileDebugKotlin etc
     */
    @get:Input
    abstract val compilationTaskName: Property<String>

    /**
     * Output directory path
     *
     * TODO joer - can this be consolidated with the output directory property?
     */
    @get:Input
    abstract val checkOutputDirectoryPath: Property<String>

    /**
     * If we have no main source sets, don't produce results
     */
    @get:Input
    abstract val hasKotlinMainSourceSet: Property<Boolean>

    /**
     * If set, don't error on missing baseline, and just report all errors
     */
    @get:Input
    abstract val reportAllOnMissingBaseline: Property<Boolean>

    /**
     * All kotlin sources for the module
     *
     * Marked as an input so we can re-run when a change occurs
     */
    @get:InputFiles
    @get:Optional
    abstract val kotlinSourceSets: ListProperty<File>

    /**
     * Configuration of this task
     *
     * Used to validate if we should error on certain conditions etc
     */
    @get:Input
    abstract val composeCompilerCheckExtension: Property<ComposeCompilerCheckExtension>

    @get:Input
    abstract val taskNameProperty: Property<String>

    @get:InputFiles
    @get:Optional
    abstract val genFiles: ListProperty<File>

    @TaskAction
    fun check() {
        val genOutputDirectory = File(genOutputDirectoryPath.get())

        val checkOutputDirectory =
            if (multiplatformCompilationTarget.get().isNotEmpty()) {
                File(
                    checkOutputDirectoryPath.get().plus(File.separator)
                        .plus(multiplatformCompilationTarget.get()),
                )
            } else {
                File(checkOutputDirectoryPath.get())
            }

        if (hasKotlinMainSourceSet.get()) {
            if (reportAllOnMissingBaseline.get().not()) {
                // if reportAllOnMissingBaseline is not enabled, verify baseline exists
                ensureVariantsExistsInDirectory(genOutputDirectory.resolve(compilationTaskName.get()), compilationVariant.get())
            }

            val goldenDirectory =
                if (multiplatformCompilationTarget.get().isNotEmpty()) {
                    File(
                        genOutputDirectoryPath.get().plus(File.separator)
                            .plus(multiplatformCompilationTarget.get()),
                    )
                } else {
                    File(genOutputDirectoryPath.get())
                }

            val goldenMetrics =
                if (reportAllOnMissingBaseline.get()) {
                    ComposeCompilerMetricsProvider(
                        ComposeCompilerRawReportProvider.Empty(),
                    )
                } else {
                    ComposeCompilerMetricsProvider(
                        ComposeCompilerRawReportProvider.FromDirectory(
                            directory = goldenDirectory,
                            variant = compilationVariant.get(),
                        ),
                    )
                }

            val containsReport =
                checkOutputDirectory.listFiles()?.any {
                    it.name.contains("-composables.txt")
                } ?: false

            if (checkOutputDirectory.exists() && containsReport) {
                val checkedMetrics =
                    ComposeCompilerMetricsProvider(
                        ComposeCompilerRawReportProvider.FromDirectory(
                            directory = checkOutputDirectory,
                            variant = compilationVariant.get(),
                        ),
                    )

                ComposeChecks.check(
                    checkedMetrics = checkedMetrics,
                    goldenMetrics = goldenMetrics,
                    composeCompilerCheckExtension = composeCompilerCheckExtension,
                )
            } else {
                GradleConnector.newConnector()
                    .forProjectDirectory(Path(checkOutputDirectoryPath.get()).parent.parent.toFile())
                    .connect()
                    .use {
                        it.newBuild()
                            .setStandardOutput(System.out)
                            .setStandardError(System.err)
                            .setStandardInput(System.`in`)
                            .forTasks(taskNameProperty.get())
                            .withArguments(
                                // Re-running is necessary. In case devs deleted raw files and if task uses cache
                                // then this task will explode 💣
                                "--rerun-tasks",
                            )
                            .run()
                    }
            }
        }
    }
}
