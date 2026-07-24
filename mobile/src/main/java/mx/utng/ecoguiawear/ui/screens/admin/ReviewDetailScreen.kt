/**
 * Archivo: ReviewDetailScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla para revisar el detalle de una pieza de contenido reportada.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun ReviewDetailScreen(
    onBack: () -> Unit,
    onActionTaken: () -> Unit
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
                    Text("Revisión", color = EcoGuiaColors.Gold, fontSize = 12.sp)
                    Text("Detalle del contenido", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                // User Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(EcoGuiaColors.Jade.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Usuario_456", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Nivel 5 • 12 contribuciones", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Content Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(64.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "\"Este es un comentario de prueba que ha sido reportado por supuesta información falsa sobre el monumento.\"",
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            "Reportado por 2 usuarios",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Decisión administrativa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DecisionOption(
                    title = "Mantener contenido",
                    description = "Verificado como correcto",
                    icon = Icons.Default.CheckCircle,
                    color = EcoGuiaColors.Jade,
                    onClick = onActionTaken
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                DecisionOption(
                    title = "Eliminar definitivamente",
                    description = "Incumple normas de comunidad",
                    icon = Icons.Default.Delete,
                    color = Color.Red,
                    onClick = onActionTaken
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                DecisionOption(
                    title = "Advertir al usuario",
                    description = "Enviar notificación de conducta",
                    icon = Icons.Default.Warning,
                    color = EcoGuiaColors.Gold,
                    onClick = onActionTaken
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DecisionOption(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(description, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewDetailScreenPreview() {
    EcoGuiaMobileTheme {
        ReviewDetailScreen({}, {})
    }
}
