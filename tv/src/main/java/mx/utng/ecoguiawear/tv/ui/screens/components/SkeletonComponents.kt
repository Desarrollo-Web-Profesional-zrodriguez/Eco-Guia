package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Caja de esqueleto animada con shimmer (brillo deslizante).
 * Úsala para representar cualquier bloque de contenido que aún no ha cargado.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val shimmerColors = listOf(
        Color(0xFF1E293B),
        Color(0xFF334155),
        Color(0xFF475569),
        Color(0xFF334155),
        Color(0xFF1E293B)
    )

    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * Línea de texto esqueleto — simula una línea de texto en carga.
 */
@Composable
fun SkeletonTextLine(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    widthFraction: Float = 1f,
    cornerRadius: Dp = 6.dp
) {
    SkeletonBox(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        cornerRadius = cornerRadius
    )
}

/**
 * Número/métrica esqueleto: muestra "--" con estilo apagado para indicar
 * que el dato real aún no está disponible.
 */
@Composable
fun SkeletonStat(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF334155)),
        contentAlignment = Alignment.Center
    ) {
        androidx.tv.material3.Text(
            text = "--",
            color = Color(0xFF64748B),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = androidx.compose.ui.unit.TextUnit(
                    20f,
                    androidx.compose.ui.unit.TextUnitType.Sp
                ),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
    }
}

/**
 * Tarjeta imagen skeleton — simula la imagen gris de un GeoDrop sin cargar.
 */
@Composable
fun SkeletonImageCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp
) {
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        SkeletonBox(modifier = Modifier.matchParentSize())
        // Ícono de imagen apagado centrado
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.tv.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Image,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Indicador de "Sin sesión" - overlay semitransparente con mensaje.
 */
@Composable
fun NoSessionOverlay(message: String = "Vincula tu cuenta para ver el contenido") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.tv.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.tv.material3.Text(
                text = message,
                color = Color(0xFF94A3B8),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        13f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
            )
        }
    }
}
