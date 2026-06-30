package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun PairingScreen(
    state: RadarUiState,
    onPairWithPhone: () -> Unit,
    onStartDemo: () -> Unit,
    onViewAlerts: () -> Unit = {}
) {
    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "ECO-GUÍA",
                subtitle = if (state.isLinkedToPhone) "Dispositivo vinculado" else "Buscando celular..."
            )
        }
        
        item {
            CircularStatus(
                progress = if (state.isLinkedToPhone) 1f else 0.3f,
                text = if (state.isLinkedToPhone) "OK" else "...",
                progressColor = if (state.isLinkedToPhone) EcoGuiaColors.Jade else EcoGuiaColors.Muted,
                modifier = Modifier.size(100.dp)
            )
        }
        
        item {
            Text(
                text = if (state.isLinkedToPhone) "Sincronización lista" else "Abre la app en tu teléfono",
                style = MaterialTheme.typography.caption3,
                color = EcoGuiaColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
        }
        
        if (!state.isLinkedToPhone) {
            item {
                Chip(
                    label = { 
                        Text(
                            "VINCULAR AHORA", 
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.button,
                            color = EcoGuiaColors.Background
                        ) 
                    },
                    onClick = onPairWithPhone,
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }
        } else {
            item {
                Chip(
                    label = { 
                        Text(
                            "INICIAR RADAR", 
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    onClick = onStartDemo,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }

        item {
            Chip(
                label = { 
                    Text(
                        "VER ALERTAS", 
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onViewAlerts,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}
