/**
 * Archivo: VisitorAnalyticsScreen.kt
 *
 * Dashboard de analítica que muestra el mapa de calor, métricas y afluencia de visitantes.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Pantalla composable para visualización de métricas de tráfico y estadísticas turísticas.
 */
@Composable
fun VisitorAnalyticsScreen(
    userId: String = "",
    userRole: String = ""
) {
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    var registeredUsersCount by remember { mutableStateOf(0) }
    var totalGeoDropsCount by remember { mutableStateOf(0) }
    var pendingReportsCount by remember { mutableStateOf(0) }
    var ownedSitesCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val isAdmin = remember(userRole) {
        userRole.lowercase() in listOf("admin", "super_admin", "administrator")
    }

    LaunchedEffect(userId) {
        isLoading = true
        try {
            // 1. Obtener lista de usuarios registrados
            val users = repository.getAllUsers()
            registeredUsersCount = users.size

            // 2. Obtener lista de Geo-Drops aprobados/registrados
            val drops = repository.getGeoDrops()
            totalGeoDropsCount = drops.size

            // 3. Obtener sitios pertenecientes al usuario o admin
            if (userId.isNotBlank()) {
                val sites = repository.getSitesByOwnerOrAdmin(userId, isAdmin)
                ownedSitesCount = sites.size
            }

            // 4. Obtener el conteo de Geo-Drops pendientes de moderación o reportes
            val pendingDrops = repository.getPendingGeoDrops()
            pendingReportsCount = pendingDrops.size
        } catch (e: Exception) {
            android.util.Log.e("VisitorAnalytics", "Error cargando analíticas reales: ${e.message}")
        } finally {
            isLoading = false
        }
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
                Text("Analítica Cultural", color = Color.White, fontSize = 14.sp)
                Text("Panel de Rendimiento", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Group, null, tint = Color.White)
            }
        }

        // Resumen de estado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen del Ecosistema", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (!isAdmin) {
                    Text(
                        "Panel exclusivo para el Administrador del sistema.", 
                        color = Color(0xFFFFB74D), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (isLoading) {
                    Text("Cargando analíticas en tiempo real desde la base de datos...", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                } else {
                    Text(
                        "Resumen global de usuarios registrados, cápsulas creadas por la comunidad y métricas de moderación.", 
                        color = Color.White.copy(alpha = 0.7f), 
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Sección de métricas reales
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Métricas Generales", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            if (!isAdmin) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Acceso Restringido",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Esta pantalla contiene datos globales del sistema y está reservada exclusivamente para la cuenta del Administrador.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        MetricItem(
                            value = registeredUsersCount.toString(),
                            label = "Usuarios registrados",
                            subLabel = "Comunidad de turistas y moderadores en la app"
                        )
                    }
                    item {
                        MetricItem(
                            value = totalGeoDropsCount.toString(),
                            label = "Cápsulas públicas (Geo-Drops)",
                            subLabel = "Fotos y recuerdos registrados en el mapa"
                        )
                    }
                    item {
                        MetricItem(
                            value = ownedSitesCount.toString(),
                            label = "Sitios del sistema",
                            subLabel = "Total de sitios históricos y museos administrados"
                        )
                    }
                    item {
                        MetricItem(
                            value = pendingReportsCount.toString(),
                            label = "Contenidos por revisar",
                            subLabel = if (pendingReportsCount == 0) "Sin contenidos pendientes de moderación" else "Cápsulas pendientes de validación"
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun MetricItem(
    value: String,
    label: String,
    subLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(EcoGuiaColors.Jade.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = value, color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VisitorAnalyticsScreenPreview() {
    EcoGuiaMobileTheme {
        VisitorAnalyticsScreen()
    }
}




