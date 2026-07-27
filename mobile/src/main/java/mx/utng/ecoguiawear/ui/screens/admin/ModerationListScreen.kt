/**
 * Archivo: ModerationListScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Pantalla que lista dinámicamente los reportes y contenidos Geo-Drop pendientes
 * de revisión desde la base de datos remota para el Administrador o Moderador.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.ModerationViewModel

@Composable
fun ModerationListScreen(
    onResolveClick: (RemoteGeoDrop) -> Unit,
    moderationViewModel: ModerationViewModel = viewModel()
) {
    val pendingDrops by moderationViewModel.pendingDrops
    val isLoading by moderationViewModel.isLoading

    LaunchedEffect(Unit) {
        moderationViewModel.loadPendingContent()
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
                Text("Reportes & Cápsulas", color = Color.White, fontSize = 14.sp)
                Text("Panel de Moderación", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { moderationViewModel.loadPendingContent() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Moderación responsable", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Revisa y aprueba el contenido para mantener una comunidad saludable.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                Surface(
                    color = EcoGuiaColors.Jade.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${pendingDrops.size}",
                        color = EcoGuiaColors.Jade,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Lista de Reportes y Cápsulas
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Pendientes de revisión (${pendingDrops.size})",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade)
                }
            } else if (pendingDrops.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("¡Todo al día!", fontWeight = FontWeight.Bold)
                        Text("No hay reportes o cápsulas pendientes de revisar.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pendingDrops) { drop ->
                        ReportListItem(
                            item = drop,
                            onViewClick = {
                                moderationViewModel.selectDrop(drop)
                                onResolveClick(drop)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportListItem(
    item: RemoteGeoDrop,
    onViewClick: () -> Unit
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
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.description ?: "Cápsula creada por usuario",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Surface(
                color = if (item.status == "approved") EcoGuiaColors.Jade.copy(alpha = 0.15f) else EcoGuiaColors.Gold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = item.status.uppercase(),
                    color = if (item.status == "approved") EcoGuiaColors.Jade else EcoGuiaColors.Gold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            TextButton(onClick = onViewClick) {
                Text("Ver", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)
            }
        }
    }
}
