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

import com.joetr.compose.guard.core.ComposeCompilerMetricsProvider
import com.joetr.compose.guard.core.model.StabilityStatus
import com.joetr.compose.guard.core.model.composables.ComposableDetail
import org.gradle.api.GradleException

public object ComposeChecks {
    public fun check(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ) {
        checkDynamicDefaultExpressions(checkedMetrics, goldenMetrics)
        checkUnstableClasses(checkedMetrics, goldenMetrics)
        checkSkippableButNotRestartableComposables(checkedMetrics, goldenMetrics)
    }

    private fun checkDynamicDefaultExpressions(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ) {
        val checkedParametersMap = mutableMapOf<ComposableDetail.FunctionAndParameter, String>()
        checkedMetrics.getComposablesReport().composables.forEach { composable ->
            composable.params.filter {
                it.stabilityStatus == StabilityStatus.DYNAMIC
            }.forEach { param ->
                checkedParametersMap[
                    ComposableDetail.FunctionAndParameter(
                        functionName = composable.functionName,
                        parameterName = param.name,
                        parameterType = param.type,
                    ),
                ] = param.stabilityStatus.toString()
            }
        }

        val goldenParametersMap = mutableMapOf<ComposableDetail.FunctionAndParameter, String>()
        goldenMetrics.getComposablesReport().composables.forEach { composable ->
            composable.params.filter {
                it.stabilityStatus == StabilityStatus.DYNAMIC
            }.forEach { param ->
                goldenParametersMap[
                    ComposableDetail.FunctionAndParameter(
                        functionName = composable.functionName,
                        parameterName = param.name,
                        parameterType = param.type,
                    ),
                ] = param.stabilityStatus.toString()
            }
        }

        checkedParametersMap.keys.removeAll(goldenParametersMap.keys)

        if (checkedParametersMap.isNotEmpty()) {
            throw GradleException(
                "New @dynamic parameters were added! \n" +
                    checkedParametersMap.keys.joinToString(separator = ",") + "\n" +
                    "More info: https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md" +
                    "#default-parameter-expressions-that-are-dynamic",
            )
        }
    }

    private fun checkSkippableButNotRestartableComposables(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ) {
        if (checkedMetrics.getComposablesReport().restartableButNotSkippableComposables.size >
            goldenMetrics.getComposablesReport().restartableButNotSkippableComposables.size
        ) {
            val difference =
                checkedMetrics.getComposablesReport().restartableButNotSkippableComposables -
                    goldenMetrics.getComposablesReport().restartableButNotSkippableComposables.toSet()
            throw GradleException(
                "New Composables were added that are restartable but not skippable! \n" +
                    difference.joinToString(",") + "\n" +
                    "More info: https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md" +
                    "#functions-that-are-restartable-but-not-skippable",
            )
        }
    }

    private fun checkUnstableClasses(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ) {
        val goldenUnstableClassesMap =
            goldenMetrics.getClassesReport().unstableClasses.associateBy {
                it.className
            }

        val checkedUnstableClassesMap =
            checkedMetrics.getClassesReport().unstableClasses.associateBy {
                it.className
            }.toMutableMap()

        // remove all golden values from checked
        checkedUnstableClassesMap.keys.removeAll(goldenUnstableClassesMap.keys)

        if (checkedUnstableClassesMap.keys.isNotEmpty()) {
            throw GradleException(
                "New unstable classes were added! \n" +
                    checkedUnstableClassesMap.map {
                        it.value
                    }.joinToString(",") + "\n" +
                    "More info: https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md" +
                    "#classes-that-are-unstable",
            )
        }
    }
}
