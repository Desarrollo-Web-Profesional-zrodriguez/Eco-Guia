package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun CompassArrow(
    bearingDegrees: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        // Fondo del radar
        drawCircle(
            color = EcoGuiaColors.DeepBlue.copy(alpha = 0.5f),
            center = center,
            radius = radius
        )
        
        // Circulo exterior
        drawCircle(
            color = EcoGuiaColors.Gold.copy(alpha = 0.3f),
            center = center,
            radius = radius,
            style = Stroke(width = 1.dp.toPx())
        )
        
        // Circulo intermedio
        drawCircle(
            color = EcoGuiaColors.Gold.copy(alpha = 0.2f),
            center = center,
            radius = radius * 0.6f,
            style = Stroke(width = 0.5.dp.toPx())
        )

        // Lineas de mira (crosshair)
        val crosshairColor = EcoGuiaColors.Gold.copy(alpha = 0.2f)
        drawLine(
            color = crosshairColor,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 0.5.dp.toPx()
        )
        drawLine(
            color = crosshairColor,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 0.5.dp.toPx()
        )

        // Flecha (Brújula)
        rotate(degrees = bearingDegrees, pivot = center) {
            val arrowPath = Path().apply {
                moveTo(center.x, center.y - radius + 10.dp.toPx())
                lineTo(center.x - 14.dp.toPx(), center.y + 14.dp.toPx())
                lineTo(center.x, center.y + 4.dp.toPx())
                lineTo(center.x + 14.dp.toPx(), center.y + 14.dp.toPx())
                close()
            }
            drawPath(path = arrowPath, color = EcoGuiaColors.Gold)
        }

        // Centro
        drawCircle(
            color = EcoGuiaColors.Gold,
            center = center,
            radius = 2.dp.toPx()
        )
    }
}
