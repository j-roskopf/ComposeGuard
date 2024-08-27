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

import com.joetr.compose.guard.core.file.ReportAndMetricsFileFinder
import com.joetr.compose.guard.core.utils.ensureDirectory
import com.joetr.compose.guard.core.utils.ensureFileExists
import java.io.File

/**
 * Provide files of compose compiler metrics and reports
 */
public sealed interface ComposeCompilerRawReportProvider {
    public val briefStatisticsJsonFiles: List<File>
    public val detailedStatisticsCsvFiles: List<File>
    public val composableReportFiles: List<File>
    public val classesReportFiles: List<File>

    public class Empty(
        override val briefStatisticsJsonFiles: List<File> = emptyList(),
        override val detailedStatisticsCsvFiles: List<File> = emptyList(),
        override val composableReportFiles: List<File> = emptyList(),
        override val classesReportFiles: List<File> = emptyList(),
    ) : ComposeCompilerRawReportProvider

    /**
     * Provides report from individual files
     */
    public class FromIndividualFiles(
        override val briefStatisticsJsonFiles: List<File>,
        override val detailedStatisticsCsvFiles: List<File>,
        override val composableReportFiles: List<File>,
        override val classesReportFiles: List<File>,
    ) : ComposeCompilerRawReportProvider {
        init {
            validateComposeCompilerRawReportProvider()
        }
    }

    /**
     * Searches for files in the given [directory] and provides report and metric files found in that directory.
     */
    public class FromDirectory(directory: File, variant: String) : ComposeCompilerRawReportProvider {
        private val finder = ReportAndMetricsFileFinder(directory)

        override val briefStatisticsJsonFiles: List<File> = finder.findBriefStatisticsJsonFileForVariant(variant = variant)
        override val detailedStatisticsCsvFiles: List<File> = finder.findDetailsStatisticsCsvFileForVariant(variant = variant)
        override val composableReportFiles: List<File> = finder.findComposablesReportTxtFileForVariant(variant = variant)
        override val classesReportFiles: List<File> = finder.findClassesReportTxtFileForVariant(variant = variant)

        init {
            ensureDirectory(directory) { "Directory '$directory' not exists" }
            validateComposeCompilerRawReportProvider()
        }
    }
}

/**
 * Validates report and metric files
 */
public fun ComposeCompilerRawReportProvider.validateComposeCompilerRawReportProvider() {
    val files =
        briefStatisticsJsonFiles +
            detailedStatisticsCsvFiles +
            composableReportFiles +
            classesReportFiles

    files.forEach { ensureFileExists(it) { "File '$it' not exists" } }
}
