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
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File

private const val REPORT_PARAM = "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination="
private const val METRIC_PARAM = "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination="
private const val GENERATE_TASK_NAME = "ComposeCompilerGenerate"
private const val CHECK_TASK_NAME = "ComposeCompilerCheck"
private const val CLEAN_TASK_NAME = "composeCompilerClean"

public class ReportGenPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // create extensions
        ComposeCompilerReportExtension.create(target)
        InternalComposeCompilerCheckExtension.create(target)
        ComposeCompilerCheckExtension.create(target)

        // register directories for storing metrics for generate + check tasks
        target.registerComposeParameters()

        if (target.isMultiplatformProject()) {
            target.registerTasksForMultiplatformTargets()
        } else {
            // assume android project
            target.registerTasksForAndroidProject()
        }

        // register clean task
        target.registerComposeCompilerReportCleanTask()
    }

    private fun registerGenerateTask(
        target: String,
        project: Project,
        variant: String,
    ) {
        val reportExtension = ComposeCompilerReportExtension.get(project)

        val taskName =
            if (project.isMultiplatformProject()) {
                "${target}${variant.capitalized()}$GENERATE_TASK_NAME"
            } else {
                "${variant}$GENERATE_TASK_NAME"
            }

        val variantTask =
            project.tasks.register<ComposeCompilerReportGenerateTask>(
                name = taskName,
            ) {
                if (project.hasNonEmptyKotlinSourceSets()) {
                    this.outputDirectory.set(project.layout.dir(reportExtension.outputDirectory))
                }
            }

        // make task depend on compile kotlin task
        variantTask.configure(
            object : Action<ComposeCompilerReportGenerateTask> {
                override fun execute(t: ComposeCompilerReportGenerateTask) {
                    if (project.isMultiplatformProject()) {
                        t.dependsOn(project.tasks.named("compile${variant.capitalized()}Kotlin${target.capitalized()}"))
                    } else {
                        t.dependsOn(project.tasks.named("compile${variant.capitalized()}Kotlin"))
                    }
                }
            },
        )
    }

    private fun registerCheckTask(
        target: String,
        project: Project,
        variant: String,
    ) {
        val checkExtension = project.extensions.getByType<ComposeCompilerCheckExtension>()
        val genExtension = project.extensions.getByType<ComposeCompilerReportExtension>()
        val internalExtension = project.extensions.getByType<InternalComposeCompilerCheckExtension>()

        val taskName =
            if (project.isMultiplatformProject()) {
                "${target}${variant.capitalized()}$CHECK_TASK_NAME"
            } else {
                "${variant}$CHECK_TASK_NAME"
            }

        // the compile task that our check task depends on
        val compileTaskDependsOn =
            if (project.isMultiplatformProject()) {
                "compile${variant.capitalized()}Kotlin${target.capitalized()}"
            } else {
                "compile${variant.capitalized()}Kotlin"
            }

        val variantTask =
            project.tasks.register<ComposeCompilerReportCheckTask>(
                name = taskName,
            ) {
                if (project.hasNonEmptyKotlinSourceSets()) {
                    outputDirectory.set(project.layout.dir(genExtension.outputDirectory))
                }
                genOutputDirectoryPath.set(genExtension.outputDirectory.get().absolutePath)
                checkOutputDirectoryPath.set(checkExtension.outputDirectory.get().absolutePath)

                multiplatformCompilationTarget.set(
                    if (project.isMultiplatformProject()) {
                        project.tasks.withType(KotlinCompile::class.java).getAt(compileTaskDependsOn).name
                    } else {
                        ""
                    },
                )
                composeCompilerCheckExtension.set(checkExtension)
                compilationVariant.set(variant)
                compilationTaskName.set(
                    // in a multiplatform project, the structure of the folder is:
                    // compose_reports/compileTask/<REPORT>
                    // but in a normal android app, it is just:
                    // compose_reports/<REPORT>
                    if (project.isMultiplatformProject()) {
                        compileTaskDependsOn
                    } else {
                        ""
                    },
                )

                hasKotlinMainSourceSet.set(
                    project.hasNonEmptyKotlinSourceSets(),
                )

                kotlinSourceSets.set(project.getKotlinSources())
                taskNameProperty.set(taskName)
            }

        // make task depend on compile kotlin task
        variantTask.configure(
            object : Action<ComposeCompilerReportCheckTask> {
                override fun execute(t: ComposeCompilerReportCheckTask) {
                    t.dependsOn(project.tasks.named(compileTaskDependsOn))
                    t.outputs.upToDateWhen {
                        checkExtension.outputDirectory.get().listFiles()?.any {
                            it.name.contains(variant.plus("-composables.txt"))
                        } ?: false
                    }
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
        val internalExtension = extensions.getByType<InternalComposeCompilerCheckExtension>()
        val genExtension = extensions.getByType<ComposeCompilerReportExtension>()
        val checkExtension = extensions.getByType<ComposeCompilerCheckExtension>()

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

        project.tasks.withType(KotlinCompile::class.java).configureEach(
            object : Action<KotlinCompile<*>> {
                override fun execute(t: KotlinCompile<*>) {
                    internalExtension.composeMultiplatformCompilationTarget.set(t.name)

                    if (gradle.startParameter.taskNames.any {
                            it.contains(CHECK_TASK_NAME)
                        }
                    ) {
                        t.kotlinOptions.freeCompilerArgs +=
                            listOf(
                                "-P",
                                REPORT_PARAM + checkExtension.outputDirectory.get().absolutePath +
                                    if (project.isMultiplatformProject()) "/${t.name}" else "",
                            )
                        t.kotlinOptions.freeCompilerArgs +=
                            listOf(
                                "-P",
                                METRIC_PARAM + checkExtension.outputDirectory.get().absolutePath +
                                    if (project.isMultiplatformProject()) "/${t.name}" else "",
                            )
                    } else if (gradle.startParameter.taskNames.any {
                            it.contains(GENERATE_TASK_NAME)
                        }
                    ) {
                        t.kotlinOptions.freeCompilerArgs +=
                            listOf(
                                "-P",
                                REPORT_PARAM + genExtension.outputDirectory.get().absolutePath +
                                    if (project.isMultiplatformProject()) "/${t.name}" else "",
                            )
                        t.kotlinOptions.freeCompilerArgs +=
                            listOf(
                                "-P",
                                METRIC_PARAM + genExtension.outputDirectory.get().absolutePath +
                                    if (project.isMultiplatformProject()) "/${t.name}" else "",
                            )
                    }
                }
            },
        )
    }

    /**
     * Registers check / generate tasks for Android / JVM / Native targets.
     */
    private fun Project.registerTasksForMultiplatformTargets() {
        val multiplatformExtension = extensions.findByType<KotlinMultiplatformExtension>()
        val androidTargets =
            multiplatformExtension?.targets?.withType(KotlinAndroidTarget::class.java)
        androidTargets?.all(
            object : Action<KotlinAndroidTarget> {
                override fun execute(kotlinAndroidTarget: KotlinAndroidTarget) {
                    kotlinAndroidTarget.compilations.all(
                        object : Action<KotlinJvmAndroidCompilation> {
                            override fun execute(kotlinJvmAndroidCompilation: KotlinJvmAndroidCompilation) {
                                registerCheckTask(
                                    variant = kotlinJvmAndroidCompilation.androidVariant.name,
                                    project = this@registerTasksForMultiplatformTargets,
                                    target = kotlinAndroidTarget.name,
                                )
                                registerGenerateTask(
                                    variant = kotlinJvmAndroidCompilation.androidVariant.name,
                                    project = this@registerTasksForMultiplatformTargets,
                                    target = kotlinAndroidTarget.name,
                                )
                            }
                        },
                    )
                }
            },
        )

        val jvmTargets =
            multiplatformExtension?.targets?.withType(KotlinJvmTarget::class.java)
        jvmTargets?.all(
            object : Action<KotlinJvmTarget> {
                override fun execute(kotlinJvmTarget: KotlinJvmTarget) {
                    registerCheckTask(
                        variant = "",
                        target = kotlinJvmTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                    )

                    registerGenerateTask(
                        target = kotlinJvmTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                        variant = "",
                    )
                }
            },
        )

        val nativeTargets =
            multiplatformExtension?.targets?.withType(KotlinNativeTarget::class.java)
        nativeTargets?.all(
            object : Action<KotlinNativeTarget> {
                override fun execute(kotlinNativeTarget: KotlinNativeTarget) {
                    registerCheckTask(
                        variant = "",
                        target = kotlinNativeTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                    )

                    registerGenerateTask(
                        target = kotlinNativeTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                        variant = "",
                    )
                }
            },
        )

        val jsTargets =
            multiplatformExtension?.targets?.withType(KotlinJsTarget::class.java)
        jsTargets?.all(
            object : Action<KotlinJsTarget> {
                override fun execute(kotlinJsTarget: KotlinJsTarget) {
                    registerCheckTask(
                        variant = "",
                        target = kotlinJsTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                    )

                    registerGenerateTask(
                        target = kotlinJsTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                        variant = "",
                    )
                }
            },
        )

        val irTargets =
            multiplatformExtension?.targets?.withType(KotlinJsIrTarget::class.java)
        irTargets?.all(
            object : Action<KotlinJsIrTarget> {
                override fun execute(kotlinJsTarget: KotlinJsIrTarget) {
                    registerCheckTask(
                        variant = "",
                        target = kotlinJsTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                    )

                    registerGenerateTask(
                        target = kotlinJsTarget.name,
                        project = this@registerTasksForMultiplatformTargets,
                        variant = "",
                    )
                }
            },
        )
    }

    /**
     * Registers check / gen task for Android Project
     */
    private fun Project.registerTasksForAndroidProject() {
        // register generate + check tasks for library modules
        extensions.findByType<LibraryAndroidComponentsExtension>()?.beforeVariants {
            registerGenerateTask(
                variant = it.name,
                project = this,
                target = "",
            )
            registerCheckTask(
                variant = it.name,
                project = this,
                target = "",
            )
        }

        // register generate + check tasks for app module
        extensions.findByType<ApplicationAndroidComponentsExtension>()?.beforeVariants {
            registerGenerateTask(
                target = "",
                project = this,
                variant = it.name,
            )
            registerCheckTask(
                target = "",
                project = this,
                variant = it.name,
            )
        }
    }

    private fun Project.isMultiplatformProject(): Boolean {
        return pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
    }

    /**
     * Checks for main / commonMain source sets that aren't empty
     */
    private fun Project.hasNonEmptyKotlinSourceSets(): Boolean {
        val kotlinSourceSet = extensions.getByType(KotlinProjectExtension::class.java).sourceSets
        val containsNonEmptyMainSourceSet =
            kotlinSourceSet.any {
                (
                    it.name == "main" ||
                        it.name == "commonMain" ||
                        it.name == "androidMain" ||
                        it.name == "source" ||
                        it.name == "jvmMain" ||
                        it.name == "jsMain" ||
                        it.name == "jsWasmMain" ||
                        it.name == "iosMain" ||
                        it.name == "wasmJsMain"
                ) &&
                    it.kotlin.isEmpty.not()
            }
        return containsNonEmptyMainSourceSet
    }

    private fun Project.getKotlinSources(): List<File> {
        val kotlinSourceSet = extensions.getByType(KotlinProjectExtension::class.java).sourceSets
        return kotlinSourceSet.flatMap {
            it.kotlin
        }
    }
}
