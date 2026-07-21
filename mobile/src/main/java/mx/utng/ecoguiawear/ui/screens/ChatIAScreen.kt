/**
 * Archivo: ChatIAScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla de interacción con el Chatbot Miguel Hidalgo IA.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
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
 * Composable que representa la pantalla de Chat IA.
 */
@Composable
fun ChatIAScreen() {
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
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Column {
                Text("Miguel Hidalgo IA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Chatbot histórico", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Chat, null, tint = Color.White)
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                "¡Hola! Soy Miguel Hidalgo. Pregúntame sobre la historia de Dolores.",
                modifier = Modifier.padding(32.dp),
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Input placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Escribe tu pregunta...", color = Color.LightGray, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Chat, null, tint = EcoGuiaColors.Jade)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatIAScreenPreview() {
    EcoGuiaMobileTheme {
        ChatIAScreen()
    }
}
