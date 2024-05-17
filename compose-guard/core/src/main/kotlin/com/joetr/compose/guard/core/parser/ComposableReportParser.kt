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
package com.joetr.compose.guard.core.parser

import com.joetr.compose.guard.core.exception.ParsingException
import com.joetr.compose.guard.core.mapper.ConditionMapper
import com.joetr.compose.guard.core.mapper.StabilityStatusMapper
import com.joetr.compose.guard.core.model.RawContent
import com.joetr.compose.guard.core.model.composables.ComposableDetail
import com.joetr.compose.guard.core.model.composables.ComposablesReport

/**
 * Parses [ComposablesReport] from the [String] content.
 */
public object ComposableReportParser : Parser<String, ComposablesReport> {
    private val REGEX_COMPOSABLE_FUNCTION = "(?:(.*))fun (\\w*)".toRegex()
    private val REGEX_COMPOSABLE_PARAMETERS = "(?:(stable|unstable|) (\\w*:\\s.*))".toRegex()

    /**
     * Parses all composable functions
     */
    override fun parse(content: String): ComposablesReport {
        val errors = mutableListOf<ParsingException>()

        val composables =
            getComposableFunctions(content)
                .mapNotNull { function ->
                    runCatching {
                        parseComposableDetail(function)
                    }.onFailure { cause ->
                        errors.add(ParsingException(function, cause))
                    }.getOrNull()
                }
                .toList()

        return ComposablesReport(composables, errors.toList())
    }

    internal fun getComposableFunctions(content: String): List<String> {
        val lines = content.split("\n").filter { it.isNotBlank() }

        val composableFunIndexes =
            lines.mapIndexedNotNull { index, s ->
                if (REGEX_COMPOSABLE_FUNCTION.containsMatchIn(s)) {
                    index
                } else {
                    null
                }
            }

        return composableFunIndexes.mapIndexed { index: Int, item: Int ->
            lines.subList(item, composableFunIndexes.getOrElse(index + 1) { lines.size }).joinToString(separator = "\n")
        }
    }

    /**
     * Parses unit composable
     */
    private fun parseComposableDetail(function: String): ComposableDetail {
        val composableDetails = REGEX_COMPOSABLE_FUNCTION.find(function)?.groupValues!!

        val functionDetail = composableDetails[0]
        val functionName = composableDetails[2]

        val isInline = functionDetail.contains("inline")
        val isRestartable = functionDetail.contains("restartable")
        val isSkippable = functionDetail.contains("skippable")

        val params =
            REGEX_COMPOSABLE_PARAMETERS.findAll(function).map { it.groupValues }.filter { it.isNotEmpty() }
                .map {
                    ComposableDetail.Parameter(ConditionMapper.from(it[1]), StabilityStatusMapper.from(it[0]), it[2])
                }.toList()

        return ComposableDetail(
            functionName = functionName,
            isRestartable = isRestartable,
            isSkippable = isSkippable,
            isInline = isInline,
            params = params,
            rawContent = RawContent(function),
        )
    }
}