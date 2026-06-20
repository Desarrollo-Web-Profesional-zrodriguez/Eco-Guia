package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun CompassScreen(
    state: RadarUiState,
    onNext: () -> Unit = {}
) {
    val title = if (state.target.distanceMeters <= 20) "GEO-DROP CERCA" else "SIGUE LA FLECHA"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onNext() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.caption2,
            color = EcoGuiaColors.Gold
        )
        
        CompassArrow(
            bearingDegrees = state.target.bearingDegrees,
            modifier = Modifier.size(120.dp)
        )
        
        Text(
            text = "${state.target.distanceMeters} m restantes",
            style = MaterialTheme.typography.title3,
            color = EcoGuiaColors.Gold
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun CompassScreenPreview() {
    EcoGuiaWearTheme {
        CompassScreen(state = RadarUiState())
    }
}
