package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun CompassScreen(
    state: RadarUiState,
    onNext: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val title = if (state.target.distanceMeters <= 20) "GEO-DROP CERCA" else "SIGUE LA FLECHA"
    
    EcoWearScaffold {
        item {
            ScreenHeader(
                title = title,
                subtitle = null
            )
        }
        
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CompassArrow(
                    bearingDegrees = state.target.bearingDegrees,
                    modifier = Modifier.size(110.dp)
                )
                
                Text(
                    text = "${state.target.distanceMeters} m restantes",
                    style = MaterialTheme.typography.title3,
                    color = EcoGuiaColors.Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun CompassScreenPreview() {
    EcoGuiaWearTheme {
        CompassScreen(state = RadarUiState())
    }
}
