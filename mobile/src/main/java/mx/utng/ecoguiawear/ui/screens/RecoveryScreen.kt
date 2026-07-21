/**
 * Archivo: RecoveryScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Interfaz de usuario para la recuperación de acceso. Permite al usuario solicitar un enlace de restablecimiento.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.utng.ecoguiawear.ui.components.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import androidx.compose.ui.tooling.preview.Preview
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla de recuperación de contraseña.
 */
@Composable
fun RecoveryScreen(
    onSendClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("cesar@email.com") }

    EcoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EcoLogo()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Recuperar acceso",
                style = MaterialTheme.typography.headlineMedium,
                color = EcoGuiaColors.Text,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Te enviaremos un enlace seguro para restablecer tu contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = EcoGuiaColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EcoTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "CORREO ELECTRÓNICO"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EcoButton(
                    text = "Enviar enlace",
                    onClick = onSendClick
                )
                
                EcoButton(
                    text = "Volver a iniciar sesión",
                    onClick = onBackToLogin,
                    useGradient = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoveryScreenPreview() {
    EcoGuiaMobileTheme {
        RecoveryScreen({}, {})
    }
}
