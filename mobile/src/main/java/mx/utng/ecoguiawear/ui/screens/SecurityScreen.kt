/**
 * Archivo: SecurityScreen.kt
 *
 * Pantalla de gestión de cuenta y seguridad del usuario, con accesos a cambio de credenciales y cierre de sesión.
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Pantalla composable de configuración de cuenta, privacidad y seguridad.
 *
 * @param user Datos del usuario actualmente autenticado.
 * @param onLogoutClick Callback ejecutado para cerrar la sesión activa del usuario.
 * @param onChangePasswordClick Callback para cambiar contraseña.
 * @param onDeleteAccountClick Callback para solicitar eliminación de cuenta.
 */
@Composable
fun SecurityScreen(
    user: RemoteUser?,
    onLogoutClick: () -> Unit,
    onChangePasswordClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seguridad de la Cuenta") },
            text = { Text("Tu cuenta está protegida y verificada con autenticación segura de Eco-Guía.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Aceptar") }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Eliminar cuenta definitivamente?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Esta acción no se puede deshacer. Se borrarán definitivamente tu usuario, guardados, progresos, dispositivos vinculados e interacciones registradas en la base de datos.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccountClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar para siempre", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        EcoTopBar(
            title = "Cuenta",
            subtitle = "Seguridad",
            actionIcon = Icons.Default.Info,
            onActionClick = { showDialog = true }
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Cuenta y seguridad",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecurityItem(
                title = "Correo electrónico",
                subtitle = user?.email ?: "Sin correo registrado",
                icon = Icons.Default.Email,
                trailing = "Verificado"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SecurityItem(
                title = "Contraseña",
                subtitle = "Gestionar o restablecer contraseña",
                icon = Icons.Default.Lock,
                trailing = "Cambiar",
                onClick = onChangePasswordClick
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SecurityItem(
                title = "Estado de verificación",
                subtitle = "Cuenta activa y verificada",
                icon = Icons.Default.Check,
                trailing = "OK"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SecurityItem(
                title = "Cerrar sesión",
                subtitle = "Salir de este dispositivo",
                icon = Icons.Default.ExitToApp,
                trailing = "",
                onClick = onLogoutClick,
                isLogout = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecurityItem(
                title = "Eliminar cuenta",
                subtitle = "Borrado permanente y en cascada",
                icon = Icons.Default.DeleteForever,
                trailing = "Eliminar",
                onClick = { showDeleteConfirmDialog = true },
                isLogout = true
            )
        }
    }
}

@Composable
fun SecurityItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String,
    onClick: () -> Unit = {},
    isLogout: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isLogout) Color(0xFFFFEBEE)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isLogout) Color.Red else EcoGuiaColors.Jade,
                    modifier = Modifier.size(20.dp)
                )
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

            if (trailing.isNotEmpty()) {
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
}

@Preview(showBackground = true)
@Composable
fun SecurityScreenPreview() {
    EcoGuiaMobileTheme {
        SecurityScreen(user = null, onLogoutClick = {})
    }
}
