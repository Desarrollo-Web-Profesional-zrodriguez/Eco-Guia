/**
 * Archivo: MoreOptionsScreen.kt
 *
 * Pantalla completa que muestra la cuadrícula de opciones y herramientas complementarias del sistema Eco-Guía.
 *
 * @since 2026-08-05
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
 * Pantalla composable de catálogo extendido de opciones y herramientas avanzadas.
 *
 * @param isSuperAdmin Habilita o restringe accesos de nivel súper administrador.
 * @param isModerator Habilita o restringe accesos con privilegios de moderador cultural.
 * @param onOptionClick Callback invocado con la ruta/clave seleccionada por el usuario.
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
                        subtitle = "Fotos, rutas y cápsulas",
                        icon = Icons.Default.Favorite,
                        onClick = { onOptionClick("collection") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Miguel Hidalgo IA",
                        subtitle = "Chatbot histórico",
                        icon = Icons.Default.AutoAwesome,
                        onClick = { onOptionClick("chat_ia") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Mi perfil",
                        subtitle = "Datos, logros y cuenta",
                        icon = Icons.Default.Person,
                        onClick = { onOptionClick("profile") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Modo offline",
                        subtitle = "Rutas descargadas",
                        icon = Icons.Default.Download,
                        onClick = { onOptionClick("offline") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Permisos de la app",
                        subtitle = "GPS y notificaciones",
                        icon = Icons.Default.Settings,
                        onClick = { onOptionClick("permissions") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Dispositivos",
                        subtitle = "Reloj y TV conectados",
                        icon = Icons.Default.Watch,
                        onClick = { onOptionClick("linked_devices") }
                    )
                }

                item {
                    MenuOptionItem(
                        title = "Configurar TV",
                        subtitle = "Salón de la fama",
                        icon = Icons.Default.Tv,
                        onClick = { onOptionClick("tv_campaign") }
                    )
                }

                if (isModerator) {
                    item {
                        MenuOptionItem(
                            title = "Dar alta del sitio",
                            subtitle = "Registrar nuevo punto histórico",
                            icon = Icons.Default.AddLocationAlt,
                            onClick = { onOptionClick("site_registration") }
                        )
                    }
                    item {
                        MenuOptionItem(
                            title = "Moderación",
                            subtitle = "Revisión de cápsulas",
                            icon = Icons.Default.Security,
                            onClick = { onOptionClick("moderation_list") }
                        )
                    }
                }

                if (isSuperAdmin) {
                    item {
                        MenuOptionItem(
                            title = "Gestión de Usuarios",
                            subtitle = "Control de roles",
                            icon = Icons.Default.SupervisorAccount,
                            onClick = { onOptionClick("user_management") }
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

