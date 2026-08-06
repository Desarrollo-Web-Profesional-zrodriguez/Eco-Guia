/**
 * Archivo: ModerateCommunityScreen.kt
 *
 * Pantalla principal del panel de moderación para revisar publicaciones, reportes y aportaciones de la comunidad.
 *
 * @since 2026-08-05
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

/**
 * Pantalla composable de moderación de contenido comunitario y gestión de denuncias.
 *
 * @param onNavigate Callback para navegar entre pantallas del módulo de administración.
 */
@Composable
fun ModerateCommunityScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            AdminBottomBar(currentRoute = "moderate_community", onNavigate = onNavigate)
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
                    Text("Comunidad", color = EcoGuiaColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Moderación", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                item {
                    ModerationSummaryCard(
                        pendingCount = 8,
                        resolvedToday = 12
                    )
                }

                item {
                    Text(
                        text = "Pendientes de revisión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(3) { index ->
                    ModerationItem(
                        type = if (index == 0) "Comentario" else if (index == 1) "Foto" else "Geo-Drop",
                        user = "Usuario_${123 + index}",
                        reason = "Contenido inapropiado",
                        date = "Hace ${index + 1}h",
                        icon = if (index == 0) Icons.Default.Comment else if (index == 1) Icons.Default.Image else Icons.Default.Place,
                        onClick = { onNavigate("review_detail") }
                    )
                }
            }
        }
    }
}

@Composable
fun ModerationSummaryCard(pendingCount: Int, resolvedToday: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pendingCount.toString(), color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Pendientes", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            VerticalDivider(modifier = Modifier.height(40.dp), color = Color.White.copy(alpha = 0.1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(resolvedToday.toString(), color = EcoGuiaColors.Jade, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Resueltos hoy", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ModerationItem(
    type: String,
    user: String,
    reason: String,
    date: String,
    icon: ImageVector,
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
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EcoGuiaColors.Gold, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(type, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(date, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                }
                Text("De: $user", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(reason, color = EcoGuiaColors.JadeLight, fontSize = 12.sp)
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModerateCommunityScreenPreview() {
    EcoGuiaMobileTheme {
        ModerateCommunityScreen({})
    }
}
