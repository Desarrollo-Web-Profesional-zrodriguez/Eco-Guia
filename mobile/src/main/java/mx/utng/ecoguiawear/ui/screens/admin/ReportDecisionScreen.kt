/**
 * Archivo: ReportDecisionScreen.kt
 *
 * Pantalla para tomar una decisión final (aprobar, rechazar o archivar) sobre un incidente o reporte de seguridad.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Pantalla composable para dictaminar resolución sobre un reporte de seguridad.
 *
 * @param onBack Callback para cancelar y volver a la vista previa.
 * @param onResolve Callback ejecutado tras asentar la decisión tomada.
 */
@Composable
fun ReportDecisionScreen(
    onBack: () -> Unit,
    onResolve: () -> Unit
) {
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
                .padding(top = 48.dp, start = 12.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column {
                    Text("Incidente", color = EcoGuiaColors.Gold, fontSize = 12.sp)
                    Text("Decisión de Seguridad", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Incident Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, null, tint = Color.Red, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("ID: SEC-8923-X", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Intento de escalada de privilegios",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "El usuario intentó acceder a rutas de la API reservadas para super-administradores desde un cliente no autorizado.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DetailRow("Origen", "192.168.1.104 (León, Gto)")
                        DetailRow("Fecha", "22/07/2026 08:15 AM")
                        DetailRow("Severidad", "Alta")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "Acción Correctiva",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DecisionOption(
                    title = "Bloquear IP permanentemente",
                    description = "Añadir a lista negra de firewall",
                    icon = Icons.Default.Block,
                    color = Color.Red,
                    onClick = onResolve
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                DecisionOption(
                    title = "Suspender cuenta de usuario",
                    description = "Inhabilitar acceso por 30 días",
                    icon = Icons.Default.PersonOff,
                    color = EcoGuiaColors.Gold,
                    onClick = onResolve
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                DecisionOption(
                    title = "Marcar como Falso Positivo",
                    description = "Cerrar incidente sin acciones",
                    icon = Icons.Default.ThumbUp,
                    color = EcoGuiaColors.Jade,
                    onClick = onResolve
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun ReportDecisionScreenPreview() {
    EcoGuiaMobileTheme {
        ReportDecisionScreen({}, {})
    }
}
