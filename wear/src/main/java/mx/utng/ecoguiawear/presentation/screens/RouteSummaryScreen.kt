package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun RouteSummaryScreen(
    state: RadarUiState,
    onBackToRadar: () -> Unit,
    requestFocus: Boolean = true
) {
    val summary = state.routeSummary

    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            ScreenHeader(
                title = "RUTA ACTIVA",
                subtitle = "Siguiente parada a ${state.target.distanceMeters} m"
            )
        }
        item {
            CircularStatus(
                progress = summary.visitedStops.toFloat() / summary.totalStops.toFloat(),
                text = "${summary.visitedStops}/${summary.totalStops}",
                progressColor = EcoGuiaColors.Jade
            )
        }
        item {
            Text(
                text = summary.title,
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Volver al radar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onBackToRadar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RouteSummaryScreenPreview() {
    EcoGuiaWearTheme {
        RouteSummaryScreen(state = RadarUiState(), onBackToRadar = {})
    }
}
