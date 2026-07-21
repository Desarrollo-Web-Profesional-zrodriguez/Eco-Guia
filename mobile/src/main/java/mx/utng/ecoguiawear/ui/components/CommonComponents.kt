/**
 * Archivo: CommonComponents.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Biblioteca de componentes de UI reutilizables para mantener la consistencia visual en la aplicación.
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Fondo con degradado vertical oficial de la aplicación.
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
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = EcoGuiaColors.Muted, fontSize = 12.sp) },
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
        singleLine = true
    )
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
