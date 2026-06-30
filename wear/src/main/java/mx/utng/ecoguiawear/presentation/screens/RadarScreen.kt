package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun RadarScreen(
    state: RadarUiState,
    onToggleRadar: () -> Unit,
    onApproachDemo: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenAlert: () -> Unit,
    onOpenArrival: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStealth: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val target = state.target

    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "Radar activo",
                subtitle = state.lastAlert
            )
        }
        item {
            CompassArrow(
                bearingDegrees = target.bearingDegrees,
                modifier = Modifier.size(108.dp)
            )
        }
        item {
            Text(
                text = target.title,
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = "${target.distanceMeters} m",
                color = EcoGuiaColors.Gold,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Ver Geo-Drop",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onOpenCompass,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Iniciar Ruta",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onOpenSummary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Ver Alertas",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onOpenAlert,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                    contentColor = EcoGuiaColors.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Ajustes",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                    contentColor = EcoGuiaColors.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RadarScreenPreview() {
    EcoGuiaWearTheme {
        RadarScreen(
            state = RadarUiState(),
            onToggleRadar = {},
            onApproachDemo = {},
            onOpenCompass = {},
            onOpenAlert = {},
            onOpenArrival = {},
            onOpenSummary = {},
            onOpenSettings = {}
        )
    }
}
