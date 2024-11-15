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
import assertk.assertions.isNotEmpty
import com.autonomousapps.kit.gradle.Plugin
import com.joetr.compose.guard.task.infra.asserts.failed
import com.joetr.compose.guard.task.infra.asserts.succeeded
import com.joetr.compose.guard.task.infra.asserts.task
import com.joetr.compose.guard.task.infra.asserts.upToDate
import com.joetr.compose.guard.task.infra.execute
import com.joetr.compose.guard.task.infra.executeAndFail
import org.junit.Test
import kotlin.io.path.readText
import kotlin.io.path.writeText

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
        ).contains("Golden metrics do not exist for variant release! Please generate them using the `releaseComposeCompilerGenerate` task")
    }

    @Test
    fun `error not thrown if running check on module with no main source set`() {
        val project =
            BasicAndroidProject.getComposeProject(
                includeEmptyModule = true,
            )
        val task = ":android-empty:releaseComposeCompilerCheck"

        val checkResult = project.execute(task)
        assertThat(checkResult).task(task).succeeded()
    }

    @Test
    fun `can detect when new restartable but not skippable composables are added`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // add new restartable but not skippable composable
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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

        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/AnotherUnstableClass.kt")
            .toFile().writeText(
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
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
            .writeText(
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

    @Test
    fun `can detect when new unstable parameters are added`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // add unstable class and dynamic property so that check doesn't fail and generate golden metrics
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
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
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
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
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult.output).contains("New unstable parameters were added in the following @Composables!")
        assertThat(checkResult.output).contains(
            "FunctionAndParameter(functionName=TestComposable, parameterName=newUnstable, parameterType=OtherUnstableClass)",
        )
        assertThat(checkResult).task(checkTask).failed()
    }

    @Test
    fun `is compatible with configuration cache`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"
        val checkTask = ":android:releaseComposeCompilerCheck"

        val generateResult = project.execute("--configuration-cache", generateTask)
        val checkResult = project.execute("--configuration-cache", checkTask)

        assertThat(generateResult).task(generateTask).succeeded()
        assertThat(checkResult).task(checkTask).succeeded()
    }

    @Test
    fun `same variant is used when running check`() {
        val project = BasicAndroidProject.getComposeProject()
        val releaseGenerateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(releaseGenerateTask)
        assertThat(generateResult).task(releaseGenerateTask).succeeded()

        // check task fails for debug because no debug golden was generated
        val debugCheckTask = ":android:debugComposeCompilerCheck"
        val checkResult = project.executeAndFail(debugCheckTask)
        assertThat(
            checkResult.output,
        ).contains("Golden metrics do not exist for variant debug! Please generate them using the `debugComposeCompilerGenerate` task")
        assertThat(checkResult).task(debugCheckTask).failed()
    }

    @Test
    fun `check task is successful if files inside build folder are deleted`() {
        val project = BasicAndroidProject.getComposeProject()
        val releaseGenerateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(releaseGenerateTask)
        assertThat(generateResult).task(releaseGenerateTask).succeeded()

        // check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // delete build folder
        val classesTxt = project.buildDir(":android").resolve("compose_reports").resolve("android_release-classes.txt")
        val composablesCsv = project.buildDir(":android").resolve("compose_reports").resolve("android_release-composables.csv")
        val composablesTxt = project.buildDir(":android").resolve("compose_reports").resolve("android_release-composables.txt")
        val modulesJson = project.buildDir(":android").resolve("compose_reports").resolve("android_release-module.json")

        assertThat(classesTxt.toFile()).exists()
        assertThat(composablesCsv.toFile()).exists()
        assertThat(composablesTxt.toFile()).exists()
        assertThat(modulesJson.toFile()).exists()

        classesTxt.toFile().delete()
        composablesCsv.toFile().delete()
        composablesTxt.toFile().delete()
        modulesJson.toFile().delete()

        assert(classesTxt.toFile().exists().not())
        assert(composablesCsv.toFile().exists().not())
        assert(composablesTxt.toFile().exists().not())
        assert(modulesJson.toFile().exists().not())

        val newCheckResult = project.execute(checkTask)
        assertThat(newCheckResult).task(checkTask).succeeded()

        assertThat(classesTxt.toFile()).exists()
        assertThat(composablesCsv.toFile()).exists()
        assertThat(composablesTxt.toFile()).exists()
        assertThat(modulesJson.toFile()).exists()
    }

    @Test
    fun `check task is successful if build folder is deleted`() {
        val project = BasicAndroidProject.getComposeProject()
        val releaseGenerateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(releaseGenerateTask)
        assertThat(generateResult).task(releaseGenerateTask).succeeded()

        // check succeeds
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // delete build folder
        val composeReportsOutput = project.buildDir(":android").resolve("compose_reports")

        assertThat(composeReportsOutput.toFile()).exists()

        composeReportsOutput.toFile().deleteRecursively()

        val newCheckResult = project.execute(checkTask)
        assertThat(newCheckResult).task(checkTask).succeeded()

        assertThat(composeReportsOutput.toFile()).exists()

        val children = composeReportsOutput.toFile().listFiles()
        require(children != null)
        assertThat(children).isNotEmpty()
    }

    @Test
    fun `ksp not invalidated with compose check output deletion`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalPluginsForAndroidSubProject =
                    listOf(
                        Plugin(
                            id = "com.google.devtools.ksp",
                            version = "1.9.22-1.0.16",
                            apply = true,
                        ),
                    ),
                additionalDependenciesForAndroidSubProject =
                    """
                    ksp("com.bennyhuo.kotlin:deepcopy-compiler-ksp:1.9.20-1.0.1")
                    implementation("com.bennyhuo.kotlin:deepcopy-runtime:1.9.20-1.0.1")
                    """.trimIndent(),
            )

        // generate golden
        val debugGenerateTask = ":android:debugComposeCompilerGenerate"
        val generateResult = project.execute("--configuration-cache", debugGenerateTask)
        assertThat(generateResult).task(debugGenerateTask).succeeded()

        // check succeeds
        val checkTask = ":android:debugComposeCompilerCheck"
        val checkResult = project.execute("--configuration-cache", checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // ksp
        val kspTask = ":android:kspDebugKotlin"
        val kspResult = project.execute("--configuration-cache", kspTask)
        assertThat(kspResult).task(kspTask).succeeded()

        // delete build folder
        val composeReportsOutput = project.buildDir(":android").resolve("compose_reports")
        assertThat(composeReportsOutput.toFile()).exists()
        composeReportsOutput.toFile().deleteRecursively()

        // ksp
        val newKspResult = project.execute("--configuration-cache", kspTask)
        assertThat(newKspResult).task(kspTask).upToDate()
    }

    @Test
    fun `up to date with subsequent reruns`() {
        val project =
            BasicAndroidProject.getComposeProject()

        // generate golden
        val debugGenerateTask = ":android:debugComposeCompilerGenerate"
        val generateResult = project.execute("--configuration-cache", debugGenerateTask)
        assertThat(generateResult).task(debugGenerateTask).succeeded()

        // check succeeds
        val checkTask = ":android:debugComposeCompilerCheck"
        val checkResult = project.execute("--configuration-cache", checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // check again
        val newCheckResult = project.execute("--configuration-cache", checkTask)
        assertThat(newCheckResult).task(checkTask).upToDate()
    }

    @Test
    fun `can clean and check multiple times`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/42

        val project =
            BasicAndroidProject.getComposeProject()

        val debugGenerateTask = ":android:debugComposeCompilerGenerate"
        val generateResult = project.execute("--build-cache", debugGenerateTask)
        assertThat(generateResult).task(debugGenerateTask).succeeded()

        val cleanTask = ":android:clean"
        val cleanResult = project.execute(cleanTask)
        assertThat(cleanResult).task(cleanTask).succeeded()

        val checkTask = ":android:debugComposeCompilerCheck"
        val checkResult = project.execute("--build-cache", checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        val newCleanResult = project.execute(cleanTask)
        assertThat(newCleanResult).task(cleanTask).succeeded()

        val newCheckResult = project.execute("--build-cache", checkTask)
        assertThat(newCheckResult).task(checkTask).succeeded()
    }

    @Test
    fun `fails after rebuilding folder from check task`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/41#issuecomment-2295743503

        val project =
            BasicAndroidProject.getComposeProject()

        val debugGenerateTask = ":android:releaseComposeCompilerGenerate"
        val generateResult = project.execute(debugGenerateTask)
        assertThat(generateResult).task(debugGenerateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
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

        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult).task(checkTask).failed()

        // delete build folder
        val composeReportsOutput = project.buildDir(":android").resolve("compose_reports")
        assertThat(composeReportsOutput.toFile()).exists()
        composeReportsOutput.toFile().deleteRecursively()

        val newCheckResult = project.executeAndFail(checkTask)
        assertThat(newCheckResult.output).contains("New unstable classes were added!")
        assertThat(newCheckResult.output).contains(
            "ClassDetail(className=OtherUnstableClass, stability=UNSTABLE, runtimeStability=UNSTABLE, fields=[Field(status=stable," +
                " details=var value: String)], rawContent=RawContent(content=unstable class OtherUnstableClass",
        )
        assertThat(newCheckResult).task(checkTask).failed()
    }

    @Test
    fun `if baseline is manually edited, check task still executes`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/41#issuecomment-2304314164

        val project =
            BasicAndroidProject.getComposeProject()

        val debugGenerateTask = ":android:releaseComposeCompilerGenerate"
        val generateResult = project.execute(debugGenerateTask)
        assertThat(generateResult).task(debugGenerateTask).succeeded()

        // modify the composable to introduce a new unstable parameter
        project.projectDir("android").resolve("src/main/kotlin/com/example/myapplication/TestComposable.kt").toFile()
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

        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.executeAndFail(checkTask)
        assertThat(checkResult).task(checkTask).failed()

        // manually edit composables txt
        val composablesTxtFile = project.projectDir(":android").resolve("compose_reports").resolve("android_release-composables.txt")
        val composableTxtContent =
            composablesTxtFile.readText().replace(
                """
                restartable scheme("[androidx.compose.ui.UiComposable]") fun TestComposable(
                  unstable newUnstable: OtherUnstableClass
                )
                """.trimIndent(),
                "",
            )
        composablesTxtFile.writeText(composableTxtContent)

        val newCheckResult = project.executeAndFail(checkTask)
        assertThat(newCheckResult).task(checkTask).failed()
    }

    @Test
    fun `can run ksp in parallel`() {
        // https://github.com/j-roskopf/ComposeGuard/issues/65

        val project =
            BasicAndroidProject.getComposeProject(
                additionalPluginsForAndroidSubProject =
                    listOf(
                        Plugin(
                            id = "com.google.devtools.ksp",
                            version = "1.9.22-1.0.16",
                            apply = true,
                        ),
                    ),
                additionalDependenciesForAndroidSubProject =
                    """
                    ksp("com.bennyhuo.kotlin:deepcopy-compiler-ksp:1.9.20-1.0.1")
                    implementation("com.bennyhuo.kotlin:deepcopy-runtime:1.9.20-1.0.1")
                    """.trimIndent(),
            )

        // generate golden
        val debugGenerateTask = ":android:debugComposeCompilerGenerate"
        val generateResult = project.execute("--configuration-cache", debugGenerateTask)
        assertThat(generateResult).task(debugGenerateTask).succeeded()

        // check succeeds
        val checkTask = ":android:debugComposeCompilerCheck"
        val checkResult = project.execute("--configuration-cache", checkTask)
        assertThat(checkResult).task(checkTask).succeeded()

        // ksp
        val kspTask = ":android:kspReleaseKotlin"
        val kspResult = project.execute("--configuration-cache", kspTask)
        assertThat(kspResult).task(kspTask).succeeded()

        val kspAndCheckResult = project.execute(checkTask, kspTask, "--rerun-tasks")
        assertThat(kspAndCheckResult).task(kspTask).succeeded()
        assertThat(kspAndCheckResult).task(checkTask).succeeded()
    }
}
