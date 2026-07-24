/**
 * Archivo: IAKnowledgeBaseScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla informativa que muestra la base de conocimiento cargada en la IA de Miguel Hidalgo.
 * Detalla preguntas frecuentes, respuestas guiadas y el tono de respuesta configurado.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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

/**
 * Composable que representa la base de conocimiento de la IA.
 */
@Composable
fun IAKnowledgeBaseScreen() {
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
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Column {
                Text("Miguel Hidalgo IA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Base de conocimiento", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
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
                Text("Pregunta y respuesta", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Consulta la base para una IA asertiva y eficaz. Estos datos guÃ­an el comportamiento del chatbot.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Knowledge Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Base de IA", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    KnowledgeEntryItem(
                        label = "PREGUNTA FRECUENTE",
                        content = "Â¿Por quÃ© este museo es importante?"
                    )
                }
                item {
                    KnowledgeEntryItem(
                        label = "RESPUESTA GUIADA",
                        content = "Porque conserva relatos y piezas vinculadas al inicio del movimiento insurgente liderado por Don Miguel Hidalgo y Costilla.",
                        description = "Fuente: Archivo HistÃ³rico de Dolores"
                    )
                }
                item {
                    KnowledgeEntryItem(
                        label = "TONO",
                        content = "HistÃ³rico, cercano y respetuoso"
                    )
                }
            }
        }

        // Bottom Action
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Entrenar base local",
                onClick = { }
            )
        }
    }
}

@Composable
fun KnowledgeEntryItem(
    label: String,
    content: String,
    description: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
            
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IAKnowledgeBaseScreenPreview() {
    EcoGuiaMobileTheme {
        IAKnowledgeBaseScreen()
    }
}



