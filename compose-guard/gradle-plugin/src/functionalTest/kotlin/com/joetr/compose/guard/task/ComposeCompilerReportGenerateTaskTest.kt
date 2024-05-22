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
import com.joetr.compose.guard.task.infra.asserts.artifactInSrc
import com.joetr.compose.guard.task.infra.asserts.isType
import com.joetr.compose.guard.task.infra.asserts.succeeded
import com.joetr.compose.guard.task.infra.asserts.task
import com.joetr.compose.guard.task.infra.execute
import org.junit.Test

class ComposeCompilerReportGenerateTaskTest {
    @Test
    fun `can generate compose compiler report`() {
        val project = BasicAndroidProject.getComposeProject()
        val task = ":android:releaseComposeCompilerGenerate"

        val result = project.execute(task)

        assertThat(result).task(task).succeeded()
        assertThat(project).artifactInSrc("android", "android_release-classes.txt", "compose_reports").isType("txt")
        assertThat(project).artifactInSrc("android", "android_release-composables.csv", "compose_reports").isType("csv")
        assertThat(project).artifactInSrc("android", "android_release-composables.txt", "compose_reports").isType("txt")
        assertThat(project).artifactInSrc("android", "android_release-module.json", "compose_reports").isType("json")
    }

    @Test
    fun `can generate compose compiler report with custom directory`() {
        val project =
            BasicAndroidProject.getComposeProject(
                additionalBuildScriptForAndroidSubProject =
                    """
                    composeGuardGenerate {
                        outputDirectory = layout.projectDirectory.dir("custom_dir").asFile
                    }
                    """.trimIndent(),
            )

        val task = ":android:releaseComposeCompilerGenerate"

        val result = project.execute(task)

        assertThat(result).task(task).succeeded()
        assertThat(project).artifactInSrc("android", "android_release-classes.txt", "custom_dir").isType("txt")
        assertThat(project).artifactInSrc("android", "android_release-composables.csv", "custom_dir").isType("csv")
        assertThat(project).artifactInSrc("android", "android_release-composables.txt", "custom_dir").isType("txt")
        assertThat(project).artifactInSrc("android", "android_release-module.json", "custom_dir").isType("json")
    }

    @Test
    fun `is compatible with configuration cache`() {
        val project = BasicAndroidProject.getComposeProject()
        val task = ":android:releaseComposeCompilerGenerate"
        val result = project.execute("--configuration-cache", task)

        assertThat(result).task(task).succeeded()
        assertThat(project).artifactInSrc("android", "android_release-classes.txt", "compose_reports").isType("txt")
        assertThat(project).artifactInSrc("android", "android_release-composables.csv", "compose_reports").isType("csv")
        assertThat(project).artifactInSrc("android", "android_release-composables.txt", "compose_reports").isType("txt")
        assertThat(project).artifactInSrc("android", "android_release-module.json", "compose_reports").isType("json")
    }
}
