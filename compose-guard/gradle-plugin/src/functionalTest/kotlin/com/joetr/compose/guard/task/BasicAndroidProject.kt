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

import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.Source
import com.autonomousapps.kit.SourceType
import com.autonomousapps.kit.gradle.android.AndroidBlock
import com.autonomousapps.kit.gradle.android.CompileOptions
import com.autonomousapps.kit.gradle.android.DefaultConfig
import com.joetr.compose.guard.task.infra.BuildFixture
import com.joetr.compose.guard.task.infra.BuildFixture.Companion.composePlugin
import com.joetr.compose.guard.task.infra.BuildFixture.Companion.kotlinAndroid
import com.joetr.compose.guard.task.infra.Plugins
import org.gradle.api.JavaVersion

object BasicAndroidProject {
    fun getComposeProject(
        additionalBuildScriptForAndroidSubProject: String = "",
        kotlinVersion: String = Plugins.KOTLIN_VERSION_1_9_22,
    ): GradleProject {
        val includeKotlinCompilerExtensionVersion = kotlinVersion.startsWith("1")
        val script =
            """
            android.buildFeatures.compose = true
            
            $additionalBuildScriptForAndroidSubProject

            dependencies {
                    implementation("androidx.core:core-ktx:1.10.1")
                    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
                    implementation("androidx.activity:activity-compose:1.7.0")
                    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
                    implementation("androidx.compose.ui:ui")
                    implementation("androidx.compose.ui:ui-graphics")
                    implementation("androidx.compose.ui:ui-tooling-preview")
                    implementation("androidx.compose.material3:material3")
            }
            
            """.trimIndent() +
                if (includeKotlinCompilerExtensionVersion) {
                    """
                    android.composeOptions.kotlinCompilerExtensionVersion = "1.5.10"
                    """.trimIndent()
                } else {
                    ""
                }

        val plugins =
            listOf(
                BuildFixture.ANDROID_APP_PLUGIN,
                kotlinAndroid(kotlinVersion = kotlinVersion),
                BuildFixture.REPORT_GEN_PLUGIN,
            ) +
                if (kotlinVersion.startsWith("2")) {
                    listOf(composePlugin(kotlinVersion = kotlinVersion))
                } else {
                    emptyList()
                }
        val project =
            BuildFixture().build(
                script =
                    """
                    buildscript {
                      repositories {
                        google()
                        mavenCentral()
                      }
                      dependencies {
                        classpath("com.android.tools.build:gradle:8.2.0")
                        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
                      }
                    }
                    """.trimIndent(),
                builder = {
                    withAndroidLibProject(
                        name = "android",
                        packageName = "com.example.myapplication",
                    ) {
                        withBuildScript {
                            plugins(plugins)
                            android =
                                AndroidBlock(
                                    namespace = "com.example.myapplication",
                                    compileSdkVersion = 34,
                                    defaultConfig =
                                        DefaultConfig(
                                            applicationId = "com.example.myapplication",
                                            minSdkVersion = 21,
                                            targetSdkVersion = 34,
                                            versionCode = 1,
                                            versionName = "1.0",
                                        ),
                                    compileOptions =
                                        CompileOptions(
                                            sourceCompatibility = JavaVersion.VERSION_17,
                                            targetCompatibility = JavaVersion.VERSION_17,
                                        ),
                                )
                            withKotlin(script)
                            sources =
                                listOf(
                                    Source(
                                        sourceType = SourceType.KOTLIN,
                                        name = "TestComposable",
                                        path = "com/example/myapplication",
                                        source =
                                            """
                                            package com.example.myapplication
                                            
                                            import androidx.compose.material3.Text
                                            import androidx.compose.runtime.Composable

                                            data class Test(var name: String)

                                            @Composable
                                            fun TestComposable(test: Test) {
                                                Text(text = test.name)
                                            }
                                            """.trimIndent(),
                                    ),
                                )
                        }
                    }
                },
                kotlinVersion = kotlinVersion,
            )

        return project
    }
}
