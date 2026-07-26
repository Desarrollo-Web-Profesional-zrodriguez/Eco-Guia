/**
 * Archivo: MoreOptionsScreen.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Pantalla completa que muestra la cuadrícula de opciones adicionales del sistema.
 * Todas las opciones están habilitadas para cualquier usuario.
 *
 * Funciones destacadas:
 * - MoreOptionsScreen: Renderiza la cuadrícula de accesos rápidos a Mi Colección, Rutas,
 *   Crear Ruta, Alta de Sitio, IA, Dispositivos, Modo Offline y Permisos.
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
    isSuperAdmin: Boolean = true,
    isModerator: Boolean = true,
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
                    .height(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(EcoGuiaColors.BackgroundGradient)
                    .padding(16.dp)
            ) {
                Column {
                    Text("Centro de navegación", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Accesos rápidos a todas las funciones del sistema.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Opciones principales", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

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
                        title = "Rutas turísticas",
                        subtitle = "Explora y sigue recorridos guiados",
                        icon = Icons.Default.Map,
                        onClick = { onOptionClick("search_experience") }
                    )
                }

                if (isModerator) {
                    item {
                        MenuOptionItem(
                            title = "Crear ruta",
                            subtitle = "Diseñar recorrido turístico",
                            icon = Icons.Default.AltRoute,
                            onClick = { onOptionClick("create_route") }
                        )
                    }
                    item {
                        MenuOptionItem(
                            title = "Alta de sitio",
                            subtitle = "Registrar nuevo punto histórico",
                            icon = Icons.Default.AddLocationAlt,
                            onClick = { onOptionClick("site_registration") }
                        )
                    }
                }

                item {
                    MenuOptionItem(
                        title = "Cámara Geo-Drop",
                        subtitle = "Escanear y anclar cápsulas AR",
                        icon = Icons.Default.CameraAlt,
                        enabled = true,
                        onClick = { onOptionClick("camera_capture") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Miguel Hidalgo IA",
                        subtitle = "Chatbot histórico con AI",
                        icon = Icons.Default.Chat,
                        enabled = true,
                        onClick = { onOptionClick("chat_ia") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Mi perfil",
                        subtitle = "Datos, logros y cuenta",
                        icon = Icons.Default.Person,
                        enabled = true,
                        onClick = { onOptionClick("profile") }
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

                item {
                    MenuOptionItem(
                        title = "Modo offline",
                        subtitle = "Rutas descargadas",
                        icon = Icons.Default.Download,
                        enabled = true,
                        onClick = { onOptionClick("offline") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Ajustes y Permisos",
                        subtitle = "Permisos de GPS y alertas",
                        icon = Icons.Default.Settings,
                        enabled = true,
                        onClick = { onOptionClick("permissions") }
                    )
                }

                if (isModerator) {
                    item {
                        MenuOptionItem(
                            title = "Moderación",
                            subtitle = "Revisión de reportes y cápsulas",
                            icon = Icons.Default.Security,
                            onClick = { onOptionClick("moderation_list") }
                        )
                    }
                }

                if (isSuperAdmin) {
                    item {
                        MenuOptionItem(
                            title = "Panel Admin",
                            subtitle = "Administración total de sistema",
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
        MoreOptionsScreen(true, true, {})
    }
}

