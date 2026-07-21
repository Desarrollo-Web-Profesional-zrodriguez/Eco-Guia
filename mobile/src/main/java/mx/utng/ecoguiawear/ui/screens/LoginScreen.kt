package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

import androidx.compose.ui.tooling.preview.Preview
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onRecoverClick: () -> Unit
) {
    var email by remember { mutableStateOf("cesar@email.com") }
    var password by remember { mutableStateOf("********") }

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
                text = "Eco-Guía Dolores",
                style = MaterialTheme.typography.headlineMedium,
                color = EcoGuiaColors.Text,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Entra para guardar tus rutas, cápsulas y colección cultural.",
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
                
                EcoTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "CONTRASEÑA"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                EcoButton(
                    text = "Iniciar sesión",
                    onClick = onLoginClick
                )
                
                EcoButton(
                    text = "Crear cuenta nueva",
                    onClick = onSignUpClick,
                    useGradient = false
                )
                
                TextButton(
                    onClick = onRecoverClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Olvidé mi contraseña",
                        color = EcoGuiaColors.Muted,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    EcoGuiaMobileTheme {
        LoginScreen({}, {}, {})
    }
}
