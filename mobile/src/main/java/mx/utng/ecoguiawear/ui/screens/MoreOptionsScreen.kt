/**
 * Archivo: MoreOptionsScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla completa que muestra la cuadrícula de opciones adicionales del sistema.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.MenuOptionItem
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla completa de "Menú Más Opciones".
 */
@Composable
fun MoreOptionsScreen(
    isAdmin: Boolean,
    onOptionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = "Más opciones",
            subtitle = "Menú"
        )

        Column(modifier = Modifier.padding(24.dp)) {
        
        // Card de bienvenida al menú
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(EcoGuiaColors.BackgroundGradient)
                .padding(16.dp)
        ) {
            Column {
                Text("Centro de navegación", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Accesos rápidos para funciones que no caben en la barra inferior.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Usuario", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MenuOptionItem(
                    title = "Mi colección",
                    subtitle = "Fotos, rutas y cápsulas guardadas",
                    icon = Icons.Default.Favorite,
                    onClick = { onOptionClick("collection") }
                )
            }
            
            item {
                MenuOptionItem(
                    title = "Miguel Hidalgo IA",
                    subtitle = "Chatbot histórico con AI",
                    icon = Icons.Default.Chat,
                    enabled = isAdmin,
                    onClick = { onOptionClick("chat_ia") }
                )
            }
            
            item {
                MenuOptionItem(
                    title = "Mi perfil",
                    subtitle = "Datos, logros y cuenta",
                    icon = Icons.Default.Person,
                    enabled = isAdmin,
                    onClick = { onOptionClick("profile") }
                )
            }
            
            item {
                MenuOptionItem(
                    title = "Modo offline",
                    subtitle = "Rutas descargadas",
                    icon = Icons.Default.Download,
                    enabled = isAdmin,
                    onClick = { onOptionClick("offline") }
                )
            }
            
            item {
                MenuOptionItem(
                    title = "Ajustes",
                    subtitle = "Permisos y seguridad",
                    icon = Icons.Default.Settings,
                    enabled = isAdmin,
                    onClick = { onOptionClick("settings") }
                )
            }

            item {
                MenuOptionItem(
                    title = "Dispositivos",
                    subtitle = "Control de reloj y TV",
                    icon = Icons.Default.Watch,
                    onClick = { onOptionClick("linked_devices") }
                )
            }
            
            if (isAdmin) {
                item {
                    MenuOptionItem(
                        title = "Alta de sitio",
                        subtitle = "Registrar nuevo punto histórico",
                        icon = Icons.Default.AddLocationAlt,
                        onClick = { onOptionClick("site_registration") }
                    )
                }
                item {
                    MenuOptionItem(
                        title = "Panel",
                        subtitle = "Administración de sistema",
                        icon = Icons.Default.AdminPanelSettings,
                        onClick = { onOptionClick("admin") }
                    )
                }
            }
        }
    }
}
}

@Preview(showBackground = true)
@Composable
fun MoreOptionsScreenPreview() {
    EcoGuiaMobileTheme {
        MoreOptionsScreen(true, {})
    }
}
