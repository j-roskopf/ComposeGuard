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
import assertk.assertions.exists
import com.joetr.compose.guard.task.infra.asserts.failed
import com.joetr.compose.guard.task.infra.asserts.succeeded
import com.joetr.compose.guard.task.infra.asserts.task
import com.joetr.compose.guard.task.infra.execute
import com.joetr.compose.guard.task.infra.executeAndFail
import org.junit.Test

class ComposeCompilerReportCheckTaskMultiplatformTest {
    @Test
    fun `multiplatform android`() {
        val project =
            BasicAndroidProject.getComposeProject(
                applyMultiplatform = true,
                additionalBuildScriptForAndroidSubProject =
                    """
                    kotlin {
                        androidTarget()
                    }
                    """.trimIndent(),
            )

        val src = project.projectDir("android").resolve("src/main").toFile()
        src.renameTo(project.projectDir("android").resolve("src/androidMain").toFile())

        // generate golden
        val generateTask = ":android:androidReleaseComposeCompilerGenerate"
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // check
        val checkTask = ":android:androidReleaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // add unstable class and dynamic property so that check doesn't fail and generate golden metrics
        project.projectDir("android").resolve("src/androidMain/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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
        val newGenerateResult = project.execute(generateTask)
        assertThat(newGenerateResult).task(generateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/androidMain/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
                """
                package com.example.myapplication

                import androidx.compose.material3.Text
                import androidx.compose.runtime.Composable

                data class UnstableClass(var value: String)
                data class OtherUnstableClass(var value: String)

                @Composable
                fun TestComposable(newUnstable: OtherUnstableClass) {
                    Text(text = newUnstable.value)
                    }
                """.trimIndent(),
            )

        // Assert check fails with new unstable parameter
        val newCheckResult = project.executeAndFail(checkTask)
        assertThat(newCheckResult.output).contains("New unstable parameters were added in the following @Composables!")
        assertThat(newCheckResult.output).contains(
            "FunctionAndParameter(functionName=TestComposable, parameterName=newUnstable, parameterType=OtherUnstableClass)",
        )
        assertThat(newCheckResult).task(checkTask).failed()
    }

    @Test
    fun `check task is successful if build folder is delete in multiplatform`() {
        val project =
            BasicAndroidProject.getComposeProject(
                applyMultiplatform = true,
                additionalBuildScriptForAndroidSubProject =
                    """
                    kotlin {
                        androidTarget()
                    }
                    """.trimIndent(),
            )

        val src = project.projectDir("android").resolve("src/main").toFile()
        src.renameTo(project.projectDir("android").resolve("src/androidMain").toFile())

        // generate golden
        val generateTask = ":android:androidReleaseComposeCompilerGenerate"
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // check
        val checkTask = ":android:androidReleaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // delete build folder
        val composeReportsOutput = project.buildDir(":android").resolve("compose_reports")
        assertThat(composeReportsOutput.toFile()).exists()
        composeReportsOutput.toFile().deleteRecursively()

        // check
        val newCheckResult = project.execute(checkTask)
        assertThat(newCheckResult).task(checkTask).succeeded()
    }
}
