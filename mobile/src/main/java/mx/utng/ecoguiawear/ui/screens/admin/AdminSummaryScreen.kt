/**
 * Archivo: AdminSummaryScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla principal del panel de administración que muestra un resumen de actividad.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.AdminBottomBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun AdminSummaryScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            AdminBottomBar(currentRoute = "admin_summary", onNavigate = onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EcoGuiaColors.DeepBlue)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Column {
                    Text("Admin", color = EcoGuiaColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Resumen", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                
                Surface(
                    onClick = { /* Profile click */ },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                // Today's Activity Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(EcoGuiaColors.JadeGradient)
                                .padding(24.dp)
                        ) {
                            Column {
                                Text("Actividad de hoy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem("24", "Nuevos Usuarios")
                                    StatItem("15", "Geo-Drops")
                                    StatItem("8", "Reportes")
                                }
                            }
                        }
                    }
                }

                // Quick Actions Section
                item {
                    Text(
                        text = "Acciones rápidas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    QuickActionItem(
                        icon = Icons.Default.Gavel,
                        title = "Moderar Comunidad",
                        status = "3 pendientes",
                        onClick = { onNavigate("moderate_community") }
                    )
                }

                item {
                    QuickActionItem(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Galería de Cápsulas",
                        status = "12 activas",
                        onClick = { onNavigate("capsule_gallery") }
                    )
                }

                item {
                    QuickActionItem(
                        icon = Icons.Default.Security,
                        title = "Reportes de Seguridad",
                        status = "2 críticos",
                        onClick = { onNavigate("security_reports") }
                    )
                }
                
                item {
                    QuickActionItem(
                        icon = Icons.Default.AddPhotoAlternate,
                        title = "Cargar a Galería Oficial",
                        status = "Nuevo",
                        onClick = { onNavigate("gallery_addition") }
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    title: String,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Text(status, color = EcoGuiaColors.JadeLight, fontSize = 12.sp)
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminSummaryScreenPreview() {
    EcoGuiaMobileTheme {
        AdminSummaryScreen({})
    }
}
