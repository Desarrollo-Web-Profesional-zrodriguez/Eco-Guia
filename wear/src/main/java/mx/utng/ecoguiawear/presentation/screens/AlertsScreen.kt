package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun AlertsScreen(
    state: RadarUiState,
    onBack: () -> Unit
) {
    EcoWearScaffold {
        item {
            Text(
                text = "ALERTAS",
                style = MaterialTheme.typography.caption2,
                color = EcoGuiaColors.Gold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            AlertItem(icon = Icons.Default.LocationOn, text = "Geo-Drop a 18 m", iconBackground = EcoGuiaColors.Jade)
        }
        item {
            AlertItem(icon = Icons.Default.Adjust, text = "Museo a 20 m", iconBackground = EcoGuiaColors.Gold)
        }
        item {
            AlertItem(icon = Icons.Default.Star, text = "Ruta activa", iconBackground = EcoGuiaColors.Jade)
        }
    }
}

@Composable
fun AlertItem(icon: ImageVector, text: String, iconBackground: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(EcoGuiaColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(24.dp)
                .background(iconBackground, CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EcoGuiaColors.Background,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.caption2,
            color = EcoGuiaColors.Text
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun AlertsScreenPreview() {
    EcoGuiaWearTheme {
        AlertsScreen(state = RadarUiState(), onBack = {})
    }
}
