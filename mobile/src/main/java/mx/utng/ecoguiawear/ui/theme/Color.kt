/**
 * Archivo: Color.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Define la paleta de colores normalizada para toda la aplicación (Mobile, Wear, TV).
 */

package mx.utng.ecoguiawear.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object EcoGuiaColors {
    val Background = Color(0xFF050B10)
    val Surface = Color(0xFF0E2A3F)
    val DeepBlue = Color(0xFF05111A)
    val Gold = Color(0xFFC5A059)
    val Jade = Color(0xFF26A69A)
    val JadeLight = Color(0xFF4DB6AC)
    val Text = Color(0xFFF7FAFC)
    val Muted = Color(0xFFB8C6D1)
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
