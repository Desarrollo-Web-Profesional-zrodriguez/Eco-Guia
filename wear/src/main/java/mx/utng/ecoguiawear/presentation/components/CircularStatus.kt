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
