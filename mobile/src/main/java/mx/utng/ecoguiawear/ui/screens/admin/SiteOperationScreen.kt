/**
 * Archivo: SiteOperationScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Configuración de horarios, costos y accesibilidad para el sitio (Paso 4).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun SiteOperationScreen(
    onFinish: () -> Unit
) {
    var hours by remember { mutableStateOf("Martes a domingo • 10:00 - 17:00") }
    var cost by remember { mutableStateOf("$85 general • estudiantes gratis") }
    var accessibility by remember { mutableStateOf("Entrada con rampa, baños cercanos") }

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
                Text("Operación", color = Color.White, fontSize = 14.sp)
                Text("Horarios y acceso", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Info, null, tint = Color.White)
            }
        }

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Datos prácticos", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Información de utilidad necesaria del sitio de interés.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        // Form Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Horarios y acceso", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    EcoTextField(value = hours, onValueChange = { hours = it }, label = "HORARIO")
                }
                item {
                    EcoTextField(value = cost, onValueChange = { cost = it }, label = "COSTO")
                }
                item {
                    EcoTextField(value = accessibility, onValueChange = { accessibility = it }, label = "ACCESIBILIDAD")
                }
            }
        }

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Publicar datos",
                onClick = onFinish
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteOperationScreenPreview() {
    EcoGuiaMobileTheme {
        SiteOperationScreen({})
    }
}
