/**
 * Archivo: Theme.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Configura el tema de Material Design 3 con la identidad visual de Eco-Guía.
 */

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

/**
 * Tema principal de la aplicación móvil.
 */
@Composable
fun EcoGuiaMobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
