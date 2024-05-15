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
package com.autonomousapps.kit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Path

public object GradleBuilder {
    @JvmStatic
    public fun build(
        projectDir: Path,
        vararg args: String,
    ): BuildResult = build(GradleVersion.current(), projectDir.toFile(), *args)

    @JvmStatic
    public fun build(
        projectDir: File,
        vararg args: String,
    ): BuildResult = build(GradleVersion.current(), projectDir, *args)

    @JvmStatic
    public fun build(
        gradleVersion: GradleVersion,
        projectDir: Path,
        vararg args: String,
    ): BuildResult = build(gradleVersion, projectDir.toFile(), *args)

    @JvmStatic
    public fun build(
        gradleVersion: GradleVersion,
        projectDir: File,
        vararg args: String,
    ): BuildResult = runner(gradleVersion, projectDir, *args).build()

    @JvmStatic
    public fun buildAndFail(
        projectDir: Path,
        vararg args: String,
    ): BuildResult = buildAndFail(GradleVersion.current(), projectDir.toFile(), *args)

    @JvmStatic
    public fun buildAndFail(
        projectDir: File,
        vararg args: String,
    ): BuildResult = buildAndFail(GradleVersion.current(), projectDir, *args)

    @JvmStatic
    public fun buildAndFail(
        gradleVersion: GradleVersion,
        projectDir: Path,
        vararg args: String,
    ): BuildResult = buildAndFail(gradleVersion, projectDir.toFile(), *args)

    @JvmStatic
    public fun buildAndFail(
        gradleVersion: GradleVersion,
        projectDir: File,
        vararg args: String,
    ): BuildResult = runner(gradleVersion, projectDir, *args).buildAndFail()

    @JvmStatic
    public fun runner(
        gradleVersion: GradleVersion,
        projectDir: Path,
        vararg args: String,
    ): GradleRunner = runner(gradleVersion, projectDir.toFile(), *args)

    @JvmStatic
    public fun runner(
        gradleVersion: GradleVersion,
        projectDir: File,
        vararg args: String,
    ): GradleRunner =
        GradleRunner.create().apply {
            forwardOutput()
            withGradleVersion(gradleVersion.version)
            withProjectDir(projectDir)
            withArguments(args.toList() + "-s")

            // Ensure this value is true when `--debug-jvm` is passed to Gradle, and false otherwise
            val isDebugJvm = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
            withDebug(isDebugJvm)
        }
}
