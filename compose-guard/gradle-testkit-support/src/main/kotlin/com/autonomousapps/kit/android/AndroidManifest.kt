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
package com.autonomousapps.kit.android

public class AndroidManifest(public val content: String) {
    override fun toString(): String = content

    public companion object {
        @JvmStatic
        public fun of(content: String): AndroidManifest = AndroidManifest(content)

        @JvmStatic
        public fun simpleApp(): AndroidManifest =
            AndroidManifest(
                """
      |<?xml version="1.0" encoding="utf-8"?>
      |<manifest xmlns:android="http://schemas.android.com/apk/res/android"
      |  package="com.example">
      |
      |<application
      |  android:allowBackup="false"
      |  android:label="Test app">
      |</application>
      |</manifest>
                """.trimMargin(),
            )

        @JvmStatic
        public fun app(
            application: String? = null,
            activities: List<String> = emptyList(),
        ): AndroidManifest =
            AndroidManifest(
                """
      |<?xml version="1.0" encoding="utf-8"?>
      |<manifest xmlns:android="http://schemas.android.com/apk/res/android"
      |  package="com.example">
      |
      |<application
      |  android:allowBackup="true"
      |  android:label="Test app"
      |  android:theme="@style/AppTheme"
      |  ${application?.let { "android:name=\"$it\"" } ?: ""}>
      |  ${activities.joinToString(separator = "\n") { activityBlock(it) }}
      |  </application>
      |</manifest>
                """.trimMargin(),
            )

        @JvmStatic
        public fun appWithoutPackage(application: String? = null): AndroidManifest {
            return AndroidManifest(
                """
        |<?xml version="1.0" encoding="utf-8"?>
        |<manifest xmlns:android="http://schemas.android.com/apk/res/android">
        |
        |<application
        |  android:allowBackup="true"
        |  android:label="Test app"
        |  android:theme="@style/AppTheme"
        |  ${application?.let { "android:name=\"$it\"" } ?: ""}>
        |  ${activityBlock()}
        |  </application>
        |</manifest>
                """.trimMargin(),
            )
        }

        @JvmStatic
        public fun app(application: String? = null): AndroidManifest {
            return AndroidManifest(
                """
        |<?xml version="1.0" encoding="utf-8"?>
        |<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        |  package="com.example">
        |
        |<application
        |  android:allowBackup="true"
        |  android:label="Test app"
        |  android:theme="@style/AppTheme"
        |  ${application?.let { "android:name=\"$it\"" } ?: ""}>
        |  ${activityBlock()}
        |  </application>
        |</manifest>
                """.trimMargin(),
            )
        }

        private fun activityBlock(activityName: String = "MainActivity"): String =
            """
      |  <activity
      |    android:name=".$activityName"
      |    android:label="$activityName"
      |    >
      |    <intent-filter>
      |      <action android:name="android.intent.action.MAIN" />
      |      <category android:name="android.intent.category.LAUNCHER" />
      |    </intent-filter>
      |  </activity>"""

        @JvmField
        public val DEFAULT_APP: AndroidManifest = app(null)

        @JvmStatic
        public fun defaultLib(packageName: String): AndroidManifest =
            AndroidManifest(
                """
      |<?xml version="1.0" encoding="utf-8"?>
      |<manifest xmlns:android="http://schemas.android.com/apk/res/android"
      |  package="$packageName"/>
                """.trimMargin(),
            )
    }
}
