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
import assertk.assertions.isNull
import com.joetr.compose.guard.task.infra.asserts.artifactInBuild
import com.joetr.compose.guard.task.infra.asserts.artifactInSrc
import com.joetr.compose.guard.task.infra.asserts.findArtifactInBuild
import com.joetr.compose.guard.task.infra.asserts.findArtifactInSrc
import com.joetr.compose.guard.task.infra.asserts.isType
import com.joetr.compose.guard.task.infra.asserts.succeeded
import com.joetr.compose.guard.task.infra.asserts.task
import com.joetr.compose.guard.task.infra.execute
import org.junit.Test

class ComposeCompilerReportCleanTaskTest {
    @Test
    fun `clean task deleted checked and generated reports`() {
        val project = BasicAndroidProject.getComposeProject()
        val generateTask = ":android:releaseComposeCompilerGenerate"

        // generate golden
        val generateResult = project.execute(generateTask)
        assertThat(generateResult).task(generateTask).succeeded()
        assertThat(project).artifactInSrc("android", "android_release-classes.txt", "compose_reports").isType("txt")

        // generate check
        val checkTask = ":android:releaseComposeCompilerCheck"
        val checkResult = project.execute(checkTask)
        assertThat(checkResult).task(checkTask).succeeded()
        assertThat(project).artifactInBuild("android", "compose_reports/android_release-classes.txt").isType("txt")

        // clean
        val cleanTask = ":android:composeCompilerClean"
        val cleanResult = project.execute(cleanTask)
        assertThat(cleanResult).task(cleanTask).succeeded()

        // verify check and generated reports no longer exist
        assertThat(project).findArtifactInBuild("android", "compose_reports/android_release-classes.txt").isNull()
        assertThat(project).findArtifactInSrc("android", "android_release-classes.txt", "compose_reports").isNull()
    }

    @Test
    fun `is compatible with configuration cache`() {
        val project = BasicAndroidProject.getComposeProject()
        val cleanTask = ":android:composeCompilerClean"
        val cleanResult = project.execute("--configuration-cache", cleanTask)
        assertThat(cleanResult).task(cleanTask).succeeded()
    }
}
