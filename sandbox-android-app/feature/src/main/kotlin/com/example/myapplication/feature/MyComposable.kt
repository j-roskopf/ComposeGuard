package com.example.myapplication.feature

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyComposable(modifier: Modifier, testClass: TestDataClass) {
    Text(
        modifier = modifier,
        text = testClass.name,
    )
}

data class TestDataClass(var name: String)