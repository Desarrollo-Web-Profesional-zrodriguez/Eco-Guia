/**
 * Archivo: LinkedDevicesScreen.kt
 *
 * Pantalla principal de gestión de dispositivos vinculados al ecosistema activo (Wear OS, Smart TV, Web).
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*


import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Pantalla composable de visualización y enlace de dispositivos periféricos del ecosistema.
 *
 * @param userId Identificador del usuario autenticado.
 * @param currentUserEmail Correo electrónico del usuario actual.
 * @param currentUserName Nombre del usuario actual.
 * @param onTVCampaignClick Callback para navegar a la difusión de campañas hacia TV.
 * @param onManageClick Callback para administrar dispositivos sincronizados.
 * @param onStatusClick Callback para revisar el estado del enlace y telemetría.
 */
@Composable
fun LinkedDevicesScreen(
    userId: String = "",
    currentUserEmail: String = "mus@ecoguia.com",
    currentUserName: String = "Usuario",
    onTVCampaignClick: () -> Unit,
    onManageClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    val userDevices = remember { mutableStateListOf<mx.utng.ecoguia.shared.domain.model.RemoteDevice>() }
    var isLoading by remember { mutableStateOf(true) }
    var showQrDialog by remember { mutableStateOf(false) }
    var pairingCodeInput by remember { mutableStateOf("") }
    var pairingMessage by remember { mutableStateOf<String?>(null) }

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
                android.util.Log.e("LinkedDevices", "Error cargando dispositivos reales: ${e.message}")
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
        EcoTopBar(
            title = "Mis Dispositivos",
            subtitle = "Sesiones Activas"
        )

        // Resumen de Sesiones del Usuario Actual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sesión de: $currentUserName", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(currentUserEmail, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showQrDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Gold)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "Vincular por QR", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vincular QR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = EcoGuiaColors.Jade.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (userDevices.isEmpty() && !isLoading) "1 sesión activa (Este Teléfono Móvil)" else "${userDevices.size + 1} sesión(es) registrada(s) en Neon PostgreSQL",
                        color = EcoGuiaColors.Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Lista de Dispositivos del Usuario
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dispositivos Vinculados",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Teléfono actual (Siempre activo por la sesión iniciada)
                    UserDeviceSessionItem(
                        title = "Teléfono Móvil (Este Dispositivo)",
                        subtitle = "Sesión activa principal · $currentUserEmail",
                        icon = Icons.Default.Phonelink,
                        isCurrentDevice = true,
                        onCloseSession = { }
                    )

                    // Dispositivos reales vinculados en Neon PostgreSQL
                    userDevices.forEach { dev ->
                        UserDeviceSessionItem(
                            title = dev.name,
                            subtitle = "Tipo: ${dev.type.uppercase()} · ${dev.deviceIdentifier ?: "Sincronizado"}",
                            icon = if (dev.type.lowercase().contains("watch") || dev.type.lowercase().contains("wear")) Icons.Default.Watch else Icons.Default.Tv,
                            isCurrentDevice = false,
                            onCloseSession = {
                                coroutineScope.launch {
                                    repository.unlinkDevice(dev.id)
                                    loadDevices()
                                }
                            }
                        )
                    }

                    if (userDevices.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tienes otros dispositivos o Smart TVs vinculadas en este momento. Toca 'Vincular QR' para sincronizar tu TV o Reloj.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Vinculación Rápida por Código QR / Pin de la TV o Reloj
    if (showQrDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val scannerOptions = remember {
            com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()
        }
        val scanner = remember {
            com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context, scannerOptions)
        }

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Vincular Dispositivo por QR / Código", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Escanea el código QR de la Smart TV con la cámara de tu teléfono o ingresa el PIN numérico de 6 dígitos:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Botón para Abrir Escáner de Cámara QR Real
                    Button(
                        onClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val rawValue = barcode.rawValue.orEmpty().trim()
                                    if (rawValue.isNotBlank()) {
                                        pairingCodeInput = rawValue
                                        coroutineScope.launch {
                                            val success = repository.pairDeviceByCode(userId, rawValue)
                                            if (success) {
                                                pairingMessage = "¡Smart TV vinculada por QR!"
                                                delay(1000)
                                                showQrDialog = false
                                                pairingCodeInput = ""
                                                pairingMessage = null
                                                loadDevices()
                                            } else {
                                                pairingMessage = "Código PIN inválido o no encontrado. Verifique la TV."
                                            }
                                        }
                                    }
                                }
                                .addOnCanceledListener {
                                    pairingMessage = "Escaneo cancelado"
                                }
                                .addOnFailureListener { e ->
                                    pairingMessage = "Error al escanear: ${e.message}"
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Gold)
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear Código QR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedTextField(
                        value = pairingCodeInput,
                        onValueChange = { pairingCodeInput = it },
                        label = { Text("O ingresa PIN de 6 dígitos (ej. 849201)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pairingMessage != null) {
                        Text(pairingMessage!!, color = EcoGuiaColors.Jade, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pairingCodeInput.isNotBlank()) {
                            coroutineScope.launch {
                                val success = repository.pairDeviceByCode(userId, pairingCodeInput.trim())
                                if (success) {
                                    pairingMessage = "¡Dispositivo vinculado con éxito!"
                                    delay(1000)
                                    showQrDialog = false
                                    pairingCodeInput = ""
                                    pairingMessage = null
                                    loadDevices()
                                } else {
                                    pairingMessage = "Código PIN inválido o no encontrado. Verifique la TV."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Jade)
                ) {
                    Text("Vincular por PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}






@Composable
fun UserDeviceSessionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCurrentDevice: Boolean,
    onCloseSession: () -> Unit
) {
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
                    .size(40.dp)
                    .background(EcoGuiaColors.Jade.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }

            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = androidx.compose.ui.Modifier.clickable { onCloseSession() }
            ) {
                Text(
                    text = if (isCurrentDevice) "Cerrar sesión" else "Desvincular",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun DeviceItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LinkedDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        LinkedDevicesScreen(
            currentUserEmail = "mus@ecoguia.com",
            currentUserName = "Museo Dolores Hidalgo",
            onTVCampaignClick = {},
            onManageClick = {},
            onStatusClick = {}
        )
    }
}

