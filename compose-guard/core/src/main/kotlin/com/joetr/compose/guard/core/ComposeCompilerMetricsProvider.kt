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
package com.joetr.compose.guard.core

import com.joetr.compose.guard.core.model.DetailedStatistics
import com.joetr.compose.guard.core.model.Item
import com.joetr.compose.guard.core.model.RowItems
import com.joetr.compose.guard.core.model.classes.ClassesReport
import com.joetr.compose.guard.core.model.composables.ComposablesReport
import com.joetr.compose.guard.core.parser.ClassReportParser
import com.joetr.compose.guard.core.parser.ComposableReportParser
import kotlinx.serialization.json.Json

/**
 * Provides metrics and reports of a Compose compiler
 */
interface ComposeCompilerMetricsProvider {
    /**
     * Returns key-value pairs from composable metrics
     */
    fun getOverallStatistics(): Map<String, Long>

    /**
     * Returns detailed statistics from composable report
     */
    fun getDetailedStatistics(): DetailedStatistics

    /**
     * Returns metrics for the composable functions.
     */
    fun getComposablesReport(): ComposablesReport

    /**
     * Returns metrics for the classes.
     */
    fun getClassesReport(): ClassesReport
}

/**
 * Default implementation for [ComposeCompilerMetricsProvider] which parses content provided by
 * [ComposeCompilerRawReportProvider].
 */
@OptIn(ExperimentalStdlibApi::class)
private class DefaultComposeCompilerMetricsProvider(
    private val contentProvider: ComposeMetricsContentProvider,
) : ComposeCompilerMetricsProvider {
    override fun getOverallStatistics(): Map<String, Long> {
        val statistics = mutableMapOf<String, Long>()
        contentProvider.briefStatisticsContents.forEach { statContent ->
            val stats = Json.decodeFromString<Map<String, Long>>(statContent)
            if (statistics.isEmpty()) {
                statistics.putAll(stats)
            } else {
                stats.forEach { (key, value) ->
                    statistics[key] = statistics[key]?.plus(value) ?: value
                }
            }
        }
        return statistics.toMap()
    }

    override fun getDetailedStatistics(): DetailedStatistics {
        val csv = contentProvider.detailedStatisticsCsvRows

        val metrics =
            if (csv.size > 1) {
                val headers = splitWithCsvSeparator(csv.first())

                csv.subList(1, csv.size)
                    .map { splitWithCsvSeparator(it) }
                    .map { items -> RowItems(items.mapIndexed { index, value -> Item(headers[index], value) }) }
            } else {
                emptyList()
            }

        return DetailedStatistics(metrics)
    }

    override fun getComposablesReport(): ComposablesReport {
        return ComposableReportParser.parse(contentProvider.composablesReportContents)
    }

    override fun getClassesReport(): ClassesReport {
        return ClassReportParser.parse(contentProvider.classesReportContents)
    }

    private fun splitWithCsvSeparator(content: String) = content.split(",").filter { it.isNotBlank() }
}

/**
 * Factory function for creating [ComposeCompilerMetricsProvider].
 */
fun ComposeCompilerMetricsProvider(files: ComposeCompilerRawReportProvider): ComposeCompilerMetricsProvider {
    val contentProvider = ComposeMetricsContentProvider(files)
    return DefaultComposeCompilerMetricsProvider(contentProvider)
}
