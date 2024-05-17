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

import com.joetr.compose.guard.core.model.StabilityStatus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ComposeCompilerMetricsProviderTest {
    @JvmField
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()

    lateinit var reportDirectory: File

    @Before
    fun before() {
        reportDirectory = folder.newFolder("reports/compose")
        val classesTxt = folder.newFile("reports/compose/feature_release-classes.txt")
        val composableCsv = folder.newFile("reports/compose/feature_release-composables.csv")
        val composablesTxt = folder.newFile("reports/compose/feature_release-composables.txt")
        val moduleJson = folder.newFile("reports/compose/feature_release-module.json")

        classesTxt.writeText(
            """
            unstable class TestDataClass {
              stable var name: String
              <runtime stability> = Unstable
            }
            """.trimIndent(),
        )

        composableCsv.writeText(
            """
            package,name,composable,skippable,restartable,readonly,inline,isLambda,hasDefaults,defaultsGroup,groups,calls,
            com.example.myapplication.feature.MyComposable,MyComposable,1,1,1,0,0,0,1,0,2,1,
            """.trimIndent(),
        )

        composablesTxt.writeText(
            """
            restartable skippable scheme("[androidx.compose.ui.UiComposable]") fun MyComposable(
              stable modifier: Modifier? = @static Companion
              unstable testClass: TestDataClass? = @dynamic TestDataClass("default")
              unused stable nonDefaultParameter: Int
            )
            """.trimIndent(),
        )

        moduleJson.writeText(
            """
            {
             "skippableComposables": 1,
             "restartableComposables": 1,
             "readonlyComposables": 0,
             "totalComposables": 1,
             "restartGroups": 1,
             "totalGroups": 2,
             "staticArguments": 0,
             "certainArguments": 1,
             "knownStableArguments": 17,
             "knownUnstableArguments": 0,
             "unknownStableArguments": 0,
             "totalArguments": 17,
             "markedStableClasses": 0,
             "inferredStableClasses": 0,
             "inferredUnstableClasses": 1,
             "inferredUncertainClasses": 0,
             "effectivelyStableClasses": 0,
             "totalClasses": 1,
             "memoizedLambdas": 0,
             "singletonLambdas": 0,
             "singletonComposableLambdas": 0,
             "composableLambdas": 0,
             "totalLambdas": 0
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `parse dynamic parameters`() {
        val metrics =
            ComposeCompilerMetricsProvider(
                ComposeCompilerRawReportProvider.FromDirectory(
                    directory = reportDirectory,
                ),
            )

        val amountOfDynamicProperties =
            metrics.getComposablesReport().composables.flatMap {
                it.params
            }.count {
                it.stabilityStatus == StabilityStatus.DYNAMIC
            }

        val amountOfStaticProperties =
            metrics.getComposablesReport().composables.flatMap {
                it.params
            }.count {
                it.stabilityStatus == StabilityStatus.STATIC
            }

        val amountOfMissingProperties =
            metrics.getComposablesReport().composables.flatMap {
                it.params
            }.count {
                it.stabilityStatus == StabilityStatus.MISSING
            }

        val amountOfUnusedProperties =
            metrics.getComposablesReport().composables.flatMap {
                it.params
            }.count {
                it.unused
            }

        assert(amountOfDynamicProperties == 1)
        assert(amountOfStaticProperties == 1)
        assert(amountOfMissingProperties == 1)
        assert(amountOfUnusedProperties == 1)
    }
}
