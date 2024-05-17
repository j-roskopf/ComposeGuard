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
package com.joetr.compose.guard.core.utils

import java.io.File
import java.io.FileNotFoundException

/**
 * Checks whether directory with [path] exists or not.
 * Else throws [FileNotFoundException].
 */
public inline fun ensureDirectory(
    directory: File,
    lazyMessage: () -> Any,
) {
    if (!directory.isDirectory) {
        val message = lazyMessage()
        throw FileNotFoundException(message.toString())
    }
}

/**
 * Checks whether [file] with exists or not. Else throws [FileNotFoundException].
 */
public inline fun ensureFileExists(
    file: File,
    lazyMessage: () -> Any,
): File {
    if (!file.exists()) {
        val message = lazyMessage()
        throw FileNotFoundException(message.toString())
    }
    return file
}

public fun cleanupDirectory(outputDirectory: File) {
    if (outputDirectory.exists()) {
        if (!outputDirectory.isDirectory) {
            throw FileNotFoundException("'$outputDirectory' is not a directory")
        }
    }

    outputDirectory.deleteRecursively()
    outputDirectory.delete()
}
