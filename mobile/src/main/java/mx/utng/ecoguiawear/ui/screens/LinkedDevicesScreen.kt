/**
 * Archivo: LinkedDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla principal de gestión de dispositivos vinculados (Ecosistema activo).
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*

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

@Composable
fun LinkedDevicesScreen(
    onTVCampaignClick: () -> Unit,
    onManageClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    val realSites = remember { mutableStateListOf<mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val sites = repository.getHistoricalSites()
            realSites.clear()
            realSites.addAll(sites)
        } catch (e: Exception) {
            android.util.Log.e("LinkedDevices", "Error cargando sitios: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = "Vinculados",
            subtitle = "Dispositivos",
            actionIcon = Icons.Default.Tv,
            onActionClick = onTVCampaignClick
        )

        // Active Ecosystem Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ecosistema activo", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Puntos de emisión activos en la base de datos Neon PostgreSQL.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Device List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dispositivos & Puntos (${realSites.size})",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                TextButton(onClick = onManageClick) {
                    Text("Gestionar", color = EcoGuiaColors.Jade)
                }
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(realSites.size) { index ->
                        val site = realSites[index]
                        DeviceItem(
                            title = site.name,
                            subtitle = "Punto activo - Radio ${site.detectionRadiusM}m (${site.siteType ?: "Sitio"})",
                            icon = if (index % 2 == 0) Icons.Default.Tv else Icons.Default.Watch,
                            statusColor = EcoGuiaColors.Jade,
                            onClick = onStatusClick
                        )
                    }
                }
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
        LinkedDevicesScreen({}, {}, {})
    }
}
