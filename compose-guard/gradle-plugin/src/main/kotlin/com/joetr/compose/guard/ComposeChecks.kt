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
import com.joetr.compose.guard.core.model.Condition
import com.joetr.compose.guard.core.model.Condition.UNSTABLE
import com.joetr.compose.guard.core.model.StabilityStatus
import com.joetr.compose.guard.core.model.composables.ComposableDetail
import org.gradle.api.GradleException
import org.gradle.api.provider.Property

internal object ComposeChecks {
    fun check(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
        composeCompilerCheckExtension: Property<ComposeCompilerCheckExtension>,
    ) {
        val errors = mutableListOf<String>()

        if (composeCompilerCheckExtension.get().errorOnNewDynamicProperties.get()) {
            errors += checkDynamicDefaultExpressions(checkedMetrics = checkedMetrics, goldenMetrics = goldenMetrics)
        }
        if (composeCompilerCheckExtension.get().errorOnNewUnstableClasses.get()) {
            errors += checkUnstableClasses(checkedMetrics = checkedMetrics, goldenMetrics = goldenMetrics)
        }
        if (composeCompilerCheckExtension.get().errorOnNewRestartableButNotSkippableComposables.get()) {
            errors += checkRestartableButNotSkippable(checkedMetrics = checkedMetrics, goldenMetrics = goldenMetrics)
        }
        if (composeCompilerCheckExtension.get().errorOnNewUnstableParams.get()) {
            errors +=
                checkUnstableParams(
                    checkedMetrics = checkedMetrics,
                    goldenMetrics = goldenMetrics,
                    ignoreUnstableParamsOnSkippableComposables =
                        composeCompilerCheckExtension
                            .get()
                            .ignoreUnstableParamsOnSkippableComposables
                            .get(),
                    assumeRuntimeStabilityAsUnstable =
                        composeCompilerCheckExtension
                            .get()
                            .assumeRuntimeStabilityAsUnstable
                            .get(),
                )
        }

        if (errors.isNotEmpty()) {
            throw GradleException(errors.joinToString("\n\n"))
        }
    }

    private fun checkDynamicDefaultExpressions(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ): List<String> {
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

        return if (checkedParametersMap.isNotEmpty()) {
            listOf(
                "New @dynamic parameters were added! \n" +
                    checkedParametersMap.keys.joinToString(separator = ",") + "\n" +
                    "More info: https://github.com/JetBrains/kotlin/blob/master/plugins/compose/design/compiler-metrics.md" +
                    "#default-parameter-expressions-that-are-dynamic",
            )
        } else {
            emptyList()
        }
    }

    private fun checkRestartableButNotSkippable(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ): List<String> {
        return if (checkedMetrics.getComposablesReport().restartableButNotSkippableComposables.size >
            goldenMetrics.getComposablesReport().restartableButNotSkippableComposables.size
        ) {
            val difference =
                checkedMetrics.getComposablesReport().restartableButNotSkippableComposables -
                    goldenMetrics.getComposablesReport().restartableButNotSkippableComposables.toSet()

            listOf(
                "New Composables were added that are restartable but not skippable! \n" +
                    difference.joinToString(",") + "\n" +
                    "More info: https://github.com/JetBrains/kotlin/blob/master/plugins/compose/design/compiler-metrics.md" +
                    "#functions-that-are-restartable-but-not-skippable",
            )
        } else {
            emptyList()
        }
    }

    private fun checkUnstableClasses(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
    ): List<String> {
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

        // we only want to alert on unstable classes used as composable parameters
        val unstableClassesUsedInComposables =
            checkedMetrics.getComposablesReport().composables.flatMap {
                it.params
            }.filter {
                checkedUnstableClassesMap.containsKey(it.type)
            }

        return if (unstableClassesUsedInComposables.isNotEmpty()) {
            listOf(
                "New unstable classes were added! \n" +
                    unstableClassesUsedInComposables.map {
                        checkedUnstableClassesMap[it.type]
                    }.joinToString(",") + "\n" +
                    "More info: https://github.com/JetBrains/kotlin/blob/master/plugins/compose/design/compiler-metrics.md" +
                    "#classes-that-are-unstable",
            )
        } else {
            emptyList()
        }
    }

    /**
     * @param ignoreUnstableParamsOnSkippableComposables - In strong skipping mode (https://developer.android.com/develop/ui/compose/performance/stability/strongskipping)
     * we don't really care about new unstable params if the composable is already skippable
     */
    private fun checkUnstableParams(
        checkedMetrics: ComposeCompilerMetricsProvider,
        goldenMetrics: ComposeCompilerMetricsProvider,
        ignoreUnstableParamsOnSkippableComposables: Boolean,
        assumeRuntimeStabilityAsUnstable: Boolean,
    ): List<String> {
        val checkedUnstableParamsMap = mutableMapOf<ComposableDetail.FunctionAndParameter, String>()
        checkedMetrics.getComposablesReport().composables.forEach { composable ->
            composable.params.filter {
                (assumeRuntimeStabilityAsUnstable && it.condition == Condition.MISSING) ||
                    (
                        it.condition == UNSTABLE &&
                            if (ignoreUnstableParamsOnSkippableComposables) {
                                composable.isSkippable.not()
                            } else {
                                true
                            }
                    )
            }.forEach { param ->
                checkedUnstableParamsMap[
                    ComposableDetail.FunctionAndParameter(
                        functionName = composable.functionName,
                        parameterName = param.name,
                        parameterType = param.type,
                    ),
                ] = param.stabilityStatus.toString()
            }
        }

        val goldenUnstableParamsMap = mutableMapOf<ComposableDetail.FunctionAndParameter, String>()
        goldenMetrics.getComposablesReport().composables.forEach { composable ->
            composable.params.filter {
                (assumeRuntimeStabilityAsUnstable && it.condition == Condition.MISSING) ||
                    (
                        it.condition == UNSTABLE &&
                            if (ignoreUnstableParamsOnSkippableComposables) {
                                composable.isSkippable.not()
                            } else {
                                true
                            }
                    )
            }.forEach { param ->
                goldenUnstableParamsMap[
                    ComposableDetail.FunctionAndParameter(
                        functionName = composable.functionName,
                        parameterName = param.name,
                        parameterType = param.type,
                    ),
                ] = param.stabilityStatus.toString()
            }
        }

        val newUnstableParamsMap = checkedUnstableParamsMap.filterKeys { it !in goldenUnstableParamsMap.keys }

        return if (newUnstableParamsMap.isNotEmpty()) {
            listOf(
                "New unstable parameters were added in the following @Composables! \n" +
                    if (assumeRuntimeStabilityAsUnstable) {
                        "Please note, since `assumeRuntimeStabilityAsUnstable` is enabled, " +
                            "Compose Guard may not correctly be able to infer" +
                            " the stability of parameters from across module boundaries \n"
                    } else {
                        ""
                    } + newUnstableParamsMap.keys.joinToString(separator = "\n") { it.toString() },
            )
        } else {
            emptyList()
        }
    }
}
