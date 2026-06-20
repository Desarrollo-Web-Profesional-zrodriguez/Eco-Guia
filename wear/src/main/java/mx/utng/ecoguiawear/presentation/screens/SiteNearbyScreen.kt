package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun SiteNearbyScreen(
    state: RadarUiState,
    onBackToRadar: () -> Unit
) {
    EcoWearScaffold(
        modifier = Modifier.clickable { onBackToRadar() }
    ) {
        item {
            ScreenHeader(
                title = "Sitio cercano",
                subtitle = state.target.subtitle
            )
        }
        item {
            Text(
                text = "Hay contenido cultural cercano. El reloj guia; el celular muestra detalles, IA y camara AR.",
                color = EcoGuiaColors.Text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = "${state.target.distanceMeters} m restantes",
                color = EcoGuiaColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Volver al radar") },
                onClick = onBackToRadar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
