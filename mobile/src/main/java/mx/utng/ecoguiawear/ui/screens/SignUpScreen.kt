/**
 * Archivo: SignUpScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Interfaz de usuario para el registro de nuevos usuarios. Permite crear una cuenta en el sistema.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.AuthState
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla de registro.
 */
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState

    // Manejo de éxito de registro
    LaunchedEffect(authState) {
        if (authState is AuthState.Registered) {
            viewModel.resetState()
            onBackToLogin()
        }
    }

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

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = EcoGuiaColors.Jade
                    )
                } else {
                    EcoButton(
                        text = "Crear cuenta",
                        onClick = { 
                            if (name.isNotBlank() && email.isNotBlank() && password.length >= 8) {
                                viewModel.register(name, email, password)
                            }
                        }
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
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    EcoGuiaMobileTheme {
        SignUpScreen(AuthViewModel(), {}, {})
    }
}
