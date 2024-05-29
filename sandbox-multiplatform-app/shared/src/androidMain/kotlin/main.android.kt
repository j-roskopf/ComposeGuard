import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Android"

@Composable fun MainView() = AndroidOnly()

@Composable fun AndroidOnly() = App()