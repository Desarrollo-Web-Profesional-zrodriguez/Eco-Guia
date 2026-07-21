/**
 * Archivo: MainActivity.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Actividad principal de la aplicación móvil. Configura la navegación y los ViewModels globales.
 */

package mx.utng.ecoguiawear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase
import mx.utng.ecoguia.shared.domain.model.ConfigEntity
import mx.utng.ecoguia.shared.domain.model.AlertEntity

import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.utng.ecoguiawear.ui.screens.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val repository = EcoGuiaRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoGuiaMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { 
                                    navController.navigate("exploration") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onSignUpClick = { navController.navigate("signup") },
                                onRecoverClick = { navController.navigate("recovery") }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                viewModel = authViewModel,
                                onSignUpSuccess = { /* Podría navegar directamente */ },
                                onBackToLogin = { navController.popBackStack() }
                            )
                        }
                        composable("recovery") {
                            RecoveryScreen(
                                onSendClick = { navController.popBackStack() },
                                onBackToLogin = { navController.popBackStack() }
                            )
                        }
                        composable("exploration") {
                            ExplorationScreen(
                                onAdminClick = { navController.navigate("admin") }
                            )
                        }
                        composable("admin") {
                            ControlPanel(this@MainActivity, repository)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Panel de control para simulación y pruebas de administración.
 */
@Composable
fun ControlPanel(activity: ComponentActivity, repository: EcoGuiaRepositoryImpl) {
    val scope = rememberCoroutineScope()
    var lastMessage by remember { mutableStateOf("Esperando interacción...") }
    
    // CRUD Geo-Drops State
    var geoDropTitle by remember { mutableStateOf("") }
    var geoDropDesc by remember { mutableStateOf("") }
    var geoDrops by remember { mutableStateOf(emptyList<RemoteGeoDrop>()) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Función para recargar datos
    val refreshGeoDrops = {
        scope.launch {
            isRefreshing = true
            try {
                geoDrops = repository.getGeoDrops()
                lastMessage = "Cápsulas actualizadas: ${geoDrops.size}"
            } catch (e: Exception) {
                lastMessage = "Error al cargar: ${e.message}"
            } finally {
                isRefreshing = false
            }
        }
    }

    // Carga inicial
    LaunchedEffect(Unit) {
        refreshGeoDrops()
    }

    // Estados locales sincronizados con UI
    var isPhoneConnected by remember { mutableStateOf(false) }
    var gpsEnabled by remember { mutableStateOf(true) }
    var cameraReady by remember { mutableStateOf(true) }

    val db = remember { EcoGuiaDatabase.getDatabase(activity) }
    val stealthModeState = db.dao().getConfigFlow("stealth_mode").collectAsState(initial = null)
    val alertsState = db.dao().getAllAlerts().collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Eco-Guía: Panel Admin", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Estados en tiempo real:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Conexión Reloj:", style = MaterialTheme.typography.bodySmall)
                    Text(if (isPhoneConnected) "CONECTADO" else "DESCONECTADO", 
                        color = if (isPhoneConnected) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GPS Preciso:", style = MaterialTheme.typography.bodySmall)
                    Text(if (gpsEnabled) "ACTIVO" else "INACTIVO", 
                        color = if (gpsEnabled) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cámara Móvil:", style = MaterialTheme.typography.bodySmall)
                    Text(if (cameraReady) "LISTA" else "ERROR/OFF", 
                        color = if (cameraReady) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Stealth Mode (DB): ${if (stealthModeState.value?.value == "1") "ON" else "OFF"}",
                    color = if (stealthModeState.value?.value == "1") Color(0xFFC5A059) else Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Log: $lastMessage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            item {
                Text("Nuevo Registro (Neon PostgreSQL)", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00E676))
                OutlinedTextField(
                    value = geoDropTitle,
                    onValueChange = { geoDropTitle = it },
                    label = { Text("Título de la Cápsula") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = geoDropDesc,
                    onValueChange = { geoDropDesc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (geoDropTitle.isNotBlank()) {
                            scope.launch {
                                lastMessage = "Registrando en Neon..."
                                val success = repository.createGeoDrop(
                                    title = geoDropTitle,
                                    description = geoDropDesc,
                                    lat = 21.1561, // Dolores Hidalgo
                                    lng = -100.9350
                                )
                                if (success) {
                                    lastMessage = "¡Registrado con éxito!"
                                    geoDropTitle = ""
                                    geoDropDesc = ""
                                    refreshGeoDrops()
                                } else {
                                    lastMessage = "Error al registrar."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Guardar en Nube")
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cápsulas en Nube:", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { refreshGeoDrops() }) {
                        Text(if (isRefreshing) "Cargando..." else "Refrescar")
                    }
                }
            }

            items(geoDrops.size) { index ->
                val drop = geoDrops[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(drop.title, style = MaterialTheme.typography.titleSmall)
                        Text(drop.description ?: "", style = MaterialTheme.typography.bodySmall)
                        Text("Fecha: ${drop.createdAt?.take(10) ?: "---"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Text("Gestión de Conexión", style = MaterialTheme.typography.titleSmall)
                Button(
                    onClick = { 
                        isPhoneConnected = !isPhoneConnected
                        scope.launch { 
                            lastMessage = "Conexión: $isPhoneConnected"
                            sendMessage(activity, "/eco-guia/simulate/link", isPhoneConnected.toString()) 
                        } 
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPhoneConnected) Color.Red else Color(0xFF2E7D32)
                    )
                ) { 
                    Text(if (isPhoneConnected) "Desconectar Reloj" else "Vincular con Reloj") 
                }
            }

            item {
                Text("Sensores y Permisos", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { 
                        gpsEnabled = !gpsEnabled
                        scope.launch { 
                            lastMessage = "GPS Toggle: $gpsEnabled"
                            sendMessage(activity, "/eco-guia/simulate/permissions", "$gpsEnabled/$cameraReady") 
                        } 
                    }, modifier = Modifier.weight(1f)) { 
                        Text(if (gpsEnabled) "Apagar GPS" else "Activar GPS") 
                    }
                    Button(onClick = { 
                        cameraReady = !cameraReady
                        scope.launch { 
                            lastMessage = "Cámara Toggle: $cameraReady"
                            sendMessage(activity, "/eco-guia/simulate/permissions", "$gpsEnabled/$cameraReady") 
                        } 
                    }, modifier = Modifier.weight(1f)) { 
                        Text(if (cameraReady) "Bloquear Cam" else "Habilitar Cam") 
                    }
                }
            }

            item {
                Text("Controles de Simulación", style = MaterialTheme.typography.titleSmall)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { 
                        scope.launch { 
                            lastMessage = "Insertando alertas..."
                            val alert1 = AlertEntity("1", "Geo-Drop Detectado", "GEODROP", System.currentTimeMillis())
                            val alert2 = AlertEntity("2", "Museo Nacional", "SITE", System.currentTimeMillis())
                            db.dao().insertAlert(alert1)
                            db.dao().insertAlert(alert2)
                            
                            val payload = "${alert1.id}|${alert1.message}|${alert1.type};${alert2.id}|${alert2.message}|${alert2.type}"
                            sendMessage(activity, "/eco-guia/simulate/alerts", payload) 
                        } 
                    }, modifier = Modifier.fillMaxWidth()) { Text("Sincronizar Alertas") }

                    Button(onClick = { 
                        scope.launch { 
                            lastMessage = "Simulando paso..."
                            sendMessage(activity, "/eco-guia/simulate/proximity", "step") 
                        } 
                    }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { 
                        Text("Simular Paso (Brújula)") 
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Ruta Automática", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { 
                    scope.launch { 
                        lastMessage = "Ruta iniciada..."
                        sendMessage(activity, "/eco-guia/simulate/proximity", "15")
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "0")
                        kotlinx.coroutines.delay(2000)
                        sendMessage(activity, "/eco-guia/simulate/route", "1/3")
                        
                        lastMessage = "Punto 2..."
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "20")
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "0")
                        kotlinx.coroutines.delay(2000)
                        sendMessage(activity, "/eco-guia/simulate/route", "2/3")
                        
                        lastMessage = "Finalizando..."
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "10")
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "0")
                        kotlinx.coroutines.delay(2000)
                        sendMessage(activity, "/eco-guia/simulate/route", "3/3")
                        lastMessage = "Ruta Completada"
                    } 
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { 
                    Text("Ejecutar Flujo Completo")
                }
            }
        }
    }
}

/**
 * Envía un mensaje al dispositivo Wear OS.
 */
private suspend fun sendMessage(context: android.content.Context, path: String, payload: String) {
    try {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            Wearable.getMessageClient(context)
                .sendMessage(node.id, path, payload.toByteArray())
                .await()
        }
    } catch (e: Exception) {
        Log.e("EcoGuiaMobile", "Error al enviar mensaje: ${e.message}")
    }
}
