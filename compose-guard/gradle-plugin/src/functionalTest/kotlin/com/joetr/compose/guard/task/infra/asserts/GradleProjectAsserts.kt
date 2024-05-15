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
@file:Suppress("NOTHING_TO_INLINE")

package com.joetr.compose.guard.task.infra.asserts

import assertk.Assert
import assertk.assertions.prop
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.artifacts.BuildArtifact

inline fun Assert<GradleProject>.artifactInBuild(
    projectName: String,
    relativePathExcludingBuild: String,
): Assert<BuildArtifact> {
    return prop(name = "singleArtifact($projectName, $relativePathExcludingBuild)") {
        it.singleArtifact(projectName, relativePathExcludingBuild)
    }
}

inline fun Assert<GradleProject>.findArtifactInBuild(
    projectName: String,
    relativePathExcludingBuild: String,
): Assert<BuildArtifact?> {
    return prop(name = "singleArtifact($projectName, $relativePathExcludingBuild)") {
        it.findArtifact(projectName, relativePathExcludingBuild)
    }
}

inline fun Assert<GradleProject>.artifactInSrc(
    projectName: String,
    relativePath: String,
    srcFolder: String,
): Assert<BuildArtifact> {
    return prop(name = "singleArtifact($projectName, $relativePath)") {
        it.singleArtifact(projectName, relativePath, srcFolder)
    }
}

inline fun Assert<GradleProject>.findArtifactInSrc(
    projectName: String,
    relativePath: String,
    srcFolder: String,
): Assert<BuildArtifact?> {
    return prop(name = "singleArtifact($projectName, $relativePath)") {
        it.findArtifact(projectName, relativePath, srcFolder)
    }
}
