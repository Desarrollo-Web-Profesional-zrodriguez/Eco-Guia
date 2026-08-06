/**
 * Archivo: AdminNavigation.kt
 *
 * Componente de barra de navegación inferior especializada para el panel de control administrativo y de moderación.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Barra de navegación inferior para roles de administrador y moderador.
 *
 * @param currentRoute Ruta actualmente seleccionada en el grafo de navegación.
 * @param onNavigate Callback invocado al seleccionar un destino en la barra.
 */
@Composable
fun AdminBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = EcoGuiaColors.DeepBlue,
        contentColor = EcoGuiaColors.Text,
        modifier = Modifier
            .height(72.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = currentRoute == "admin_summary",
            onClick = { onNavigate("admin_summary") },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Resumen") },
            label = { Text("Resumen", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                selectedTextColor = EcoGuiaColors.Jade,
                unselectedTextColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "moderate_community",
            onClick = { onNavigate("moderate_community") },
            icon = { Icon(Icons.Default.Gavel, contentDescription = "Moderar") },
            label = { Text("Moderar", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                selectedTextColor = EcoGuiaColors.Jade,
                unselectedTextColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "capsule_gallery",
            onClick = { onNavigate("capsule_gallery") },
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería") },
            label = { Text("Galería", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                selectedTextColor = EcoGuiaColors.Jade,
                unselectedTextColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "security_reports",
            onClick = { onNavigate("security_reports") },
            icon = { Icon(Icons.Default.Security, contentDescription = "Reportes") },
            label = { Text("Seguridad", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                selectedTextColor = EcoGuiaColors.Jade,
                unselectedTextColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
    }
}
