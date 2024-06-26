package example.imageviewer

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication
import platform.UIKit.safeAreaInsets
import androidx.compose.runtime.Composable

private val iosNotchInset = object : WindowInsets {
    override fun getTop(density: Density): Int {
        return 0
    }

    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = 0
    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = 0
    override fun getBottom(density: Density): Int = 0
}

actual fun Modifier.notchPadding(): Modifier =
    this.windowInsetsPadding(iosNotchInset)

@Composable
fun IosOnlyShared() {

}