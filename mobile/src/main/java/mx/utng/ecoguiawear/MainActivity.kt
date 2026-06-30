package mx.utng.ecoguiawear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase
import mx.utng.ecoguia.shared.domain.model.ConfigEntity
import mx.utng.ecoguia.shared.domain.model.AlertEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ControlPanel(this)
                }
            }
        }
    }
}

@Composable
fun ControlPanel(activity: ComponentActivity) {
    val scope = rememberCoroutineScope()
    var lastMessage by remember { mutableStateOf("Esperando interacción...") }
    
    // Estados locales sincronizados con UI
    var isPhoneConnected by remember { mutableStateOf(false) }
    var gpsEnabled by remember { mutableStateOf(true) }
    var cameraReady by remember { mutableStateOf(true) }

    val db = remember { EcoGuiaDatabase.getDatabase(activity) }
    val stealthModeState = db.dao().getConfigFlow("stealth_mode").collectAsState(initial = null)
    val alertsState = db.dao().getAllAlerts().collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eco-Guía: Panel de Control Móvil", style = MaterialTheme.typography.headlineMedium)
        
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
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
