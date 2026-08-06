/**
 * Archivo: CommonComponents.kt
 *
 * Biblioteca de componentes de UI reutilizables para mantener la consistencia visual en toda la aplicación EcoGuía.
 * Incluye cabeceras, fondos con degradados, campos de texto, grupos de filtros y botones de acción.
 *
 * @since 2026-08-05
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Cabecera superior estándar para las pantallas de la aplicación.
 *
 * @param title Título principal de la pantalla.
 * @param subtitle Subtítulo descriptivo secundario.
 * @param actionIcon Ícono de acción opcional para la esquina superior derecha.
 * @param onActionClick Callback invocado al presionar el ícono de acción.
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
 * Fondo con degradado vertical oficial de la aplicación.
 *
 * @param content Contenido composable que se renderizará sobre el fondo con degradado.
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
 * Campo de texto estilizado con esquinas redondeadas y temática oscura.
 *
 * @param value Texto actual del campo.
 * @param onValueChange Callback invocado al modificar el contenido.
 * @param label Etiqueta flotante del campo.
 * @param modifier Modificador de diseño.
 * @param placeholder Texto de sugerencia cuando el campo está vacío.
 * @param readOnly Indica si el campo es de solo lectura.
 * @param singleLine Indica si el texto debe limitarse a una sola línea horizontal.
 */
@Composable
fun EcoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
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
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon
    )
}

/**
 * Grupo desplazable horizontal de chips de filtrado.
 *
 * @param options Lista de opciones disponibles.
 * @param selectedOptions Conjunto de opciones actualmente seleccionadas.
 * @param onOptionSelected Callback invocado al pulsar sobre una opción.
 * @param modifier Modificador de diseño.
 */
@Composable
fun EcoChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
 * Botón principal de acción con soporte para degradado temático Jade o fondo sólido.
 *
 * @param text Texto del botón.
 * @param onClick Callback de acción al pulsar el botón.
 * @param modifier Modificador de diseño.
 * @param useGradient Indica si se debe aplicar el degradado Jade característico.
 * @param enabled Indica si el botón está habilitado para interacción.
 */
@Composable
fun EcoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useGradient: Boolean = true,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
 * Logotipo central institucional de Eco-Guía con ícono destacado y contenedor con esquinas redondeadas.
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

/**
 * Estado de validación de requisitos de la contraseña.
 */
data class PasswordRequirements(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false
) {
    val isValid: Boolean
        get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
}

fun calculatePasswordRequirements(password: String): PasswordRequirements {
    return PasswordRequirements(
        hasMinLength = password.length >= 8,
        hasUppercase = password.any { it.isUpperCase() },
        hasLowercase = password.any { it.isLowerCase() },
        hasDigit = password.any { it.isDigit() },
        hasSpecialChar = password.any { !it.isLetterOrDigit() }
    )
}

/**
 * Componente reactivo que muestra los requisitos de seguridad de la contraseña en tiempo real.
 */
@Composable
fun PasswordRequirementsBox(
    password: String,
    modifier: Modifier = Modifier
) {
    val reqs = remember(password) { calculatePasswordRequirements(password) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EcoGuiaColors.Surface.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Requisitos de la contraseña:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EcoGuiaColors.Muted
        )
        RequirementRow(text = "Al menos 8 caracteres", isMet = reqs.hasMinLength)
        RequirementRow(text = "Una letra mayúscula (A-Z)", isMet = reqs.hasUppercase)
        RequirementRow(text = "Una letra minúscula (a-z)", isMet = reqs.hasLowercase)
        RequirementRow(text = "Un número (0-9)", isMet = reqs.hasDigit)
        RequirementRow(text = "Un carácter especial (!@#$%...)", isMet = reqs.hasSpecialChar)
    }
}

@Composable
private fun RequirementRow(text: String, isMet: Boolean) {
    val color = if (isMet) EcoGuiaColors.Jade else Color.Gray.copy(alpha = 0.7f)
    val iconText = if (isMet) "✓" else "○"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = iconText,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(16.dp)
        )
        Text(
            text = text,
            color = color,
            fontSize = 11.sp
        )
    }
}


