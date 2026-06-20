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
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eco-Guía Dolores: Panel de Control", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Simular eventos para Wear OS:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ControlAction(
                    label = "Simular Cercanía (Geo-Drop a 15m)",
                    onClick = { 
                        scope.launch { 
                            lastMessage = "Enviando proximidad 15m..."
                            sendMessage(activity, "/eco-guia/simulate/proximity", "15") 
                        } 
                    }
                )
            }
            item {
                ControlAction(
                    label = "Simular Llegada (0m)",
                    onClick = { 
                        scope.launch { 
                            lastMessage = "Enviando llegada..."
                            sendMessage(activity, "/eco-guia/simulate/proximity", "0") 
                        } 
                    }
                )
            }
            item {
                ControlAction(
                    label = "Enviar Ruta Independencia (4/7)",
                    onClick = { 
                        scope.launch { 
                            lastMessage = "Enviando progreso de ruta..."
                            sendMessage(activity, "/eco-guia/simulate/route", "4/7") 
                        } 
                    }
                )
            }
            item {
                ControlAction(
                    label = "Activar Modo Sigilo",
                    onClick = { 
                        scope.launch { 
                            lastMessage = "Activando sigilo..."
                            sendMessage(activity, "/eco-guia/simulate/stealth", "true") 
                        } 
                    }
                )
            }
            item {
                ControlAction(
                    label = "Desactivar Modo Sigilo",
                    onClick = { 
                        scope.launch { 
                            lastMessage = "Desactivando sigilo..."
                            sendMessage(activity, "/eco-guia/simulate/stealth", "false") 
                        } 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text("Estado: $lastMessage", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ControlAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
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
