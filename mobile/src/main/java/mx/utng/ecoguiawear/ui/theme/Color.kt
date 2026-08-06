/**
 * Archivo: Color.kt
 *
 * Paleta de colores y tokens de diseño oficiales del ecosistema Eco-Guía.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tokens de color y degradados institucionales de Eco-Guía.
 */
object EcoGuiaColors {
    /** Color de fondo principal oscuro. */
    val Background = Color(0xFF050B10)
    /** Color de superficie para tarjetas y contenedores elevados. */
    val Surface = Color(0xFF0E2A3F)
    /** Azul profundo usado en contrastes de fondo. */
    val DeepBlue = Color(0xFF05111A)
    /** Color dorado para acentos premium y recompensas. */
    val Gold = Color(0xFFC5A059)
    /** Verde jade característico de Eco-Guía para acciones afirmativas. */
    val Jade = Color(0xFF26A69A)
    /** Tono claro de verde jade para estados activos o destacados. */
    val JadeLight = Color(0xFF4DB6AC)
    /** Color de texto de alto contraste. */
    val Text = Color(0xFFF7FAFC)
    /** Color atenuado para texto secundario e íconos inactivos. */
    val Muted = Color(0xFFB8C6D1)
    /** Color de alerta para advertencias y llamadas a la acción importantes. */
    val Alert = Color(0xFFE4B84A)
    
    /**
     * Degradado principal para botones y acentos.
     */
    val JadeGradient = Brush.horizontalGradient(
        colors = listOf(Gold, Jade)
    )
    
    /**
     * Degradado de fondo para pantallas principales.
     */
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(DeepBlue, Background)
    )
}
