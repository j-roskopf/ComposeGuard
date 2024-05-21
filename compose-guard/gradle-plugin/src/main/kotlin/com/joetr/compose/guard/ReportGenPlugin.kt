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

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.joetr.compose.guard.task.ComposeCompilerReportCheckTask
import com.joetr.compose.guard.task.ComposeCompilerReportCleanTask
import com.joetr.compose.guard.task.ComposeCompilerReportGenerateTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

private const val REPORT_PARAM = "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination="
private const val METRIC_PARAM = "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination="
private const val GENERATE_TASK_NAME = "ComposeCompilerGenerate"
private const val CHECK_TASK_NAME = "ComposeCompilerCheck"
private const val CLEAN_TASK_NAME = "composeCompilerClean"

public class ReportGenPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // create extensions
        ComposeCompilerReportExtension.create(target)
        ComposeCompilerCheckExtension.create(target)

        // register directories for storing metrics for generate + check tasks
        target.registerComposeParameters()

        // register generate + check tasks for library modules
        target.extensions.findByType<LibraryAndroidComponentsExtension>()?.beforeVariants {
            registerGenerateTask(
                name = it.name,
                target = target,
            )
            registerCheckTask(
                name = it.name,
                target = target,
            )
        }

        // register generate + check tasks for app module
        target.extensions.findByType<ApplicationAndroidComponentsExtension>()?.beforeVariants {
            registerGenerateTask(
                name = it.name,
                target = target,
            )
            registerCheckTask(
                name = it.name,
                target = target,
            )
        }

        // register clean task
        target.registerComposeCompilerReportCleanTask()
    }

    private fun registerGenerateTask(
        name: String,
        target: Project,
    ) {
        val reportExtension = ComposeCompilerReportExtension.get(target)

        val variantTask =
            target.tasks.register<ComposeCompilerReportGenerateTask>(
                name = "${name}$GENERATE_TASK_NAME",
            ) {
                this.outputDirectory.set(target.layout.dir(reportExtension.outputDirectory))
            }

        // make task depend on compile kotlin task
        variantTask.configure(
            object : Action<ComposeCompilerReportGenerateTask> {
                override fun execute(t: ComposeCompilerReportGenerateTask) {
                    t.dependsOn(target.tasks.named("compile${name.capitalized()}Kotlin"))
                }
            },
        )
    }

    private fun registerCheckTask(
        name: String,
        target: Project,
    ) {
        val checkExtension = target.extensions.getByType<ComposeCompilerCheckExtension>()
        val genExtension = target.extensions.getByType<ComposeCompilerReportExtension>()

        val variantTask =
            target.tasks.register<ComposeCompilerReportCheckTask>(
                name = "${name}$CHECK_TASK_NAME",
            ) {
                outputDirectory.set(target.layout.dir(genExtension.outputDirectory))
                genOutputDirectoryPath.set(genExtension.outputDirectory.get().absolutePath)
                checkOutputDirectoryPath.set(checkExtension.outputDirectory.get().absolutePath)
                inputDirectory.set(checkExtension.outputDirectory.get())
            }

        // make task depend on compile kotlin task
        variantTask.configure(
            object : Action<ComposeCompilerReportCheckTask> {
                override fun execute(t: ComposeCompilerReportCheckTask) {
                    t.dependsOn(target.tasks.named("compile${name.capitalized()}Kotlin"))
                }
            },
        )
    }

    private fun Project.registerComposeCompilerReportCleanTask(): TaskProvider<ComposeCompilerReportCleanTask> {
        val checkExtension = project.extensions.getByType<ComposeCompilerCheckExtension>()
        val genExtension = project.extensions.getByType<ComposeCompilerReportExtension>()

        val task = tasks.register(CLEAN_TASK_NAME, ComposeCompilerReportCleanTask::class.java)

        task.get().genOutputDirectoryPath.set(genExtension.outputDirectory.get().absolutePath)
        task.get().checkOutputDirectoryPath.set(checkExtension.outputDirectory.get().absolutePath)
        return task
    }

    private fun Project.registerComposeParameters() {
        val checkExtension = extensions.getByType<ComposeCompilerCheckExtension>()
        val genExtension = extensions.getByType<ComposeCompilerReportExtension>()

        val isGenDirectoryNotEmpty = genExtension.outputDirectory.get().list()?.isNotEmpty() == true

        // if output directory doesn't exist, re-run kotlin compile task
        tasks.withType(KotlinJvmCompile::class.java).configureEach(
            object : Action<KotlinJvmCompile> {
                override fun execute(t: KotlinJvmCompile) {
                    t.outputs.upToDateWhen {
                        isGenDirectoryNotEmpty
                    }
                }
            },
        )

        // Depending on this task. We need to do this to generate the metrics report
        tasks.withType(KotlinJvmCompile::class.java).whenTaskAdded(
            object : Action<KotlinJvmCompile> {
                override fun execute(t: KotlinJvmCompile) {
                    t.kotlinOptions {
                        if (gradle.startParameter.taskNames.any {
                                it.contains(CHECK_TASK_NAME)
                            }
                        ) {
                            freeCompilerArgs = freeCompilerArgs +
                                listOf(
                                    "-P",
                                    REPORT_PARAM + checkExtension.outputDirectory.get().absolutePath,
                                )
                            freeCompilerArgs = freeCompilerArgs +
                                listOf(
                                    "-P",
                                    METRIC_PARAM + checkExtension.outputDirectory.get().absolutePath,
                                )
                        } else if (gradle.startParameter.taskNames.any {
                                it.contains(GENERATE_TASK_NAME)
                            }
                        ) {
                            freeCompilerArgs = freeCompilerArgs +
                                listOf(
                                    "-P",
                                    REPORT_PARAM + genExtension.outputDirectory.get().absolutePath,
                                )
                            freeCompilerArgs = freeCompilerArgs +
                                listOf(
                                    "-P",
                                    METRIC_PARAM + genExtension.outputDirectory.get().absolutePath,
                                )
                        }
                    }
                }
            },
        )
    }
}
