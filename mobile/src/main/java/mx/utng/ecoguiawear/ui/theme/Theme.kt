/**
 * Archivo: Theme.kt
 *
 * Configuración del tema [MaterialTheme] de Material Design 3 para la aplicación móvil Eco-Guía.
 * Gestiona esquemas de color dinámicos (claro/oscuro) y la jerarquía tipográfica estándar.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Esquema Oscuro ────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = EcoGuiaColors.Jade,
    secondary          = EcoGuiaColors.Gold,
    tertiary           = EcoGuiaColors.JadeLight,
    background         = EcoGuiaColors.Background,
    surface            = EcoGuiaColors.Surface,
    surfaceVariant     = EcoGuiaColors.Surface,
    onPrimary          = EcoGuiaColors.Background,
    onSecondary        = EcoGuiaColors.Background,
    onBackground       = EcoGuiaColors.Text,
    onSurface          = EcoGuiaColors.Text,
    onSurfaceVariant   = EcoGuiaColors.Muted,
)

// ── Esquema Claro ─────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = EcoGuiaColors.Jade,
    secondary          = EcoGuiaColors.Gold,
    tertiary           = EcoGuiaColors.JadeLight,
    background         = Color(0xFFF4F7F4),
    surface            = Color(0xFFFFFFFF),
    surfaceVariant     = Color(0xFFE8F5E9),
    onPrimary          = Color.White,
    onSecondary        = Color.White,
    onBackground       = Color(0xFF1A2C1F),
    onSurface          = Color(0xFF1A2C1F),
    onSurfaceVariant   = Color(0xFF4A5E52),
)

// ── Tipografía M3 (Roboto por defecto) ────────────────────────────────────────
private val EcoTypography = Typography(
    // Títulos de pantalla — usado en EcoTopBar
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Subtítulo en EcoTopBar
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Subtítulo secundario / etiquetas de sección
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Cuerpo principal
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Cuerpo secundario (subtítulos de cards)
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // Chips y etiquetas pequeñas
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
)

/**
 * Envoltorio de tema principal de la aplicación móvil Eco-Guía.
 * Aplica automáticamente la tipografía, esquemas de color y estilos de superficie.
 *
 * @param darkTheme Indica si se debe forzar el modo oscuro. Por defecto consulta la preferencia del sistema.
 * @param content Contenido composable de la interfaz de usuario.
 */
@Composable
fun EcoGuiaMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = EcoTypography,
        content     = content
    )
}
