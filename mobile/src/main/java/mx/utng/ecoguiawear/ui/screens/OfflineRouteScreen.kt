/**
 * Archivo: OfflineRouteScreen.kt
 *
 * Pantalla que muestra el contenido de una ruta descargada y el estado de la base de datos local para navegación y acceso sin conexión.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Pantalla composable para visualizar rutas descargadas offline.
 */
@Composable
fun OfflineRouteScreen() {
    val context = LocalContext.current
    val database = remember { EcoGuiaDatabase.getDatabase(context) }
    var pendingSyncCount by remember { mutableStateOf(0) }
    var routesCount by remember { mutableStateOf(0) }
    var geoDropsCount by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            mx.utng.ecoguiawear.worker.SyncOfflineWorker.enqueueSync(context)
            val pending = database.dao().getAllPendingSyncActions()
            pendingSyncCount = pending.size
        } catch (e: Exception) {
            pendingSyncCount = 0
        }
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
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Modo Offline", color = Color.White, fontSize = 14.sp)
                Text("Ruta guardada localmente", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (pendingSyncCount == 0) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = "Estado de Sincronización",
                    tint = if (pendingSyncCount == 0) EcoGuiaColors.Jade else EcoGuiaColors.Gold
                )
            }
        }

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ruta descargada", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Mapas, historia y audios disponibles localmente para tu recorrido sin internet.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
                if (pendingSyncCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$pendingSyncCount acción(es) pendiente(s)",
                            color = EcoGuiaColors.Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = {
                                mx.utng.ecoguiawear.worker.SyncOfflineWorker.enqueueSync(context)
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    val pending = database.dao().getAllPendingSyncActions()
                                    pendingSyncCount = pending.size
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Jade),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Sincronizar ahora", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Local Content List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Contenido local", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LocalContentItem(
                    title = "Centro histórico",
                    subtitle = "Sitios guardados en base de datos local",
                    icon = Icons.Default.Place
                )
                LocalContentItem(
                    title = "Mi colección",
                    subtitle = "Cápsulas y fotos almacenadas en el dispositivo",
                    icon = Icons.Default.Favorite
                )
                LocalContentItem(
                    title = "Preguntas frecuentes",
                    subtitle = "Base de datos local (Room + SQLite)",
                    icon = Icons.Default.Chat
                )
            }
        }
    }
}

@Composable
fun LocalContentItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            
            RadioButton(selected = true, onClick = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OfflineRouteScreenPreview() {
    EcoGuiaMobileTheme {
        OfflineRouteScreen()
    }
}




