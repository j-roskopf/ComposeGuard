package com.example.myapplication.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class Test(var name: String)

@Composable
fun TestComposable(test: Test) {
    Text(text = test.name)
}