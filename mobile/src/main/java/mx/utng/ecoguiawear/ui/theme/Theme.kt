package mx.utng.ecoguiawear.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EcoGuiaColors.Jade,
    secondary = EcoGuiaColors.Gold,
    tertiary = EcoGuiaColors.JadeLight,
    background = EcoGuiaColors.Background,
    surface = EcoGuiaColors.Surface,
    onPrimary = EcoGuiaColors.Background,
    onSecondary = EcoGuiaColors.Background,
    onBackground = EcoGuiaColors.Text,
    onSurface = EcoGuiaColors.Text,
)

@Composable
fun EcoGuiaMobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
