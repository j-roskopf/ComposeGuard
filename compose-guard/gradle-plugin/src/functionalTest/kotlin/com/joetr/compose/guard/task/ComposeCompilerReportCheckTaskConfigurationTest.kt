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

import assertk.assertThat
import assertk.assertions.contains
import com.joetr.compose.guard.task.infra.Plugins
import com.joetr.compose.guard.task.infra.asserts.failed
import com.joetr.compose.guard.task.infra.asserts.succeeded
import com.joetr.compose.guard.task.infra.asserts.task
import com.joetr.compose.guard.task.infra.asserts.upToDate
import com.joetr.compose.guard.task.infra.execute
import com.joetr.compose.guard.task.infra.executeAndFail
import org.junit.Test
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ComposeCompilerReportCheckTaskConfigurationTest {
    @Test
    fun `dynamic properties configuration`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeGuardCheck {
                        errorOnNewDynamicProperties.set(false)
                    }
                    """.trimIndent(),
            )
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // add new dynamic properties class
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class Test(val name: String)

            @Composable
            // added default parameter
            fun TestComposable(test: Test = Test("default")) {
                Text(text = test.name)
            }
            """.trimIndent(),
        )

        // assert check succeeds because check for dynamic properties is turned off
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `unstable classes configuration`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeGuardCheck {
                        errorOnNewUnstableClasses.set(false)
                        errorOnNewRestartableButNotSkippableComposables.set(false)
                        errorOnNewUnstableParams.set(false)
                    }
                    """.trimIndent(),
            )
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // add new unstable class
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class Test(var name: String)
            
            // this is the new class
            data class AnotherUnstableClass(var unstable: String)

            @Composable
            fun TestComposable(test: Test) {
                Text(text = test.name)
            }
            
            @Composable
            fun AnotherTestComposable(test: AnotherUnstableClass) {
                Text(text = test.unstable)
            }
            """.trimIndent(),
        )

        // assert check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `unstable params configuration`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeGuardCheck {
                        errorOnNewUnstableParams.set(false)
                    }
                    """.trimIndent(),
            )
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // add unstable class and dynamic property so that check doesn't fail and generate golden metrics
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(firstUnstable: UnstableClass) {
                Text(text = firstUnstable.value)
            }
            """.trimIndent(),
        )
        // Generate golden metrics with the new class included
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(newUnstable: OtherUnstableClass) {
                Text(text = newUnstable.value )
                }
            """.trimIndent(),
        )

        // Assert check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `new restartable but not skippable configuration`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeGuardCheck {
                        errorOnNewUnstableParams.set(false)
                        errorOnNewRestartableButNotSkippableComposables.set(false)
                    }
                    """.trimIndent(),
            )
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // add new restartable but not skippable composable
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class Test(var name: String)

            @Composable
            fun TestComposable(test: Test) {
                Text(text = test.name)
            }
            
            @Composable
            fun NewTestComposable(test: Test) {
                Text(text = test.name)
            }
            """.trimIndent(),
        )

        // assert check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `new unstable params are still flagged when strong skipping is enabled`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeCompiler.enableStrongSkippingMode.set(true)
                    """.trimIndent(),
                kotlinVersion = Plugins.KOTLIN_VERSION_2_0_0,
            )
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // add unstable class and dynamic property so that check doesn't fail and generate golden metrics
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(firstUnstable: UnstableClass) {
                Text(text = firstUnstable.value)
            }
            """.trimIndent(),
        )
        // Generate golden metrics with the new class included
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(newUnstable: OtherUnstableClass) {
                Text(text = newUnstable.value )
                }
            """.trimIndent(),
        )

        // assert check fails
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New unstable parameters were added in the following @Composables!")
        assertThat(checkResult.output).contains(
            "FunctionAndParameter(functionName=TestComposable, parameterName=newUnstable, parameterType=OtherUnstableClass)",
        )
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `new unstable params are not flagged when strong skipping is enabled and ignore unstable params is checked`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeCompiler.enableStrongSkippingMode.set(true)
                    
                    composeGuardCheck {
                        ignoreUnstableParamsOnSkippableComposables.set(true)
                    }
                    """.trimIndent(),
                kotlinVersion = Plugins.KOTLIN_VERSION_2_0_0,
            )
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // add unstable class and dynamic property so that check doesn't fail and generate golden metrics
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(firstUnstable: UnstableClass) {
                Text(text = firstUnstable.value)
            }
            """.trimIndent(),
        )
        // Generate golden metrics with the new class included
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(newUnstable: OtherUnstableClass) {
                Text(text = newUnstable.value )
                }
            """.trimIndent(),
        )

        // assert check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `kotlin compile not configured when disabled`() {
        val composeMetricsBuildFile =
            """
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
                compilerOptions.freeCompilerArgs.addAll(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${"$"}{project.layout.projectDirectory.asFile.absolutePath.plus("/").plus("newComposeReports")}",
                )
            }
            
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
                compilerOptions.freeCompilerArgs.addAll(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${"$"}{project.layout.projectDirectory.asFile.absolutePath.plus("/").plus("newComposeReports")}",
                )
            }
            """.trimIndent()
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """                    
                    composeGuard {
                        configureKotlinTasks.set(false)
                    }
                    
                    composeGuardGenerate {
                        outputDirectory = layout.projectDirectory.dir("newComposeReports").asFile
                    }
                    
                    $composeMetricsBuildFile
                    
                    """.trimIndent(),
            )

        val compileTask = ":android:compileReleaseKotlin"
        val compileResult = project.execute(compileTask)
        assertThat(compileResult).task(compileTask).succeeded()

        val generateTask = ":android:releaseComposeCompilerGenerate"
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).upToDate()

        val composableReport =
            project.projectDir("android").resolve("newComposeReports/android_release-composables.txt").toFile()
                .readText()

        assertThat(composableReport).contains("fun TestComposable")
    }

    @Test
    fun `if no baseline, check reports all issues when reportAllOnMissingBaseline disabled`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/53

        val project =
            BasicAndroidProject.getComposeProject(
                kotlinVersion = Plugins.KOTLIN_VERSION_2_0_0,
            )

        // assert check failed because there are no metrics
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `if baseline, when reportAllOnMissingBaseline enabled baseline is still used`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/53

        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """                    
                    composeGuardCheck {
                        reportAllOnMissingBaseline.set(true)
                    }
                    """.trimIndent(),
                kotlinVersion = Plugins.KOTLIN_VERSION_2_0_0,
            )

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableParamClass(var value: String)

            @Composable
            fun TestComposable(unstableClass: UnstableParamClass) {
                Text(text = unstableClass.value )
                }
            """.trimIndent(),
        )

        val generateTask = ":android:releaseComposeCompilerGenerate"
        val generateResult = project.execute(generateTask)

        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)

        assertThat(checkResult).task(checkTask).succeeded()
        assertThat(generateResult).task(generateTask).succeeded()
    }

    @Test
    fun `if no baseline, check reports no issues when reportAllOnMissingBaseline enabled and there are no errors`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/53

        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """                    
                    composeGuardCheck {
                        reportAllOnMissingBaseline.set(true)
                    }
                    """.trimIndent(),
                kotlinVersion = Plugins.KOTLIN_VERSION_2_0_0,
            )

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class NormalClass(val value: String)

            @Composable
            fun TestComposable(normalClass: NormalClass) {
                Text(text = normalClass.value )
                }
            """.trimIndent(),
        )

        // assert check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `if no baseline, check reports all issues when reportAllOnMissingBaseline enabled`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/53

        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """                    
                    composeGuardCheck {
                        reportAllOnMissingBaseline.set(true)
                    }
                    """.trimIndent(),
                kotlinVersion = Plugins.KOTLIN_VERSION_2_0_0,
            )

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class UnstableClass(var value: String)
            data class OtherUnstableClass(var value: String)

            @Composable
            fun TestComposable(newUnstable: OtherUnstableClass = OtherUnstableClass("test")) {
                Text(text = newUnstable.value )
                }
                
            @Composable
            fun NewTestComposable(newUnstable: OtherUnstableClass) {
                Text(text = newUnstable.value )
                }
            """.trimIndent(),
        )

        // assert check fails with all checks
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New @dynamic parameters were added!")
        assertThat(checkResult.output).contains("New unstable classes were added!")
        assertThat(checkResult.output).contains("New Composables were added that are restartable but not skippable!")
        assertThat(checkResult.output).contains("New unstable parameters were added in the following @Composables!")
        assertThat(checkResult).task(checkTask).failed()
    }
}
