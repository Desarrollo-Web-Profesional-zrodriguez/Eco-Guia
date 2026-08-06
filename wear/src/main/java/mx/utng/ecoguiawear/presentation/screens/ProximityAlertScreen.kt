/**
 * Pantalla informativa de proximidad a un sitio turístico detectado en Wear OS.
 *
 * Ofrece accesos rápidos para abrir el visor AR en el teléfono móvil o marcar la llegada de forma manual.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Pantalla de aviso de proximidad a un punto de interés.
 *
 * @param state Estado global reactivo del radar.
 * @param onOpenPhone Callback para abrir la cámara de Geo-Drops en el smartphone.
 * @param onArrived Callback para marcar el punto como alcanzado.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun ProximityAlertScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onArrived: () -> Unit
) {
    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "Sitio histórico",
                subtitle = "${state.target.distanceMeters} m restantes"
            )
        }
        item {
            Text(
                text = "Dentro del rango del sitio. Abre la cámara Geo-Drop en tu móvil.",
                color = EcoGuiaColors.Text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Ver Geo-Drops del sitio") },
                onClick = onOpenPhone,
                colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Jade),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Ya llegue") },
                onClick = onArrived,
                colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Gold),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
