/**
 * Archivo: BottomMenu.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Contenido del menú desplegable inferior (Bottom Sheet) reactivo. 
 * Muestra opciones dinámicas que emergen desde abajo hacia arriba según el contexto.
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Representa una opción dentro del menú inferior.
 */
data class ContextMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val enabled: Boolean = true
)

/**
 * Composable que renderiza las opciones del menú que sale desde abajo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenuSheet(
    currentRoute: String,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val items = getContextItems(currentRoute, isAdmin)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = EcoGuiaColors.DeepBlue,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = EcoGuiaColors.Jade) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EcoGuiaColors.Jade
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    ContextMenuItemRow(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { 
                            onNavigate(item.route)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContextMenuItemRow(
    item: ContextMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = item.enabled, onClick = onClick),
        color = if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (!item.enabled) Color.Gray 
                       else if (isSelected) EcoGuiaColors.Jade 
                       else Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = if (!item.enabled) Color.Gray else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Obtiene las opciones del menú según la pantalla donde se encuentre el usuario.
 */
private fun getContextItems(route: String, isAdmin: Boolean): List<ContextMenuItem> {
    return when (route) {
        "edit_profile" -> listOf(
            ContextMenuItem("Seguridad", Icons.Default.Security, "security"),
            ContextMenuItem("Ver Perfil", Icons.Default.Person, "profile"),
            ContextMenuItem("Mi Colección", Icons.Default.Favorite, "collection")
        )
        else -> listOf(
            ContextMenuItem("Mi Colección", Icons.Default.Favorite, "collection"),
            ContextMenuItem("Miguel Hidalgo IA", Icons.Default.AutoAwesome, "chat_ia", enabled = isAdmin),
            ContextMenuItem("Mi Perfil", Icons.Default.AccountCircle, "profile", enabled = isAdmin),
            ContextMenuItem("Ajustes", Icons.Default.Settings, "settings", enabled = isAdmin)
        )
    }
}
