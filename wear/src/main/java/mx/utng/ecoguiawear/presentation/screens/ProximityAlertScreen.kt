package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun ProximityAlertScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onArrived: () -> Unit
) {
    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "Geo-Drop cerca",
                subtitle = "${state.target.distanceMeters} m restantes"
            )
        }
        item {
            Text(
                text = "Saca el telefono para activar la camara cuando llegues.",
                color = EcoGuiaColors.Text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Abrir en telefono") },
                onClick = onOpenPhone,
                colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Jade),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Ya llegue") },
                onClick = onArrived,
                colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Gold),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
