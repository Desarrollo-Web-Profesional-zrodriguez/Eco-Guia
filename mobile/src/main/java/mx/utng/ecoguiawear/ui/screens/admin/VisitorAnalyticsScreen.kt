/**
 * Archivo: VisitorAnalyticsScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Dashboard de analÃ­tica que muestra el mapa de calor y mÃ©tricas de visitantes.
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
fun VisitorAnalyticsScreen() {
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
                Text("AnalÃ­tica", color = Color.White, fontSize = 14.sp)
                Text("Visitantes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Group, null, tint = Color.White)
            }
        }

        // Heatmap Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mapa de calor", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Pico de exploraciÃ³n entre 12:00 y 17:00 en centro histÃ³rico.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        // Metrics Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("MÃ©tricas", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    MetricItem(
                        value = "128",
                        label = "Visitantes hoy",
                        subLabel = "+15% vs ayer"
                    )
                }
                item {
                    MetricItem(
                        value = "42",
                        label = "CÃ¡psulas vistas",
                        subLabel = "Top: Museos"
                    )
                }
                item {
                    MetricItem(
                        value = "9",
                        label = "Reportes",
                        subLabel = "Sin tickets pendientes"
                    )
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
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = value, color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            
            RadioButton(selected = false, onClick = null)
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




