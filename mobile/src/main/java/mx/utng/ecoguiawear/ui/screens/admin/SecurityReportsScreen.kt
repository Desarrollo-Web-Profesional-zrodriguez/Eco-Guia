/**
 * Archivo: SecurityReportsScreen.kt
 *
 * Pantalla que lista los reportes de seguridad e incidentes críticos del sistema.
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.AdminBottomBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Pantalla composable para visualizar incidentes y vulnerabilidades de seguridad reportadas.
 *
 * @param onNavigate Callback para navegar entre secciones de administración.
 */
@Composable
fun SecurityReportsScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            AdminBottomBar(currentRoute = "security_reports", onNavigate = onNavigate)
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
                    Text("Sistema", color = EcoGuiaColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Reportes de Seguridad", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                    AlertCard(
                        count = 2,
                        level = "Crítico"
                    )
                }

                item {
                    Text(
                        text = "Alertas activas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(4) { index ->
                    SecurityAlertItem(
                        title = if (index % 2 == 0) "Intento de fuerza bruta" else "Acceso desde IP inusual",
                        description = "Detectado por el módulo de IA en tiempo real.",
                        time = "Hace ${index * 15 + 5} min",
                        isCritical = index < 2,
                        onClick = { onNavigate("report_decision") }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertCard(count: Int, level: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("$count Incidentes $level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Se requiere acción inmediata del administrador.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SecurityAlertItem(
    title: String,
    description: String,
    time: String,
    isCritical: Boolean,
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
                    .background(
                        if (isCritical) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isCritical) Icons.Default.Error else Icons.Default.Info,
                    null,
                    tint = if (isCritical) Color.Red else EcoGuiaColors.Gold,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(description, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
                Text(time, color = EcoGuiaColors.JadeLight, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SecurityReportsScreenPreview() {
    EcoGuiaMobileTheme {
        SecurityReportsScreen({})
    }
}
