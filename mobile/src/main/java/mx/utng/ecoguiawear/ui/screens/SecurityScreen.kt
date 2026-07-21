/**
 * Archivo: SecurityScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla de gestión de cuenta y seguridad.
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla de seguridad.
 */
@Composable
fun SecurityScreen(
    onLogoutClick: () -> Unit
) {
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
                Text("Cuenta", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Seguridad", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("Cuenta y seguridad", fontWeight = FontWeight.Bold, color = EcoGuiaColors.DeepBlue)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecurityItem(
                title = "Correo",
                subtitle = "cesar@email.com",
                icon = Icons.Default.Email,
                trailing = "Editar"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SecurityItem(
                title = "Contraseña",
                subtitle = "Actualizado hace 10 días",
                icon = Icons.Default.Lock,
                trailing = "Cambiar"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SecurityItem(
                title = "Verificación",
                subtitle = "Cuenta verificada",
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isLogout) Color(0xFFFFEBEE) else Color(0xFFE0F2F1), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if (isLogout) Color.Red else EcoGuiaColors.Jade, 
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            if (trailing.isNotEmpty()) {
                Surface(
                    color = Color(0xFFE0F2F1),
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
        SecurityScreen({})
    }
}
