package mx.utng.ecoguiawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

object EcoGuiaColors {
    val Background = Color(0xFF050B10)
    val Surface = Color(0xFF0E2A3F)
    val DeepBlue = Color(0xFF05111A)
    val Gold = Color(0xFFC5A059)
    val Jade = Color(0xFF26A69A)
    val Text = Color(0xFFF7FAFC)
    val Muted = Color(0xFFB8C6D1)
    val Alert = Color(0xFFE4B84A)
}

@Composable
fun EcoGuiaWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = Colors(
            primary = EcoGuiaColors.Jade,
            secondary = EcoGuiaColors.Gold,
            background = EcoGuiaColors.Background,
            surface = EcoGuiaColors.Surface,
            onPrimary = EcoGuiaColors.Background,
            onSecondary = EcoGuiaColors.Background,
            onBackground = EcoGuiaColors.Text,
            onSurface = EcoGuiaColors.Text
        ),
        content = content
    )
}
