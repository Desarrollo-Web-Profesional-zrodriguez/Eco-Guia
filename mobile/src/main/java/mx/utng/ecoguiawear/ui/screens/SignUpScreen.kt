/**
 * Archivo: SignUpScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Interfaz de usuario para el registro de nuevos usuarios. 
 * Soporta nombres completos de forma flexible para evitar errores con apellidos.
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
 * Composable que representa la pantalla de registro con soporte para nombre completo.
 */
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
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
                text = if (authState is AuthState.AwaitingVerification) "Verifica tu correo" else "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = EcoGuiaColors.Text,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (authState is AuthState.AwaitingVerification) 
                    "Ingresa el código de 6 dígitos que enviamos a tu correo electrónico."
                else 
                    "Únete a la comunidad que descubre y comparte historia local.",
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
                if (authState is AuthState.AwaitingVerification) {
                    EcoTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        label = "CÓDIGO DE VERIFICACIÓN (6 DÍGITOS)"
                    )
                } else {
                    EcoTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
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
                }

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
                    if (authState is AuthState.AwaitingVerification) {
                        EcoButton(
                            text = "Verificar y Crear Cuenta",
                            onClick = { 
                                if (otpCode.length == 6) {
                                    viewModel.verifyOtp(otpCode)
                                }
                            }
                        )
                        
                        EcoButton(
                            text = "Cancelar",
                            onClick = { viewModel.resetState() },
                            useGradient = false
                        )
                    } else {
                        EcoButton(
                            text = "Crear cuenta",
                            onClick = { 
                                if (fullName.isNotBlank() && email.isNotBlank() && password.length >= 8) {
                                    viewModel.register(fullName, email, password)
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
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    EcoGuiaMobileTheme {
        SignUpScreen(AuthViewModel(), {}, {})
    }
}
