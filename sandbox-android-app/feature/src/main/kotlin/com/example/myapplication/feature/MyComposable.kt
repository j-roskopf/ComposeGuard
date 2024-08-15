package com.example.myapplication.feature

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyComposable(modifier: Modifier = Modifier, testClass: TestDataClass = TestDataClass("default"), nonDefaultParameter: Int) {
    Text(
        modifier = modifier,
        text = testClass.name.plus(nonDefaultParameter),
    )
}

data class TestDataClass(var name: String)

@Composable
fun Test(test: TestDataClass) {
    Text(test.name)
}
