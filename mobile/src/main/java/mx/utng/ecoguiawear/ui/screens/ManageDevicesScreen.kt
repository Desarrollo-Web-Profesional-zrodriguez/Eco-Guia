/**
 * Archivo: ManageDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla para gestionar y desvincular dispositivos del ecosistema.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun ManageDevicesScreen(
    userId: String = "",
    onConfirmChanges: () -> Unit
) {
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    val userDevices = remember { mutableStateListOf<mx.utng.ecoguia.shared.domain.model.RemoteDevice>() }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    fun loadDevices() {
        coroutineScope.launch {
            isLoading = true
            try {
                if (userId.isNotBlank()) {
                    val devices = repository.getUserDevices(userId)
                    userDevices.clear()
                    userDevices.addAll(devices)
                }
            } catch (e: Exception) {
                android.util.Log.e("ManageDevices", "Error cargando dispositivos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        loadDevices()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            Column {
                Text("Dispositivos", color = Color.White, fontSize = 12.sp)
                Text("Desvincular", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { loadDevices() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Warning Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Gestión de dispositivos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Esta acción cortará la conexión en tiempo real con el dispositivo seleccionado.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 11.sp
                )
            }
        }

        // Lista de Dispositivos Editables Reales
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Sesiones y Dispositivos Conectados", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade, modifier = Modifier.size(28.dp))
                }
            } else if (userDevices.isEmpty()) {
                Text(
                    text = "No tienes dispositivos adicionales o sesiones secundarias vinculadas.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    userDevices.forEach { device ->
                        val deviceIcon = when (device.type.lowercase()) {
                            "watch", "reloj" -> Icons.Default.Watch
                            else -> Icons.Default.Tv
                        }
                        ManageDeviceItem(
                            title = device.name.ifBlank { "Dispositivo conectado" },
                            subtitle = "Estado: ${if (device.isActive) "ACTIVO" else "INACTIVO"} · ID: ${device.deviceIdentifier ?: device.id.take(8)}",
                            icon = deviceIcon,
                            actionText = "Desvincular",
                            onRemoveClick = {
                                coroutineScope.launch {
                                    try {
                                        repository.unlinkDevice(device.id)
                                        userDevices.remove(device)
                                    } catch (e: Exception) {
                                        userDevices.remove(device)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Box(modifier = Modifier.padding(16.dp)) {
            EcoButton(
                text = "Guardar y confirmar cambios",
                onClick = onConfirmChanges
            )
        }
    }
}

@Composable
fun ManageDeviceItem(
    title: String,
    subtitle: String = "Sesión activa",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String = "Quitar",
    onRemoveClick: () -> Unit = {}
) {
    var isRemoved by remember { mutableStateOf(false) }

    if (isRemoved) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(18.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = androidx.compose.ui.Modifier.clickable { 
                    isRemoved = true 
                    onRemoveClick()
                }
            ) {
                Text(
                    text = actionText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ManageDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        ManageDevicesScreen(userId = "", onConfirmChanges = {})
    }
}




