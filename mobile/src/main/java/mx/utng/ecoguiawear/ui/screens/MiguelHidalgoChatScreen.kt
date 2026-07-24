/**
 * Archivo: MiguelHidalgoChatScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla de chat interactivo con el avatar de Miguel Hidalgo IA. 
 * Implementa una interfaz de conversaciÃ³n guiada con burbujas de mensaje estilizadas.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
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
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la interfaz de chat con Miguel Hidalgo IA.
 */
@Composable
fun MiguelHidalgoChatScreen(
    onKnowledgeBaseClick: () -> Unit
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
                Text("Miguel Hidalgo IA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Chat", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            IconButton(
                onClick = onKnowledgeBaseClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // IA Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(EcoGuiaColors.JadeGradient, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MH", color = EcoGuiaColors.Background, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Miguel Hidalgo IA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tu mentor e historiador virtual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }

        // Conversation Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "ConversaciÃ³n guiada", 
                    color = Color.Black, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                UserChatBubble("Â¿QuÃ© sucediÃ³ en esta casona?")
            }
            
            item {
                IAChatBubble("AquÃ­ se gestÃ³ la conspiraciÃ³n de Dolores el 15 de septiembre de 1810. Allende y Aldama vinieron a informar sobre la traiciÃ³n de la conspiraciÃ³n de QuerÃ©taro.")
            }
            
            item {
                UserChatBubble("Dime de este sitio")
            }
            
            item {
                IAChatBubble("Actualmente funciona como el Museo de la Independencia, preservando la memoria del primer cura insurgente.")
            }
        }

        // Input Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Â¿QuÃ© deseas descubrir?", 
                    color = Color.LightGray, 
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Send, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun UserChatBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(EcoGuiaColors.JadeGradient)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, color = EcoGuiaColors.Background, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun IAChatBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, color = Color.DarkGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MiguelHidalgoChatScreenPreview() {
    EcoGuiaMobileTheme {
        MiguelHidalgoChatScreen({})
    }
}




