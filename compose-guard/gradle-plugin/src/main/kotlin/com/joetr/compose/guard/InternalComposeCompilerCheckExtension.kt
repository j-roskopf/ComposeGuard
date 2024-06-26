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
package com.joetr.compose.guard

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

internal interface InternalComposeCompilerCheckExtension {
    val composeMultiplatformCompilationTarget: Property<String>

    companion object {
        private const val NAME = "internalComposeGuardCheck"

        /**
         * Creates an extension of type [InternalComposeCompilerCheckExtension] and returns
         */
        fun create(target: Project): InternalComposeCompilerCheckExtension =
            target.extensions.create<InternalComposeCompilerCheckExtension>(NAME).apply {
                composeMultiplatformCompilationTarget.convention("")
            }

        /**
         * Get extensions applied to the [target] project.
         */
        fun get(target: Project): InternalComposeCompilerCheckExtension =
            target.extensions.getByType<InternalComposeCompilerCheckExtension>()
    }
}
