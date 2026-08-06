/**
 * Archivo: CampaignDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla de selecciÃ³n de dispositivos para una campaÃ±a visual.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.screens.DeviceItem
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun CampaignDevicesScreen(
    userId: String = "",
    currentUserEmail: String = "mus@ecoguia.com",
    programType: String = "gallery",
    onManageContentClick: () -> Unit
) {
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    val tvDevices = remember { mutableStateListOf<mx.utng.ecoguia.shared.domain.model.RemoteDevice>() }
    var isLoading by remember { mutableStateOf(true) }
    val selectedDeviceIds = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        try {
            isLoading = true
            val devices = repository.getUserDevices(userId)
            tvDevices.clear()
            selectedDeviceIds.clear()
            // Filtrar solo dispositivos de tipo TV o emisor
            val tvsOnly = devices.filter { it.type.lowercase().contains("tv") || it.name.lowercase().contains("tv") }
            tvDevices.addAll(tvsOnly)
            // Marcar por defecto todas las pantallas encontradas
            tvsOnly.forEach { selectedDeviceIds.add(it.id) }
        } catch (e: Exception) {
            android.util.Log.e("CampaignDevices", "Error cargando TVs: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val programTitle = when (programType) {
        "public" -> "Colección Pública (Mapa AR)"
        "ranking" -> "Ranking Semanal (Top Likes)"
        else -> "Galería Móvil (Hotel / Museo)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Pantallas Conectadas", color = Color.White, fontSize = 14.sp)
                Text(programTitle, color = EcoGuiaColors.Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Tv, null, tint = Color.White)
            }
        }

        // Card explicativa
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Smart TVs con Sesión Iniciada", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona una o más pantallas donde deseas transmitir la programación '$programTitle'. Usa las casillas para marcar las pantallas objetivo.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // TV Sessions List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Pantallas Vinculadas (${selectedDeviceIds.size}/${tvDevices.size} Seleccionadas)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade)
                }
            } else if (tvDevices.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes ninguna Smart TV vinculada actualmente. Vincula tu Smart TV ingresando su código PIN de 6 dígitos en 'Mis Dispositivos'.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tvDevices.size) { index ->
                        val tv = tvDevices[index]
                        val isChecked = selectedDeviceIds.contains(tv.id)
                        DeviceItemToggle(
                            title = tv.name,
                            subtitle = "Sesión activa · ${tv.deviceIdentifier ?: "Conectada"}",
                            isSelected = isChecked,
                            onSelect = {
                                if (isChecked) {
                                    selectedDeviceIds.remove(tv.id)
                                } else {
                                    selectedDeviceIds.add(tv.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button
        if (tvDevices.isNotEmpty()) {
            Box(modifier = Modifier.padding(24.dp)) {
                EcoButton(
                    text = "Iniciar Transmisión (${selectedDeviceIds.size} TV)",
                    onClick = {
                        coroutineScope.launch {
                            val selectedTvs = tvDevices.filter { selectedDeviceIds.contains(it.id) }
                            selectedTvs.forEach { tv ->
                                val rawIdentifier = tv.deviceIdentifier.orEmpty()
                                val pairingCode = if (rawIdentifier.contains("TV-PIN-")) {
                                    rawIdentifier.substringAfter("TV-PIN-")
                                } else {
                                    rawIdentifier
                                }
                                if (pairingCode.isNotBlank()) {
                                    repository.setTvTransmissionProgram(pairingCode, programType)
                                }
                            }
                            onManageContentClick()
                        }
                    }
                )
            }
        }

    }
}


@Composable
fun DeviceItemToggle(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, EcoGuiaColors.Jade) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tv, null, tint = if (isSelected) EcoGuiaColors.Jade else Color.Gray, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                colors = CheckboxDefaults.colors(checkedColor = EcoGuiaColors.Jade)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CampaignDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        CampaignDevicesScreen(programType = "gallery", onManageContentClick = {})
    }
}





