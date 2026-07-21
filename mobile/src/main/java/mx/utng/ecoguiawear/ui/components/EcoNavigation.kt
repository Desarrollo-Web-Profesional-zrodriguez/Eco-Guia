/**
 * Archivo: EcoNavigation.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Componentes de navegación centralizados, incluyendo la barra inferior optimizada.
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Barra de navegación inferior personalizada para Eco-Guía.
 */
@Composable
fun EcoBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit
) {
    NavigationBar(
        containerColor = EcoGuiaColors.DeepBlue,
        contentColor = EcoGuiaColors.Text,
        modifier = Modifier
            .height(70.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = currentRoute == "exploration",
            onClick = { onNavigate("exploration") },
            icon = { Icon(Icons.Default.LocationOn, null) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "radar",
            onClick = { onNavigate("radar") },
            icon = { Icon(Icons.Default.ShareLocation, null) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") },
            icon = { Icon(Icons.Default.FavoriteBorder, null) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onOpenSidebar,
            icon = { Icon(Icons.Default.Menu, null) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
    }
}

/**
 * Item genérico para menús de opciones (utilizado en MoreOptionsScreen).
 */
@Composable
fun MenuOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) EcoGuiaColors.Jade else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp, 
                color = if (enabled) Color.Black else Color.Gray
            )
            Text(
                text = subtitle, 
                fontSize = 10.sp, 
                color = Color.Gray, 
                lineHeight = 12.sp
            )
        }
    }
}
