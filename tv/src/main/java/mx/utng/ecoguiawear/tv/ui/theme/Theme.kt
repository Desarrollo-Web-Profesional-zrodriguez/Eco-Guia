@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val SmartTVColorScheme = darkColorScheme(
    primary = DeepBlue,
    onPrimary = TextPrimary,
    secondary = BrushedGold,
    onSecondary = BackgroundDark,
    tertiary = JadeGreen,
    onTertiary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary
)

@Composable
fun EcoGuiaTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartTVColorScheme,
        content = content
    )
}