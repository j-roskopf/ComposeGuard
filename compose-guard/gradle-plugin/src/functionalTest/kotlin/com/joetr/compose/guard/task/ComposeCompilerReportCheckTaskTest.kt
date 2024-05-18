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
            
            @Composable
            fun AnotherTestComposable(test: AnotherUnstableClass) {
                Text(text = test.unstable)
            }
            """.trimIndent(),
        )

        // assert check fails with new unstable class
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New unstable classes were added!")
        assertThat(checkResult.output).contains(
            "ClassDetail(className=AnotherUnstableClass, stability=UNSTABLE, runtimeStability=UNSTABLE, " +
                "fields=[Field(status=stable, details=var unstable: String)], " +
                "rawContent=RawContent(content=unstable class AnotherUnstableClass {",
        )
        assertThat(checkResult.output).contains("stable var unstable: String")
        assertThat(checkResult.output).contains("<runtime stability> = Unstable")
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `can detect when new dynamic properties were added`() {
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

            @Composable
            // added default parameter
            fun TestComposable(test: Test = Test("default")) {
                Text(text = test.name)
            }
            """.trimIndent(),
        )

        // assert check fails with new unstable class
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New @dynamic parameters were added!")
        assertThat(
            checkResult.output,
        ).contains(
            "FunctionAndParameter(functionName=TestComposable, parameterName=test, parameterType=Test?)",
        )
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `can detect when new dynamic properties were added and old ones were deleted`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // start with 3 dynamic properties
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class Test1(var name: String)
            data class Test2(var name: String)
            data class Test3(var name: String)

            @Composable
            fun TestComposable(
                test1: Test1 = Test1("default"),
                test2: Test2 = Test2("default"),
                test3: Test3 = Test3("default"),
            ) {
                Text(text = test1.name)
                Text(text = test2.name)
                Text(text = test3.name)
            }
            """.trimIndent(),
        )

        // delete 2 and add another
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable

            data class Test1(var name: String)
            data class Test4(var name: String)

            @Composable
            fun TestComposable(
                test1: Test1 = Test1("default"),
                test4: Test4 = Test4("default"),
            ) {
                Text(text = test1.name)
                Text(text = test4.name)
            }
            """.trimIndent(),
        )

        // assert check fails with new unstable class
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New @dynamic parameters were added!")
        assertThat(
            checkResult.output,
        ).contains(
            "FunctionAndParameter(functionName=TestComposable, parameterName=test4, parameterType=Test4?)",
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

    @Test
    fun `can detect when new unstable classes were added and old ones were deleted`() {
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
            
            // this is the new class
            data class AnotherUnstableClass(var unstable: String)

            @Composable
            fun TestComposable(test: AnotherUnstableClass) {
                Text(text = test.unstable)
            }
            """.trimIndent(),
        )

        // assert check fails with new unstable class
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New unstable classes were added!")
        assertThat(checkResult.output).contains(
            "ClassDetail(className=AnotherUnstableClass, stability=UNSTABLE, runtimeStability=UNSTABLE, " +
                "fields=[Field(status=stable, details=var unstable: String)], " +
                "rawContent=RawContent(content=unstable class AnotherUnstableClass {",
        )
        assertThat(checkResult.output).contains("stable var unstable: String")
        assertThat(checkResult.output).contains("<runtime stability> = Unstable")
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `if new unstable class is added that is not used in composable, check succeeds, then when it is used, check task fails`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // add new unstable class
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/AnotherUnstableClass.kt").toFile().writeText(
            """
            package com.example.myapplication
            
            // this is the new class
            data class AnotherUnstableClass(var unstable: String)
            """.trimIndent(),
        )

        // assert check succeeds because we don't want to trigger a failure if the new class is not used in composable
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // now use the unstable class
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile().writeText(
            """
            package com.example.myapplication

            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            
            @Composable
            fun TestComposable(test: AnotherUnstableClass) {
                Text(text = test.unstable)
            }
            """.trimIndent(),
        )

        val secondaryCheckResult = project.executeAndFail(":android:releaseComposeCompilerCheck", "--rerun-tasks")
        assertThat(secondaryCheckResult.output).contains("New unstable classes were added!")
        assertThat(secondaryCheckResult.output).contains(
            "ClassDetail(className=AnotherUnstableClass, stability=UNSTABLE, runtimeStability=UNSTABLE, " +
                "fields=[Field(status=stable, details=var unstable: String)], " +
                "rawContent=RawContent(content=unstable class AnotherUnstableClass {",
        )
        assertThat(secondaryCheckResult.output).contains("stable var unstable: String")
        assertThat(secondaryCheckResult.output).contains("<runtime stability> = Unstable")
        assertThat(secondaryCheckResult).task(checkTask).failed()
    }
}
