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

@Composable
fun SignUpScreen(
    onSignUpClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("César Martínez") }
    var email by remember { mutableStateOf("cesar@email.com") }
    var password by remember { mutableStateOf("Mínimo 8 caracteres") }

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
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = EcoGuiaColors.Text,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Únete a la comunidad que descubre y comparte historia local.",
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
                    value = name,
                    onValueChange = { name = it },
                    label = "NOMBRE COMPLETO"
                )
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EcoButton(
                    text = "Crear cuenta",
                    onClick = onSignUpClick
                )
                
                EcoButton(
                    text = "Ya tengo cuenta",
                    onClick = onBackToLogin,
                    useGradient = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    EcoGuiaMobileTheme {
        SignUpScreen({}, {})
    }
}
