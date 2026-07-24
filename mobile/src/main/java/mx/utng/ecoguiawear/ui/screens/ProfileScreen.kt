/**
 * Archivo: ProfileScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla de perfil de usuario. Muestra información pública, logros y estadísticas.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla de perfil.
 */
@Composable
fun ProfileScreen(
    user: RemoteUser?,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = "Mi Perfil",
            subtitle = "Datos públicos",
            actionIcon = Icons.Default.Edit,
            onActionClick = onEditClick
        )

        // Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(EcoGuiaColors.JadeGradient, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.displayName?.take(1)?.uppercase() ?: "U", 
                        color = EcoGuiaColors.Background, 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = user?.displayName ?: "Usuario Eco-Guía", 
                        color = Color.White, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email ?: "sin_correo@ejemplo.com", 
                        color = Color.White.copy(alpha = 0.7f), 
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Stats Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Ver perfil", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            StatItem(
                title = "Nivel de explorador",
                subtitle = "Nivel 3 - Curador comunitario",
                icon = Icons.Default.Star,
                trailing = "Ver"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatItem(
                title = "Cápsulas publicadas",
                subtitle = "24 aportes en la comunidad",
                icon = Icons.Default.AddCircle,
                trailing = "24"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatItem(
                title = "Colección guardada",
                subtitle = "18 fotos y 4 rutas",
                icon = Icons.Default.Favorite,
                trailing = "18"
            )
        }
    }
}

@Composable
fun StatItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = EcoGuiaColors.Jade.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = trailing,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.Jade
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    EcoGuiaMobileTheme {
        ProfileScreen(null, {})
    }
}
