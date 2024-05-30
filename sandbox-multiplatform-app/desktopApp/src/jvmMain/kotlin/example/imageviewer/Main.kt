package example.imageviewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application
import example.imageviewer.view.ImageViewerDesktop

fun main() = application {
    ImageViewerDesktop()
}

@Composable
fun DesktopOnlyApp() {

}