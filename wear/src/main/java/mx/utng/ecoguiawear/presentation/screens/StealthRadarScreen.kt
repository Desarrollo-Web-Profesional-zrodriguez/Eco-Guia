package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun StealthRadarScreen(
    state: RadarUiState,
    onToggleStealth: () -> Unit,
    onNavigateNext: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    EcoWearScaffold {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "RADAR DISCRETO",
                    style = MaterialTheme.typography.caption2,
                    color = EcoGuiaColors.Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                CircularStatus(
                    progress = 0.75f,
                    text = if (state.isStealthMode) "ON" else "OFF",
                    progressColor = if (state.isStealthMode) EcoGuiaColors.Jade else EcoGuiaColors.Muted
                )
                
                Text(
                    text = if (state.isStealthMode) "Vibración baja \n solo proximidad" else "Radar visible \n vibración normal",
                    style = MaterialTheme.typography.caption3,
                    color = EcoGuiaColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Chip(
                    label = { 
                        Text(
                            text = if (state.isStealthMode) "Desactivar" else "Activar",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    onClick = onToggleStealth,
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Chip(
                    label = { 
                        Text(
                            text = "Ajustes",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    onClick = onNavigateBack,
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun StealthRadarScreenPreview() {
    EcoGuiaWearTheme {
        StealthRadarScreen(state = RadarUiState(), onToggleStealth = {})
    }
}
