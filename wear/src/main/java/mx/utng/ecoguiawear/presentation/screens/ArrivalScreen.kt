package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun ArrivalScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onResetDemo: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ESTÁS AQUÍ",
            style = MaterialTheme.typography.caption2,
            color = EcoGuiaColors.Gold
        )
        
        CircularStatus(
            progress = 1f,
            text = "0 m",
            progressColor = EcoGuiaColors.Jade
        )
        
        Text(
            text = "Luce el móvil para abrir la cámara AR",
            style = MaterialTheme.typography.caption3,
            color = EcoGuiaColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ArrivalScreenPreview() {
    EcoGuiaWearTheme {
        ArrivalScreen(state = RadarUiState(), onOpenPhone = {}, onResetDemo = {})
    }
}
