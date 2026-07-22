/**
 * Archivo: SiteRegistrationScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla inicial para dar de alta un sitio histórico (Paso 1).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
fun SiteRegistrationScreen(
    onNext: () -> Unit
) {
    var siteName by remember { mutableStateOf("Museo de la Independencia Nacional") }
    var category by remember { mutableStateOf("Museo histórico") }
    var address by remember { mutableStateOf("Zacatecas 6, Centro, Dolores Hidalgo") }

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
                Text("Alta de sitio", color = Color.White, fontSize = 14.sp)
                Text("Datos básicos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Selected Site Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Museo de la Independencia", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Ruta principal para visitantes locales y turistas.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Form Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Alta de museo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    EcoTextField(value = siteName, onValueChange = { siteName = it }, label = "NOMBRE DEL SITIO")
                }
                item {
                    EcoTextField(value = category, onValueChange = { category = it }, label = "CATEGORÍA")
                }
                item {
                    EcoTextField(value = address, onValueChange = { address = it }, label = "DIRECCIÓN")
                }
            }
        }

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Guardar sitio",
                onClick = onNext
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteRegistrationScreenPreview() {
    EcoGuiaMobileTheme {
        SiteRegistrationScreen({})
    }
}
