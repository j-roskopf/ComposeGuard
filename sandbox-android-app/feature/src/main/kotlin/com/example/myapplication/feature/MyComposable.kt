package com.example.myapplication.feature

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bennyhuo.kotlin.deepcopy.annotations.DeepCopy

@Composable
fun MyComposable(modifier: Modifier = Modifier, testClass: TestDataClass = TestDataClass("default"), nonDefaultParameter: Int) {
    Text(
        modifier = modifier,
        text = testClass.name.plus(nonDefaultParameter),
    )
}

@DeepCopy
data class TestDataClass(var name: String)

@Composable
fun Test(test: TestDataClass) {
    Text(test.name)
}

@Composable
fun Test2(test: TestDataClass) {
    Text(test.name)
}

@Composable
fun Test3(test: TestDataClass) {
    Text(test.name)
}

