@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text


import mx.utng.ecoguiawear.tv.ui.theme.EcoGuiaTVTheme

@Composable
fun LobbyScreen(

    onNavigateToHeatmap: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToPortal360: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }

    // Generar PIN la primera vez o recuperar el PIN persistente guardado en SharedPreferences
    val pairingCode = remember {
        val savedPin = prefs.getString("saved_pairing_code", null)
        if (savedPin != null) {
            savedPin
        } else {
            val newPin = (100000..999999).random().toString()
            prefs.edit().putString("saved_pairing_code", newPin).apply()
            newPin
        }
    }

    var loggedUser by remember { mutableStateOf<mx.utng.ecoguia.shared.domain.model.RemoteUser?>(null) }
    var assignedSite by remember { mutableStateOf<mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite?>(null) }
    var isPairingSuccess by remember { mutableStateOf(prefs.getBoolean("saved_is_paired", false)) }

    // Consultar sesión guardada o en Neon DB y escuchar comandos de transmisión vía MQTT instantáneo
    LaunchedEffect(pairingCode) {
        // Suscribirse de inmediato al canal MQTT de la TV para responder a comandos remotos en < 100ms
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            mx.utng.ecoguia.shared.data.remote.HiveMQManager.subscribeToTvCommands(pairingCode) { command ->
                when (command) {
                    "public" -> onNavigateToPortal360()
                    "ranking" -> onNavigateToHeatmap()
                    else -> onNavigateToGallery()
                }
            }
        }


        // Si ya estaba vinculada previamente, restaurar datos del usuario
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedUserEmail = prefs.getString("saved_paired_user_email", null)
        if (savedUserId != null && savedUserEmail != null) {
            loggedUser = mx.utng.ecoguia.shared.domain.model.RemoteUser(
                id = savedUserId,
                email = savedUserEmail,
                displayName = prefs.getString("saved_paired_user_name", "Usuario") ?: "Usuario",
                role = "museum_hotel"
            )
            isPairingSuccess = true
            assignedSite = repository.getSiteByOwner(savedUserId)
        }

        while (true) {
            try {
                if (!isPairingSuccess) {
                    val pairedUser = repository.getPairingStatus(pairingCode)
                    if (pairedUser != null) {
                        loggedUser = pairedUser
                        isPairingSuccess = true
                        prefs.edit()
                            .putBoolean("saved_is_paired", true)
                            .putString("saved_paired_user_id", pairedUser.id)
                            .putString("saved_paired_user_email", pairedUser.email)
                            .putString("saved_paired_user_name", pairedUser.displayName)
                            .apply()
                        assignedSite = repository.getSiteByOwner(pairedUser.id)
                    }
                } else {
                    // Verificación secundaria vía Neon DB en caso de reconexión
                    val remoteProgram = repository.getTvActiveProgram(pairingCode)
                    if (remoteProgram != null) {
                        when (remoteProgram) {
                            "public" -> onNavigateToPortal360()
                            "ranking" -> onNavigateToHeatmap()
                            else -> onNavigateToGallery()
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TVLobby", "Error en sondeo de TV: ${e.message}")
            }
            kotlinx.coroutines.delay(3000)
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = assignedSite?.name ?: "Eco-Guía Smart TV",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = assignedSite?.address ?: "Dolores Hidalgo - Museo & Cuna de la Independencia",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (isPairingSuccess && loggedUser != null) {
            // Estado de Sesión Iniciada Exitosamente (Sin botones manuales)
            Card(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = Color(0xFF064E3B))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.tv.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Smart TV Conectada!",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sesión vinculada a: ${loggedUser?.email}",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sitio Asignado: ${assignedSite?.name ?: "Cargando datos del Museo..."}",
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "Esperando comando de transmisión desde la app móvil...",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        } else {
            // Bloque de Vinculación Limpio por PIN (Sin imagen QR)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vincular Smart TV con tu cuenta Móvil",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingresa este código PIN de 6 dígitos en tu app móvil ('Mis Dispositivos -> Vincular QR') para iniciar sesión:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                // Código PIN Grande
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = pairingCode,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        letterSpacing = 6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de prueba en modo offline / sin sesión iniciada
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onNavigateToGallery) {
                    Text("Galería Móvil & Transmisión")
                }
                Button(onClick = onNavigateToPortal360) {
                    Text("Mapa 360 del Sitio")
                }
                Button(onClick = onNavigateToHeatmap) {
                    Text("Analítica y Mapa de Calor")
                }
            }
        }
    }
}


