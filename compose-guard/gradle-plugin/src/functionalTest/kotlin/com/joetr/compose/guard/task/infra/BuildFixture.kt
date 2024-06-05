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

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.GradleProperties
import com.autonomousapps.kit.gradle.Plugin
import org.intellij.lang.annotations.Language

private const val ANDROID_GRADLE_PLUGIN_VERSION = "7.4.0"

class BuildFixture : AbstractGradleProject() {
    fun build(
        script: String = "",
        builder: GradleProject.Builder.() -> Unit,
        kotlinVersion: String = Plugins.KOTLIN_VERSION_1_9_22,
    ): GradleProject {
        return minimumFixture(script = script, kotlinVersion = kotlinVersion).apply { builder() }
            .write()
    }

    private fun minimumFixture(
        @Language("kt") script: String = "",
        kotlinVersion: String,
    ): GradleProject.Builder {
        val plugins =
            listOf(
                Plugin("com.android.library", ANDROID_GRADLE_PLUGIN_VERSION, apply = false),
                androidAppPlugin(false),
                kotlinAndroid(apply = false, kotlinVersion = kotlinVersion),
                reportGenPlugin(false),
            ) +
                if (kotlinVersion.startsWith("2")) {
                    listOf(composePlugin(apply = false, kotlinVersion = kotlinVersion))
                } else {
                    emptyList()
                }

        val ksp = "$kotlinVersion-1.0.17"
        val moshix = "0.25.1"
        val hilt = "2.51.1"
        val root =
            newGradleProjectBuilder(dslKind = GradleProject.DslKind.KOTLIN).withRootProject {
                gradleProperties = GradleProperties.minimalAndroidProperties()
                withVersionCatalog(
                    """
        [versions]
        jdk = "17"
        kotlin = "$kotlinVersion"
        ksp = "$ksp"
        jvmTarget = "17"
        compileSdk = "34"
        targetSdk = "28"
        minSdk = "27"
        hilt = "$hilt"
        androidGradlePlugin = "$ANDROID_GRADLE_PLUGIN_VERSION"
        compose-guard-gradle-plugin = "$PLUGIN_UNDER_TEST_VERSION"
        moshix = "$moshix"

        [plugins]
        android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
        kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
        kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
        google-ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
        moshix = { id = "dev.zacsweers.moshix", version.ref = "moshix" }
        compose-guard-gradle-plugin = { id = "com.joetr.compose.guard", version.ref = "compose-guard-gradle-plugin" }
        hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt"}

        [libraries]
        desugar-jdk-libs = "com.android.tools:desugar_jdk_libs:2.0.4"
        junit = "junit:junit:4.13.2"
        assertk = "com.willowtreeapps.assertk:assertk-jvm:0.28.0"

        hilt = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
        hilt-core = { module = "com.google.dagger:hilt-core", version.ref = "hilt" }
        hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
        """
                        .trimIndent(),
                )
                withBuildScript {
                    plugins(plugins)
                    withKotlin(script = script)
                }

                withFile("local.properties", "sdk.dir=${System.getProperty("user.home")}/Library/Android/sdk")
            }

        return root
    }

    companion object {
        val REPORT_GEN_PLUGIN = reportGenPlugin()
        val ANDROID_APP_PLUGIN = androidAppPlugin()

        private fun androidAppPlugin(apply: Boolean = true): Plugin {
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

        private fun reportGenPlugin(apply: Boolean = true): Plugin {
            return Plugin(
                id = "com.joetr.compose.guard",
                version = if (apply.not()) PLUGIN_UNDER_TEST_VERSION else null,
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
}
