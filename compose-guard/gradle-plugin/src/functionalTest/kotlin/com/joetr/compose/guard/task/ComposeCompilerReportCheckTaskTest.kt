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
import com.joetr.compose.guard.task.infra.asserts.failed
import com.joetr.compose.guard.task.infra.asserts.succeeded
import com.joetr.compose.guard.task.infra.asserts.task
import com.joetr.compose.guard.task.infra.execute
import com.joetr.compose.guard.task.infra.executeAndFail
import org.junit.Test

class ComposeCompilerReportCheckTaskTest {
    @Test
    fun `error thrown if no golden metrics exist`() {
        val project = BasicAndroidProject.getComposeProject()
        val task = ":android:releaseComposeCompilerCheck"

        val result = project.executeAndFail(task)

        assertThat(result).task(task).failed()

        // fails because no metrics exist generated
        assertThat(
            result.output,
        ).contains("Golden metrics do not exist! Please generate them using the <variant>ComposeCompilerGenerate task")
    }

    @Test
    fun `can detect when new restartable but not skippable composables are added`() {
        val project = BasicAndroidProject.getComposeProject()
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

        // assert check fails with new unstable class
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New Composables were added that are restartable but not skippable!")
        assertThat(checkResult.output).contains("functionName=NewTestComposable")
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `can detect when new unstable classes were added`() {
        val project = BasicAndroidProject.getComposeProject()
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
            """.trimIndent(),
        )

        // assert check fails with new unstable class
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains(
            """
            New unstable classes were added! ClassDetail(className=AnotherUnstableClass, stability=UNSTABLE, runtimeStability=UNSTABLE, fields=[Field(status=stable, details=var unstable: String)], rawContent=RawContent(content=unstable class AnotherUnstableClass {
              stable var unstable: String
              <runtime stability> = Unstable
            }))
            """.trimIndent(),
        )
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `if no change in metrics, no failures`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"
        val checkTask = ":android:releaseComposeCompilerCheck"

        val generateResult = project.execute(generateTask)
        val checkResult = project.execute(checkTask)

        assertThat(generateResult).task(generateTask).succeeded()
        assertThat(checkResult).task(checkTask).succeeded()
    }
}
