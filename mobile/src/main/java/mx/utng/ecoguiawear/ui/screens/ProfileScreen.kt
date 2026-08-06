/**
 * Archivo: ProfileScreen.kt
 *
 * Pantalla de perfil de usuario. Muestra información pública, logros, estadísticas culturales y nivel de explorador.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

import androidx.compose.runtime.LaunchedEffect
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Pantalla composable de perfil del usuario.
 *
 * @param user Datos remotos del usuario logueado.
 * @param viewModel ViewModel de autenticación para obtener métricas y progreso.
 * @param onEditClick Callback para abrir el formulario de edición de perfil.
 */
@Composable
fun ProfileScreen(
    user: RemoteUser?,
    viewModel: AuthViewModel? = null,
    onEditClick: () -> Unit
) {
    LaunchedEffect(user?.id) {
        viewModel?.fetchUserStats()
    }

    val capsulesCount = viewModel?.capsulesCount?.value ?: 0
    val savedItemsCount = viewModel?.savedItemsCount?.value ?: 0
    val explorerLevel = viewModel?.explorerLevel?.value ?: "Nivel 1 - Turista Reciente"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
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
                    if (!user?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user?.bio!!,
                            color = EcoGuiaColors.Gold,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Stats Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Ver perfil", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            var showLevelDialog by remember { mutableStateOf(false) }

            if (showLevelDialog) {
                AlertDialog(
                    onDismissRequest = { showLevelDialog = false },
                    title = { Text("Nivel de Explorador") },
                    text = { Text("Tu nivel actual es: $explorerLevel.\n\nCompleta más cápsulas y registros para subir de rango en la comunidad.") },
                    confirmButton = {
                        TextButton(onClick = { showLevelDialog = false }) { Text("Cerrar") }
                    }
                )
            }

            StatItem(
                title = "Nivel de explorador",
                subtitle = explorerLevel,
                icon = Icons.Default.Star,
                trailing = "Ver",
                onClick = { showLevelDialog = true }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatItem(
                title = "Cápsulas publicadas",
                subtitle = "$capsulesCount aportes en la comunidad",
                icon = Icons.Default.AddCircle,
                trailing = capsulesCount.toString()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatItem(
                title = "Colección guardada",
                subtitle = "$savedItemsCount elementos guardados",
                icon = Icons.Default.Favorite,
                trailing = savedItemsCount.toString()
            )
        }
    }
}

@Composable
fun StatItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
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
        ProfileScreen(user = null, viewModel = null, onEditClick = {})
    }
}
