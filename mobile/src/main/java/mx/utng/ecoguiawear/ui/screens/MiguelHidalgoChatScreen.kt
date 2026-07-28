/**
 * Archivo: MiguelHidalgoChatScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-24
 * Descripción: Pantalla de chat interactivo con el avatar de Miguel Hidalgo IA. 
 * Implementa una interfaz de conversación guiada con burbujas de mensaje estilizadas.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.ChatViewModel

/**
 * Función auxiliar para formatear texto con negritas sencillas (**texto**) 
 * y viñetas (* o -).
 */
fun formatAIText(text: String): AnnotatedString {
    // Primero reemplazamos viñetas de texto (* ) por caracteres de punto (• )
    val bulletedText = text.lines().joinToString("\n") { line ->
        if (line.trim().startsWith("* ") || line.trim().startsWith("- ")) {
            "  • ${line.trim().substring(2)}"
        } else {
            line
        }
    }

    return buildAnnotatedString {
        val parts = bulletedText.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Texto entre asteriscos es negrita
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

/**
 * Composable que representa la interfaz de chat con Miguel Hidalgo IA.
 */
@Composable
fun MiguelHidalgoChatScreen(
    onKnowledgeBaseClick: () -> Unit,
    isSuperAdmin: Boolean = false,
    viewModel: ChatViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val isLoading by viewModel.isLoading

    // Refrescar el contexto e historia del chat automáticamente al abrir la pantalla para asegurar que incluye el último entrenamiento
    LaunchedEffect(Unit) {
        viewModel.resetConversation()
    }


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
            
            if (isSuperAdmin) {
                IconButton(
                    onClick = onKnowledgeBaseClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Surface(
                        color = EcoGuiaColors.Gold.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("IA", color = EcoGuiaColors.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    "Conversación histórica", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(messages) { message ->
                if (message.isUser) {
                    UserChatBubble(message.text)
                } else {
                    IAChatBubble(message.text)
                }
            }

            if (isLoading) {
                item {
                    Text(
                        "Miguel Hidalgo está redactando...", 
                        fontSize = 12.sp, 
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Pregúntale al Padre de la Patria...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send, 
                        null, 
                        tint = if (inputText.isNotBlank()) EcoGuiaColors.Jade else Color.Gray, 
                        modifier = Modifier.size(24.dp)
                    )
                }
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
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = formatAIText(text), 
                color = Color.DarkGray, 
                fontSize = 14.sp, 
                lineHeight = 20.sp
            )
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




