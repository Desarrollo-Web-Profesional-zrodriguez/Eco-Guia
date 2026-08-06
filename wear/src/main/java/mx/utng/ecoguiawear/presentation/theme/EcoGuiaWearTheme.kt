/**
 * Sistema de diseño, paleta cromática y tema visual para Wear OS.
 *
 * Configura los colores de alto contraste con fondo negro profundo ([EcoGuiaColors.Background])
 * optimizados para displays circulares AMOLED y bajo consumo de energía en smartwatches.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/**
 * Tokens de color para la interfaz de usuario en Wear OS.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
object EcoGuiaColors {
    /** Color de fondo negro AMOLED para máxima eficiencia energética. */
    val Background = Color(0xFF050B10)

    /** Color de superficie para tarjetas y chips interactivos. */
    val Surface = Color(0xFF0E2A3F)

    /** Tono azul petróleo profundo para bordes y fondos de radar. */
    val DeepBlue = Color(0xFF05111A)

    /** Color dorado colonial representativo de patrimonio histórico. */
    val Gold = Color(0xFFC5A059)

    /** Tono jade ecológico para acentos de navegación y progreso. */
    val Jade = Color(0xFF26A69A)

    /** Color principal de texto en alto contraste. */
    val Text = Color(0xFFF7FAFC)

    /** Color secundario atenuado para etiquetas y distancias. */
    val Muted = Color(0xFFB8C6D1)

    /** Tono ámbar/dorado para notificaciones y alertas. */
    val Alert = Color(0xFFE4B84A)
}

/**
 * Tema principal de Wear Compose basado en Material 3 Wear.
 *
 * @param content Contenido composable envuelto en el tema de la aplicación.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun EcoGuiaWearTheme(content: @Composable () -> Unit) {
    val colorScheme = ColorScheme(
        primary = EcoGuiaColors.Jade,
        onPrimary = EcoGuiaColors.Background,
        secondary = EcoGuiaColors.Gold,
        onSecondary = EcoGuiaColors.Background,
        background = EcoGuiaColors.Background,
        onBackground = EcoGuiaColors.Text,
        surfaceContainer = EcoGuiaColors.Surface,
        onSurface = EcoGuiaColors.Text,
        error = EcoGuiaColors.Alert,
        onError = EcoGuiaColors.Background
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
