/**
 * Archivo: SiteOperationScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Configuración de horarios, costos y accesibilidad para el sitio (Paso 4).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.components.EcoChipGroup
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteOperationScreen(
    viewModel: SiteRegistrationViewModel,
    onFinish: () -> Unit
) {
    var hours by viewModel.hours
    var cost by viewModel.cost
    var selectedAccessibility by viewModel.selectedAccessibility

    val accessibilityOptions = listOf(
        "Rampa ♿", "Elevador 🛗", "Braille 🦯", "Lenguaje de señas 👋", "Audio guía 🎧", "Estacionamiento 🅿️"
    )

    var showTimePicker by remember { mutableStateOf(false) }
    var pickingOpeningTime by remember { mutableStateOf(true) }
    
    val timePickerState = rememberTimePickerState()

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    if (pickingOpeningTime) {
                        hours = "$formattedTime - "
                        pickingOpeningTime = false
                    } else {
                        hours += formattedTime
                        showTimePicker = false
                    }
                }) {
                    Text(if (pickingOpeningTime) "Siguiente (Cierre)" else "Confirmar")
                }
            },
            title = { Text(if (pickingOpeningTime) "Seleccionar Apertura" else "Seleccionar Cierre") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ... (Header remains the same)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Operación", color = Color.White, fontSize = 14.sp)
                Text("Horarios y acceso", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Info, null, tint = Color.White)
            }
        }

        // ... (Info Card remains the same)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Datos prácticos", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Información de utilidad necesaria del sitio de interés.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        // Form Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Horarios y acceso", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    OutlinedCard(
                        onClick = { 
                            pickingOpeningTime = true
                            showTimePicker = true 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("HORARIO", color = EcoGuiaColors.Muted, fontSize = 12.sp)
                                Text(if (hours.isEmpty()) "Seleccionar horario" else hours, color = Color.White)
                            }
                            Icon(Icons.Default.AccessTime, null, tint = EcoGuiaColors.Gold)
                        }
                    }
                }
                item {
                    EcoTextField(
                        value = cost, 
                        onValueChange = { cost = it }, 
                        label = "COSTO",
                        placeholder = "Ej. $85 o Entrada Libre"
                    )
                }
                item {
                    Text("ACCESIBILIDAD", color = EcoGuiaColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    EcoChipGroup(
                        options = accessibilityOptions,
                        selectedOptions = selectedAccessibility,
                        onOptionSelected = { option ->
                            selectedAccessibility = if (selectedAccessibility.contains(option)) {
                                selectedAccessibility - option
                            } else {
                                selectedAccessibility + option
                            }
                        }
                    )
                }
            }
        }

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Publicar datos",
                onClick = onFinish
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteOperationScreenPreview() {
    EcoGuiaMobileTheme {
        SiteOperationScreen(SiteRegistrationViewModel(), {})
    }
}

