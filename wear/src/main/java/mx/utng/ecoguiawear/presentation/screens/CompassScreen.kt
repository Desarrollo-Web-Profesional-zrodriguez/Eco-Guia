/**
 * Pantalla de brújula digital interactiva para Wear OS.
 *
 * Presenta la aguja de navegación orientada dinámicamente según la diferencia angular entre
 * el azimut del reloj y el rumbo hacia el sitio turístico objetivo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de brújula y orientación visual.
 *
 * @param state Estado global reactivo del radar.
 * @param onNext Callback para avanzar de página.
 * @param onBack Callback para retroceder de página.
 * @param requestFocus Solicita foco rotatorio para el bisel físico si está en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun CompassScreen(
    state: RadarUiState,
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    requestFocus: Boolean = true
) {
    val title = if (state.target.distanceMeters <= 1000) "SITIO CERCA" else "SIGUE LA FLECHA"
    
    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            ScreenHeader(
                title = title,
                subtitle = null
            )
        }
        
        item {
            CompassArrow(
                bearingDegrees = state.target.bearingDegrees - state.currentHeading,
                modifier = Modifier.size(120.dp)
            )
        }
        
        item {
            Text(
                text = "${state.target.distanceMeters} m restantes",
                style = MaterialTheme.typography.titleMedium,
                color = EcoGuiaColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

/** Previsualización en el diseñador de layouts Wear OS. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun CompassScreenPreview() {
    EcoGuiaWearTheme {
        CompassScreen(state = RadarUiState())
    }
}
