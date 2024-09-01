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
 * Checks whether directory with [directory] exists or not.
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
 * Checks whether there are metrics for the given variant
 */
public fun ensureVariantsExistsInDirectory(
    directory: File,
    variant: String,
) {
    val files = directory.listFiles() ?: emptyArray()
    val variantFiles =
        files.filter {
            it.name.contains(variant)
        }.size

    if (variantFiles <= 0) {
        val message =
            if (variant.isEmpty()) {
                "Golden metrics do not exist! " +
                    "Please generate them using the `<variant>ComposeCompilerGenerate` task"
            } else {
                "Golden metrics do not exist for variant $variant! " +
                    "Please generate them using the `${variant}ComposeCompilerGenerate` task"
            }

        throw FileNotFoundException(message)
    }
}

public fun variantsExistsInDirectory(
    directory: File,
    variant: String,
): Boolean {
    val files = directory.listFiles() ?: emptyArray()
    val variantFiles =
        files.filter {
            it.name.contains(variant)
        }.size

    return variantFiles > 0
}

public inline fun ensureDirectoryIsNotEmpty(
    directory: File,
    lazyMessage: () -> Any,
) {
    if (!directory.isDirectory) {
        val message = lazyMessage()
        throw FileNotFoundException(message.toString())
    }

    if (directory.listFiles()?.isEmpty() == true) {
        val message = lazyMessage()
        throw FileNotFoundException(message.toString())
    }
}

/**
 * Checks whether [file] with exists or not. Else throws [FileNotFoundException].
 */
internal inline fun ensureFileExists(
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
