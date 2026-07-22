/**
 * Archivo: CreateRouteScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla de creación de rutas personalizadas por parte del administrador.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
fun CreateRouteScreen() {
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
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Crear ruta", color = Color.White, fontSize = 14.sp)
                Text("Cuna-H", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Map Section Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9))
        ) {
            Text("Visor de Mapa", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
        }

        // Configuration List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Ruta nueva", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    ConfigStepItem(
                        number = 1,
                        title = "Seleccionar sitios",
                        subtitle = "Museo, parroquia, casa"
                    )
                }
                item {
                    ConfigStepItem(
                        number = 2,
                        title = "Orden del recorrido",
                        subtitle = "Arrastra y organiza"
                    )
                }
                item {
                    ConfigStepItem(
                        number = 3,
                        title = "Recompensa final",
                        subtitle = "Cápsula histórica"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            EcoButton(
                text = "Publicar ruta",
                onClick = { }
            )
        }
    }
}

@Composable
fun ConfigStepItem(
    number: Int,
    title: String,
    subtitle: String
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
                    .background(Color(0xFFF1F4F1), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            RadioButton(selected = false, onClick = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRouteScreenPreview() {
    EcoGuiaMobileTheme {
        CreateRouteScreen()
    }
}
