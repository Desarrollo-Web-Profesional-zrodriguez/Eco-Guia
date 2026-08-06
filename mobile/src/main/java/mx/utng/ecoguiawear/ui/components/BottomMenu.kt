/**
 * Archivo: BottomMenu.kt
 *
 * Contenido del menú desplegable inferior ([ModalBottomSheet]) reactivo de acciones contextuales.
 * Muestra accesos directos y opciones según la ruta activa y el rol del usuario autenticado.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
 * Modelo de datos que representa una opción seleccionable dentro del menú inferior contextual.
 *
 * @property title Título visible de la opción.
 * @property icon Ícono vectorial representativo.
 * @property route Identificador o destino de navegación asociado.
 * @property enabled Indica si el elemento está habilitado para interacción.
 */
data class ContextMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val enabled: Boolean = true
)

/**
 * Hoja modal inferior que despliega acciones contextuales y de sesión.
 *
 * @param currentRoute Ruta activa en el grafo de navegación.
 * @param isSuperAdmin Indica si el usuario autenticado cuenta con rol de superadministrador.
 * @param isModerator Indica si el usuario autenticado cuenta con rol de moderador.
 * @param onDismiss Callback invocado para cerrar la hoja modal.
 * @param onNavigate Callback invocado para navegar a una ruta seleccionada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenuSheet(
    currentRoute: String,
    isSuperAdmin: Boolean = false,
    isModerator: Boolean = false,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val items = getContextItems(currentRoute, isSuperAdmin, isModerator)


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
 * Obtiene las opciones del menú según la pantalla y el rol del usuario.
 */
private fun getContextItems(route: String, isSuperAdmin: Boolean, isModerator: Boolean): List<ContextMenuItem> {
    val items = mutableListOf<ContextMenuItem>()

    // Opciones contextuales de perfil
    if (route == "profile" || route == "edit_profile") {
        items.add(ContextMenuItem("Seguridad", Icons.Default.Security, "security"))
    }

    // Cerrar Sesión siempre presente
    items.add(ContextMenuItem("Cerrar Sesión", Icons.AutoMirrored.Filled.ExitToApp, "logout"))

    return items

}



