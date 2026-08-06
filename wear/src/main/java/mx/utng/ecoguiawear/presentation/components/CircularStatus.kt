/**
 * Indicador visual circular para la representación de porcentajes y avances en Wear OS.
 *
 * Renderiza una barra perimetral curva con anillo de fondo y texto centrado, adaptado
 * a pantallas circulares AMOLED.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Componente gráfico circular para estado de avance o conexión.
 *
 * @param progress Proporción de avance entre 0.0f y 1.0f.
 * @param text Etiqueta textual mostrada en el núcleo del círculo.
 * @param modifier Modificador de diseño Compose.
 * @param progressColor Color del arco de progreso activo.
 * @param trackColor Color de la pista o guía de fondo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun CircularStatus(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier,
    progressColor: Color = EcoGuiaColors.Jade,
    trackColor: Color = EcoGuiaColors.DeepBlue
) {
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(120.dp),
            startAngle = 270f,
            indicatorColor = progressColor,
            trackColor = trackColor,
            strokeWidth = 12.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.title1,
            color = EcoGuiaColors.Text
        )
    }
}
