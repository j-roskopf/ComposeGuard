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
@file:Suppress("NOTHING_TO_INLINE")

package com.joetr.compose.guard.task.infra.asserts

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome

inline fun Assert<BuildTask>.succeeded() {
    return prop(BuildTask::getOutcome).isEqualTo(TaskOutcome.SUCCESS)
}

inline fun Assert<BuildTask>.failed() {
    return prop(BuildTask::getOutcome).isEqualTo(TaskOutcome.FAILED)
}

inline fun Assert<BuildTask>.fromCache() {
    return prop(BuildTask::getOutcome).isEqualTo(TaskOutcome.FROM_CACHE)
}

inline fun Assert<BuildTask>.noSource() {
    return prop(BuildTask::getOutcome).isEqualTo(TaskOutcome.NO_SOURCE)
}

inline fun Assert<BuildTask>.skipped() {
    return prop(BuildTask::getOutcome).isEqualTo(TaskOutcome.SKIPPED)
}

inline fun Assert<BuildTask>.upToDate() {
    return prop(BuildTask::getOutcome).isEqualTo(TaskOutcome.UP_TO_DATE)
}

inline fun Assert<BuildTask>.hasPath(expected: String) {
    return prop(BuildTask::getPath).isEqualTo(expected)
}

inline fun Assert<BuildTask>.hasMessage(expected: String) {
    return prop(BuildTask::getOutcome).isEqualTo(expected)
}
