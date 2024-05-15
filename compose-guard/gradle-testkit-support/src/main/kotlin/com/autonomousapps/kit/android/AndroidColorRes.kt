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

public class AndroidColorRes
    @JvmOverloads
    constructor(
        private val colors: List<AndroidColor> = emptyList(),
    ) {
        override fun toString(): String {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n  ${colors.joinToString("\n  ")}\n</resources>"
        }

        internal fun isBlank(): Boolean = colors.isEmpty() || colors.all { it.name.isBlank() && it.value.isBlank() }

        public companion object {
            private val DEFAULT_COLORS =
                listOf(
                    AndroidColor("colorPrimaryDark", "#0568ae"),
                    AndroidColor("colorPrimary", "#009fdb"),
                    AndroidColor("colorAccent", "#009fdb"),
                )

            @JvmField
            public val DEFAULT: AndroidColorRes = AndroidColorRes(DEFAULT_COLORS)

            @JvmField
            public val EMPTY: AndroidColorRes = AndroidColorRes()
        }

        public class AndroidColor(
            public val name: String,
            public val value: String,
        ) {
            override fun toString(): String {
                return "<color name=\"$name\">$value</color>"
            }
        }
    }
