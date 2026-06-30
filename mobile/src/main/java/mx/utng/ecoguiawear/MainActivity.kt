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
    var gpsEnabled by remember { mutableStateOf(true) }
    var cameraReady by remember { mutableStateOf(true) }

    val db = remember { EcoGuiaDatabase.getDatabase(activity) }
    val stealthModeState = db.dao().getConfigFlow("stealth_mode").collectAsState(initial = null)
    val alertsState = db.dao().getAllAlerts().collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eco-Guía: Dashboard DB en Vivo", style = MaterialTheme.typography.headlineMedium)
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Valores actuales en DB Local:", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Stealth Mode: ${if (stealthModeState.value?.value == "1") "ON (1)" else "OFF (0)"}",
                    color = if (stealthModeState.value?.value == "1") Color(0xFF2E7D32) else Color.Red
                )
                Text("Alertas registradas: ${alertsState.value.size}")
                for (alert in alertsState.value.take(2)) {
                    Text("- ${alert.message} (${alert.type})", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Estado Transmisión: $lastMessage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Conectividad", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { 
                    scope.launch { 
                        lastMessage = "Enviando señal de vinculado..."
                        sendMessage(activity, "/eco-guia/simulate/link", "true") 
                    } 
                }, modifier = Modifier.fillMaxWidth()) { Text("Simular Vinculación con Reloj") }
            }

            item {
                Text("Simulación de Base de Datos (0/1)", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { 
                        scope.launch { 
                            lastMessage = "DB Update: Stealth OFF (0)"
                            db.dao().saveConfig(ConfigEntity("stealth_mode", "0"))
                            sendMessage(activity, "/eco-guia/simulate/stealth", "0") 
                        } 
                    }, modifier = Modifier.weight(1f)) { Text("OFF (0)") }
                    Button(onClick = { 
                        scope.launch { 
                            lastMessage = "DB Update: Stealth ON (1)"
                            db.dao().saveConfig(ConfigEntity("stealth_mode", "1"))
                            sendMessage(activity, "/eco-guia/simulate/stealth", "1") 
                        } 
                    }, modifier = Modifier.weight(1f)) { Text("ON (1)") }
                }
            }

            item {
                Text("Registros de Alertas (DB)", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { 
                    scope.launch { 
                        lastMessage = "Insertando alertas en DB..."
                        val alert1 = AlertEntity("1", "Geo-Drop Detectado", "GEODROP", System.currentTimeMillis())
                        val alert2 = AlertEntity("2", "Museo Nacional", "SITE", System.currentTimeMillis())
                        db.dao().insertAlert(alert1)
                        db.dao().insertAlert(alert2)
                        
                        val payload = "${alert1.id}|${alert1.message}|${alert1.type};${alert2.id}|${alert2.message}|${alert2.type}"
                        sendMessage(activity, "/eco-guia/simulate/alerts", payload) 
                    } 
                }, modifier = Modifier.fillMaxWidth()) { Text("Sincronizar Alertas") }
            }

            item {
                Text("Simulación de Avance (Brújula)", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { 
                    scope.launch { 
                        lastMessage = "Simulando avance..."
                        sendMessage(activity, "/eco-guia/simulate/proximity", "step") 
                    } 
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { 
                    Text("Simular Paso (Brújula)") 
                }
            }

            item {
                Text("Estado de Sistema/Permisos", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { 
                        gpsEnabled = !gpsEnabled
                        scope.launch { 
                            lastMessage = "GPS: $gpsEnabled"
                            sendMessage(activity, "/eco-guia/simulate/permissions", "$gpsEnabled/$cameraReady") 
                        } 
                    }, modifier = Modifier.weight(1f)) { Text("Toggle GPS") }
                    Button(onClick = { 
                        cameraReady = !cameraReady
                        scope.launch { 
                            lastMessage = "Cámara: $cameraReady"
                            sendMessage(activity, "/eco-guia/simulate/permissions", "$gpsEnabled/$cameraReady") 
                        } 
                    }, modifier = Modifier.weight(1f)) { Text("Toggle Cámara") }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Flujo de Prueba (3 Ubicaciones)", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { 
                    scope.launch { 
                        lastMessage = "Iniciando Flujo: Punto 1"
                        sendMessage(activity, "/eco-guia/simulate/proximity", "15")
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "0")
                        kotlinx.coroutines.delay(2000)
                        sendMessage(activity, "/eco-guia/simulate/route", "1/3")
                        
                        lastMessage = "Iniciando Flujo: Punto 2"
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "20")
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "0")
                        kotlinx.coroutines.delay(2000)
                        sendMessage(activity, "/eco-guia/simulate/route", "2/3")
                        
                        lastMessage = "Iniciando Flujo: Punto 3"
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "10")
                        kotlinx.coroutines.delay(3000)
                        sendMessage(activity, "/eco-guia/simulate/proximity", "0")
                        kotlinx.coroutines.delay(2000)
                        sendMessage(activity, "/eco-guia/simulate/route", "3/3")
                        lastMessage = "Ruta Completada (3/3)"
                    } 
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { 
                    Text("Ejecutar Ruta Completa") 
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
