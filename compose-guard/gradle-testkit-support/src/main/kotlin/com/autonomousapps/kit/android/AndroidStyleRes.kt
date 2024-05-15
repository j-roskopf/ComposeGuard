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

public class AndroidStyleRes(public val content: String) {
    override fun toString(): String = content

    internal fun isBlank(): Boolean = content.isBlank() || this == EMPTY

    public companion object {
        @JvmStatic
        public fun of(content: String): AndroidStyleRes = AndroidStyleRes(content)

        @JvmStatic
        public val EMPTY: AndroidStyleRes =
            AndroidStyleRes(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                </resources>
                """.trimIndent(),
            )

        @JvmStatic
        public val DEFAULT: AndroidStyleRes =
            AndroidStyleRes(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                  <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
                    <item name="colorPrimary">@color/colorPrimary</item>
                    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
                    <item name="colorAccent">@color/colorAccent</item>
                  </style>
                      
                  <style name="AppTheme.NoActionBar">
                    <item name="windowActionBar">false</item>
                    <item name="windowNoTitle">true</item>
                  </style>
                      
                  <style name="AppTheme.AppBarOverlay" parent="ThemeOverlay.AppCompat.Dark.ActionBar" />
                  <style name="AppTheme.PopupOverlay" parent="ThemeOverlay.AppCompat.Light" />
                </resources>
                """.trimIndent(),
            )
    }
}
