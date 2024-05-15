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
package com.autonomousapps.kit.artifacts

import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.notExists

/**
 * Essentially a wrapper around [path], with the intention to provide an expanded API eventually.
 */
public class BuildArtifact(private val path: Path) {
    /**
     * The [Path] represented by this build artifact.
     */
    public val asPath: Path get() = path

    /**
     * The [File] represented by this build artifact.
     */
    public val asFile: File get() = path.toFile()

    public fun exists(): Boolean = path.exists()

    public fun notExists(): Boolean = path.notExists()

    public fun isRegularFile(): Boolean = path.isRegularFile()

    public fun isDirectory(): Boolean = path.isDirectory()

    public fun isSymbolicLink(): Boolean = path.isSymbolicLink()

    public val extension: String get() = path.extension
}

internal fun Path.toBuildArtifact(): BuildArtifact = BuildArtifact(this)
