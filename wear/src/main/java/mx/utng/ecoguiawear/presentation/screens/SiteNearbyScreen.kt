/**
 * Pantalla informativa de proximidad a un punto patrimonial o sitio turístico en Wear OS.
 *
 * Ofrece detalles breves sobre el patrimonio cultural detectado e instruye al usuario a consultar
 * la app móvil para visualizar el contenido multimedia e IA generativa.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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

/**
 * Pantalla de aviso de sitio patrimonial próximo.
 *
 * @param state Estado global reactivo del radar.
 * @param onBackToRadar Callback para volver al radar.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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
