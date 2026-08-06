/**
 * Archivo: TVCampaignScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: GestiÃ³n de campaÃ±as visuales para Smart TVs en hoteles y museos (SalÃ³n de la Fama).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun TVCampaignScreen(
    userId: String = "",
    userRole: String = "",
    onAnalyticsClick: () -> Unit,
    onManageDevicesClick: (String) -> Unit
) {
    var selectedProgram by remember { mutableStateOf("gallery") }
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    var ownedSitesCount by remember { mutableStateOf(0) }
    var isLoadingSites by remember { mutableStateOf(true) }

    val isAdmin = remember(userRole) {
        userRole.lowercase() in listOf("admin", "super_admin", "administrator")
    }

    LaunchedEffect(userId) {
        isLoadingSites = true
        try {
            if (userId.isNotBlank()) {
                val sites = repository.getSitesByOwnerOrAdmin(userId, isAdmin)
                ownedSitesCount = sites.size
            } else {
                ownedSitesCount = 0
            }
        } catch (e: Exception) {
            android.util.Log.e("TVCampaignScreen", "Error consultando sitios del usuario: ${e.message}")
            ownedSitesCount = 0
        } finally {
            isLoadingSites = false
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
                Text("Campañas & Emisión TV", color = Color.White, fontSize = 14.sp)
                Text("Programación para Museos y Hoteles", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = onAnalyticsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Resumen Analítico", tint = EcoGuiaColors.Gold, modifier = Modifier.size(22.dp))
                    }
                }
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
                Text("Salón de la Fama Visual", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (isLoadingSites) {
                    Text(
                        "Verificando propiedad de sitios históricos...", 
                        color = Color.White.copy(alpha = 0.7f), 
                        fontSize = 12.sp
                    )
                } else if (ownedSitesCount == 0) {
                    Text(
                        "No tienes ningún sitio histórico o museo registrado a tu propiedad en la plataforma.", 
                        color = Color(0xFFFFB74D), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        "Selecciona 1 programación activa para transmitir en las Smart TV conectadas de tu establecimiento ($ownedSitesCount sitio(s) registrado(s)).", 
                        color = Color.White.copy(alpha = 0.7f), 
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Programming List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Modos de Programación Disponibles", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            if (isLoadingSites) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade)
                }
            } else if (ownedSitesCount == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Transmisión no disponible",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No tienes ningún sitio histórico registrado a tu propiedad para proyectar en Smart TV. Registra tu museo o establecimiento antes de iniciar una campaña visual.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        CampaignItem(
                            title = "Galería de Geo-Drops",
                            subtitle = "Presentación automática de imágenes y fichas informativas capturadas por los visitantes.",
                            icon = Icons.Default.Tv,
                            isSelected = selectedProgram == "gallery",
                            onClick = {
                                selectedProgram = "gallery"
                                onManageDevicesClick("gallery")
                            }
                        )
                    }
                    item {
                        CampaignItem(
                            title = "Mapa del Sitio & Cápsulas",
                            subtitle = "Vista del sitio histórico con su radio de detección y ubicación de todos los Geo-Drops dados de alta.",
                            icon = Icons.Default.QrCode,
                            isSelected = selectedProgram == "public",
                            onClick = {
                                selectedProgram = "public"
                                onManageDevicesClick("public")
                            }
                        )
                    }

                    item {
                        CampaignItem(
                            title = "Resumen & Estadísticas de Cápsulas",
                            subtitle = "Muestra el resumen de visitas e información destacada de las cápsulas históricas.",
                            icon = Icons.Default.AccountTree,
                            isSelected = selectedProgram == "ranking",
                            onClick = {
                                selectedProgram = "ranking"
                                onManageDevicesClick("ranking")
                            }
                        )
                    }
                }
            }

        }

    }
}

@Composable
fun CampaignItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, EcoGuiaColors.Jade) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) EcoGuiaColors.Jade else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = EcoGuiaColors.Jade)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TVCampaignScreenPreview() {
    EcoGuiaMobileTheme {
        TVCampaignScreen(onAnalyticsClick = {}, onManageDevicesClick = {})
    }
}




