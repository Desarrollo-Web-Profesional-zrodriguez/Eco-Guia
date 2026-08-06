/**
 * Pantalla de configuración del motor de retroalimentación háptica en Wear OS.
 *
 * Permite alternar el estado general de las vibraciones táctiles y seleccionar entre tres niveles
 * de intensidad predefinidos ([HapticStrength.LOW], [HapticStrength.MEDIUM], [HapticStrength.HIGH]).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Pantalla de ajustes hápticos en el smartwatch.
 *
 * @param state Estado reactivo del radar.
 * @param onToggleHaptics Callback para activar o desactivar la respuesta sensorial.
 * @param onSelectStrength Callback para establecer la intensidad deseada.
 * @param onBackToRadar Callback para retornar a la vista de radar.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun HapticSettingsScreen(
    state: RadarUiState,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectStrength: (HapticStrength) -> Unit,
    onBackToRadar: () -> Unit
) {
    val settings = state.hapticSettings

    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "Hapticos",
                subtitle = if (settings.enabled) "Vibracion activa" else "Vibracion apagada"
            )
        }
        item {
            Chip(
                label = { Text(if (settings.enabled) "Apagar vibracion" else "Activar vibracion") },
                onClick = { onToggleHaptics(!settings.enabled) },
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = if (settings.enabled) EcoGuiaColors.Surface else EcoGuiaColors.Jade
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Suave") },
                onClick = { onSelectStrength(HapticStrength.LOW) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Media") },
                onClick = { onSelectStrength(HapticStrength.MEDIUM) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Alta") },
                onClick = { onSelectStrength(HapticStrength.HIGH) },
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
