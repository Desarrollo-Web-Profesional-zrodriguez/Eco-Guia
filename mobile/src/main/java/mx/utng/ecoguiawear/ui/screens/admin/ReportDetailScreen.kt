/**
 * Archivo: ReportDetailScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla para la resoluciÃ³n detallada de un reporte de moderaciÃ³n.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun ReportDetailScreen(
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
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Reporte", color = Color.White, fontSize = 14.sp)
                Text("Detalle de seguridad", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Image in question
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Archivo Reportado", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Action Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("DecisiÃ³n del reporte", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    DecisionItem(
                        title = "Reporte de acoso",
                        subtitle = "El contenido rompe las reglas",
                        color = Color.Red,
                        tag = "Eliminar"
                    )
                }
                item {
                    DecisionItem(
                        title = "Mala ubicaciÃ³n",
                        subtitle = "El punto estÃ¡ desplazado",
                        color = EcoGuiaColors.Gold,
                        tag = "Mover"
                    )
                }
                item {
                    DecisionItem(
                        title = "AnÃ¡lisis asertivo",
                        subtitle = "Contenido verificado",
                        color = EcoGuiaColors.Jade,
                        tag = "Aprobar"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Action
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Resolver reporte",
                onClick = onResolve
            )
        }
    }
}

@Composable
fun DecisionItem(
    title: String,
    subtitle: String,
    color: Color,
    tag: String
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
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportDetailScreenPreview() {
    EcoGuiaMobileTheme {
        ReportDetailScreen({})
    }
}




