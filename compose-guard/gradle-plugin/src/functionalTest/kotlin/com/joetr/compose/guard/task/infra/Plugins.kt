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
package com.joetr.compose.guard.task.infra

import com.autonomousapps.kit.AbstractGradleProject.Companion.PLUGIN_UNDER_TEST_VERSION
import com.autonomousapps.kit.gradle.Plugin

object Plugins {
    @JvmStatic val KOTLIN_VERSION_1_9_22: String = "1.9.22"

    @JvmStatic val KOTLIN_VERSION_2_0_0: String = "2.0.0"

    @JvmStatic val ANDROID_GRADLE_PLUGIN_VERSION = "7.4.0"

    internal fun androidAppPlugin(apply: Boolean = true): Plugin {
        return Plugin(
            id = "com.android.application",
            version = if (apply.not()) ANDROID_GRADLE_PLUGIN_VERSION else null,
            apply = apply,
        )
    }

    internal fun kotlinAndroid(
        apply: Boolean = true,
        kotlinVersion: String,
    ): Plugin {
        return Plugin(
            id = "org.jetbrains.kotlin.android",
            version = if (apply.not()) kotlinVersion else null,
            apply = apply,
        )
    }

    internal fun reportGenPlugin(apply: Boolean = true): Plugin {
        return Plugin(
            id = "com.joetr.compose.guard",
            version = if (apply.not()) PLUGIN_UNDER_TEST_VERSION else null,
            apply = apply,
        )
    }

    internal fun multiplatform(
        apply: Boolean = true,
        kotlinVersion: String,
    ): Plugin {
        return Plugin(
            id = "org.jetbrains.kotlin.multiplatform",
            version = if (apply.not()) kotlinVersion else null,
            apply = apply,
        )
    }

    internal fun composePlugin(
        apply: Boolean = true,
        kotlinVersion: String,
    ): Plugin {
        return Plugin(
            id = "org.jetbrains.kotlin.plugin.compose",
            version = if (apply.not()) kotlinVersion else null,
            apply = apply,
        )
    }
}
