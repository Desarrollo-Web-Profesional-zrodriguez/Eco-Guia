/**
 * Archivo: CommonComponents.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Biblioteca de componentes de UI reutilizables para mantener la consistencia
 * visual en toda la aplicación EcoGuía. Incluye EcoTopBar, EcoBackground, EcoTextField,
 * EcoChipGroup, EcoButton y EcoLogo.
 *
 * Todos los componentes siguen el sistema de diseño EcoGuiaColors y se adaptan
 * automáticamente al modo oscuro mediante MaterialTheme.colorScheme.
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Header estándar Material3 para las pantallas de la app.
 *
 * Usa [MaterialTheme.colorScheme] para adaptarse automÃ¡ticamente al modo oscuro/claro.
 * Compatible con edge-to-edge gracias a [WindowInsets.statusBars].
 *
 * @param title       Texto principal del header (usa typography.titleLarge)
 * @param subtitle    Texto secundario debajo del tÃ­tulo (usa typography.bodyMedium, opcional)
 * @param actionIcon  Icono de acciÃ³n en la esquina superior derecha (opcional)
 * @param onActionClick Callback para el icono de acciÃ³n (opcional)
 */
@Composable
fun EcoTopBar(
    title: String,
    subtitle: String = "",
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 24.dp, end = 16.dp, top = 0.dp, bottom = 4.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = onSurface
            )
        }

        if (actionIcon != null && onActionClick != null) {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // LÃ­nea divisora sutil
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
}

/**
 * Fondo con degradado vertical oficial de la aplicaciÃ³n.
 */
@Composable
fun EcoBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.BackgroundGradient)
    ) {
        content()
    }
}

/**
 * Campo de texto personalizado con bordes redondeados y estilo oscuro.
 */
@Composable
fun EcoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = EcoGuiaColors.Muted, fontSize = 12.sp) },
        placeholder = { 
            if (placeholder.isNotBlank()) {
                Text(placeholder, color = EcoGuiaColors.Muted.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
            unfocusedContainerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = EcoGuiaColors.Text,
            unfocusedTextColor = EcoGuiaColors.Text
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        readOnly = readOnly
    )
}

/**
 * Grupo de chips para selección múltiple o única.
 */
@Composable
fun EcoChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Usamos una LazyRow para evitar problemas de compatibilidad con FlowRow en ciertas versiones
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(options.size) { index ->
            val option = options[index]
            val isSelected = selectedOptions.contains(option)
            FilterChip(
                selected = isSelected,
                onClick = { onOptionSelected(option) },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EcoGuiaColors.Jade,
                    selectedLabelColor = EcoGuiaColors.Background,
                    containerColor = EcoGuiaColors.Surface.copy(alpha = 0.3f),
                    labelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Gray.copy(alpha = 0.5f),
                    selectedBorderColor = EcoGuiaColors.Jade
                )
            )
        }
    }
}

/**
 * Botón con degradado Jade o fondo blanco.
 */
@Composable
fun EcoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useGradient: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = if (!useGradient) ButtonDefaults.buttonColors(containerColor = Color.White) 
                 else ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (useGradient) Modifier.background(EcoGuiaColors.JadeGradient)
                    else Modifier.background(Color.White)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = EcoGuiaColors.Background,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Logotipo central estilizado.
 */
@Composable
fun EcoLogo() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(EcoGuiaColors.Surface.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Logo",
            tint = EcoGuiaColors.Gold,
            modifier = Modifier.size(40.dp)
        )
    }
}

