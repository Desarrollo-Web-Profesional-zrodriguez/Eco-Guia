/**
 * Archivo: ManualGeoDropScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla para el anclaje manual de cápsulas (Geo-Drops) por parte de un administrador.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ManualGeoDropScreen(
    onAnchorClick: () -> Unit
) {
    var title by remember { mutableStateOf("Detalle del arco principal") }
    var location by remember { mutableStateOf("Museo - Entrada norte") }

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
                Text("Geo-Drop", color = Color.White, fontSize = 14.sp)
                Text("Anclar cápsula", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Preview Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Vista previa de cápsula", color = Color.Gray)
        }

        // Form Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Geo-Drop manual", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Título", fontSize = 11.sp, color = Color.Gray)
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Ubicación", fontSize = 11.sp, color = Color.Gray)
                            Text(location, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("OK", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Visibilidad", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            Text("Pública después de revisión", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Anclar Geo-Drop",
                onClick = onAnchorClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManualGeoDropScreenPreview() {
    EcoGuiaMobileTheme {
        ManualGeoDropScreen({})
    }
}
