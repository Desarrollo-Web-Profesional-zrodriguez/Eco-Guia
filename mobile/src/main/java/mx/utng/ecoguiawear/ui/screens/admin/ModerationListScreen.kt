/**
 * Archivo: ModerationListScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla que lista los reportes y contenidos pendientes de moderación.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ModerationListScreen(
    onResolveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F4F1))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Reportes", color = Color.White, fontSize = 14.sp)
                Text("Seguridad", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text("M", color = Color.White, fontWeight = FontWeight.Bold)
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
                Text("Moderación responsable", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Revisa los reportes para mantener una comunidad saludable.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        // Report List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Pendientes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    ReportListItem(
                        title = "Contenido reportado",
                        subtitle = "2 fotos por revisar",
                        onViewClick = onResolveClick
                    )
                }
                item {
                    ReportListItem(
                        title = "Geo-Drop mal ubicado",
                        subtitle = "1 reporte por corregir",
                        onViewClick = onResolveClick
                    )
                }
                item {
                    ReportListItem(
                        title = "Sugerencia de IA marcada",
                        subtitle = "3 tickets abiertos",
                        onViewClick = onResolveClick
                    )
                }
            }
        }
    }
}

@Composable
fun ReportListItem(
    title: String,
    subtitle: String,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF1F4F1), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            TextButton(onClick = onViewClick) {
                Text("Ver", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModerationListScreenPreview() {
    EcoGuiaMobileTheme {
        ModerationListScreen({})
    }
}
