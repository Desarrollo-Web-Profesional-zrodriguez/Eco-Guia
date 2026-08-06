@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Configuración del tema visual y esquema cromático Material 3 para Android TV.
 *
 * Aplica estilos globales de tipografía, formas y colores optimizados para pantallas
 * de gran escala y alta definición en televisores y dispositivos de transmisión.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * Esquema de colores oscuros base configurado según la identidad visual de Eco-Guía.
 */
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

/**
 * Composable contenedor que envuelve la jerarquía de vistas de Android TV con el tema institucional.
 *
 * @param content Contenido composable a renderizar bajo el árbol temático.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun EcoGuiaTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartTVColorScheme,
        content = content
    )
}