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

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.joetr.compose.guard.task.ComposeCompilerReportCheckTask
import com.joetr.compose.guard.task.ComposeCompilerReportCleanTask
import com.joetr.compose.guard.task.ComposeCompilerReportGenerateTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

public class ReportGenPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        ComposeCompilerReportExtension.create(target)
        ComposeCompilerCheckExtension.create(target)

        val android =
            runCatching {
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            }.getOrElse { error("This plugin is only applicable for Android modules") }

        val commonExtension = runCatching { target.extensions.getByType(CommonExtension::class.java) }.getOrNull()

        android.onVariants { variant ->
            target.registerComposeCompilerReportGenTaskForVariant(variant)
            target.registerComposeCompilerReportCheckTaskForVariant(variant)
        }

        target.registerComposeCompilerReportCleanTaskForVariant()

        target.afterEvaluate {
            val isComposeEnabled = commonExtension?.buildFeatures?.compose

            if (isComposeEnabled != true) {
                error("Jetpack Compose is not found enabled in this module '${target.name}'")
            }

            // When this method returns true it means gradle task for generating report is executing otherwise
            // normal compilation task is executing.
            val isFromReportGenGradleTask = target.executingComposeCompilerReportGenerationGradleTask()
            if (isFromReportGenGradleTask) {
                val kotlinAndroidExt = target.extensions.getByType<KotlinAndroidProjectExtension>()
                kotlinAndroidExt.target {
                    // Exclude for test variants, no use!
                    compilations.filter { !it.name.endsWith("Test") }.forEach {
                        it.kotlinOptions {
                            configureKotlinOptionsForComposeCompilerReport(target)
                        }
                    }
                }
            }

            val isFromCheckGradleTask = target.executingComposeCompilerCheckGradleTask()
            if (isFromCheckGradleTask) {
                val kotlinAndroidExt = target.extensions.getByType<KotlinAndroidProjectExtension>()
                kotlinAndroidExt.target {
                    // Exclude for test variants, no use!
                    compilations.filter { !it.name.endsWith("Test") }.forEach {
                        it.kotlinOptions {
                            configureKotlinOptionsForComposeCompilerCheck(target)
                        }
                    }
                }
            }
        }
    }

    private fun KotlinJvmOptions.configureKotlinOptionsForComposeCompilerReport(project: Project) {
        val reportExtension = project.extensions.getByType<ComposeCompilerReportExtension>()
        val outputPath = reportExtension.composeRawMetricsOutputDirectory.absolutePath
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$outputPath",
            )
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$outputPath",
            )
    }

    private fun KotlinJvmOptions.configureKotlinOptionsForComposeCompilerCheck(project: Project) {
        val reportExtension = project.extensions.getByType<ComposeCompilerCheckExtension>()
        val outputPath = reportExtension.composeRawMetricsOutputDirectory.absolutePath
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$outputPath",
            )
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$outputPath",
            )
    }
}

private fun Project.registerComposeCompilerReportGenTaskForVariant(variant: Variant): TaskProvider<ComposeCompilerReportGenerateTask> {
    val taskName = variant.name + "ComposeCompilerGenerate"
    val compileKotlinTaskName = compileKotlinTaskNameFromVariant(variant)
    val reportExtension = ComposeCompilerReportExtension.get(project)

    val task = tasks.register(taskName, ComposeCompilerReportGenerateTask::class.java)

    task.get().compileKotlinTasks.set(compileKotlinTaskName)
    task.get().composeRawMetricsOutputDirectory.set(reportExtension.composeRawMetricsOutputDirectory)
    task.get().outputDirectory.set(layout.dir(reportExtension.outputDirectory))

    return task
}

/**
 * Returns true if currently executing task is about generating compose compiler report
 */
private fun Project.executingComposeCompilerReportGenerationGradleTask() =
    runCatching {
        property(KEY_GOLDEN_GEN)
    }.getOrNull() == "true"

private fun Project.executingComposeCompilerCheckGradleTask() =
    runCatching {
        property(KEY_CHECK_GEN)
    }.getOrNull() == "true"

private fun Project.registerComposeCompilerReportCheckTaskForVariant(variant: Variant): TaskProvider<ComposeCompilerReportCheckTask> {
    val taskName = variant.name + "ComposeCompilerCheck"
    val compileKotlinTaskName = compileKotlinTaskNameFromVariant(variant)

    val task = tasks.register(taskName, ComposeCompilerReportCheckTask::class.java)

    val reportExtension = ComposeCompilerReportExtension.get(project)

    task.get().compileKotlinTasks.set(compileKotlinTaskName)
    task.get().outputDirectory.set(layout.dir(reportExtension.outputDirectory))

    return task
}

/**
 * Returns a task name for compile<VARIANT>Kotlin with [variant]
 */
private fun compileKotlinTaskNameFromVariant(variant: Variant): String {
    val variantName = variant.name.let { it[0].uppercaseChar() + it.substring(1) }
    return "compile${variantName}Kotlin"
}

private fun Project.registerComposeCompilerReportCleanTaskForVariant(): TaskProvider<ComposeCompilerReportCleanTask> {
    val taskName = "composeCompilerClean"

    val task = tasks.register(taskName, ComposeCompilerReportCleanTask::class.java)

    return task
}
