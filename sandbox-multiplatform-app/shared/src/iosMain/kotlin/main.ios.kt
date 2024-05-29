import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController { IosOnly() }

@Composable
fun IosOnly() {
    App()
}