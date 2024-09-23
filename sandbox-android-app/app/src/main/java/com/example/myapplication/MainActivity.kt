package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.feature.MyComposable
import com.example.myapplication.feature.SomeDataClassFromAnotherModule
import com.example.myapplication.feature.TestDataClass
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android", "Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, unusedName: String, modifier: Modifier = Modifier) {
    val testClass = TestDataClass(name = name)
    MyComposable(
        modifier = modifier,
        testClass = testClass,
        nonDefaultParameter = 2,
    )
}

@Composable
fun NewComposable(someDataClassFromAnotherModule: SomeDataClassFromAnotherModule) {
    Text(someDataClassFromAnotherModule.text)
}


