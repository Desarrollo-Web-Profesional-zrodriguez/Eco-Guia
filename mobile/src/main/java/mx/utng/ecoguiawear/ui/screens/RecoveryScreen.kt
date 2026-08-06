/**
 * Archivo: RecoveryScreen.kt
 *
 * Interfaz de usuario para la recuperación de acceso y restablecimiento de contraseña mediante código OTP de 6 dígitos enviado por correo.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.ui.components.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.AuthState
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel

import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Pantalla composable de recuperación de cuenta y restablecimiento de contraseña vía OTP.
 *
 * @param viewModel ViewModel de autenticación y gestión de credenciales.
 * @param onBackToLogin Callback para retornar a la pantalla de login.
 */
@Composable
fun RecoveryScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val authState by viewModel.authState

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    EcoBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EcoLogo()

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = when (authState) {
                            is AuthState.AwaitingPasswordReset -> "Ingresa tu código"
                            is AuthState.AwaitingNewPassword -> "Nueva contraseña"
                            else -> "Recuperar acceso"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = EcoGuiaColors.Text,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = when (authState) {
                            is AuthState.AwaitingPasswordReset -> "Escribe el código PIN de 6 dígitos que enviamos a tu correo electrónico."
                            is AuthState.AwaitingNewPassword -> "Escribe tu nueva contraseña (mínimo 6 caracteres)."
                            else -> "Te enviaremos un código PIN de 6 dígitos por correo para restablecer tu cuenta."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = EcoGuiaColors.Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (authState is AuthState.AwaitingNewPassword) {
                            EcoTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "NUEVA CONTRASEÑA",
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = EcoGuiaColors.Muted
                                        )
                                    }
                                }
                            )

                            PasswordRequirementsBox(password = newPassword)

                            Spacer(modifier = Modifier.height(16.dp))

                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(color = EcoGuiaColors.Gold)
                            } else {
                                EcoButton(
                                    text = "Guardar Nueva Contraseña",
                                    onClick = {
                                        keyboardController?.hide()
                                        val reqs = calculatePasswordRequirements(newPassword)
                                        if (!reqs.isValid) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("La contraseña no cumple con todos los requisitos de seguridad.")
                                            }
                                        } else {
                                            viewModel.confirmNewPassword(newPassword) {
                                                viewModel.resetState()
                                                onBackToLogin()
                                            }
                                        }
                                    }
                                )
                            }

                            EcoButton(
                                text = "Cancelar",
                                onClick = { viewModel.resetState() },
                                useGradient = false
                            )
                        } else if (authState is AuthState.AwaitingPasswordReset) {
                            EcoTextField(
                                value = otpCode,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    if (filtered.length <= 6) otpCode = filtered
                                },
                                label = "CÓDIGO DE VERIFICACIÓN (6 DÍGITOS)",
                                placeholder = "123456",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(color = EcoGuiaColors.Gold)
                            } else {
                                EcoButton(
                                    text = "Verificar Código PIN",
                                    onClick = {
                                        keyboardController?.hide()
                                        if (otpCode.length < 6) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Ingresa el código PIN completo de 6 dígitos.")
                                            }
                                        } else {
                                            viewModel.verifyPasswordResetOtp(otpCode)
                                        }
                                    }
                                )
                            }

                            EcoButton(
                                text = "Cancelar",
                                onClick = { viewModel.resetState() },
                                useGradient = false
                            )
                        } else {
                            EcoTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "CORREO ELECTRÓNICO",
                                placeholder = "ejemplo@correo.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(color = EcoGuiaColors.Gold)
                            } else {
                                EcoButton(
                                    text = "Enviar Código PIN",
                                    onClick = {
                                        keyboardController?.hide()
                                        val trimmedEmail = email.trim()
                                        if (trimmedEmail.isEmpty()) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Por favor ingresa tu correo electrónico.")
                                            }
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("El correo electrónico no tiene un formato válido.")
                                            }
                                        } else {
                                            viewModel.sendRecoveryEmail(trimmedEmail, {}, {})
                                        }
                                    }
                                )
                            }

                            EcoButton(
                                text = "Volver al inicio de sesión",
                                onClick = onBackToLogin,
                                useGradient = false
                            )
                        }
                    }
                }
            }

            // Snackbar Verde de Alerta en la parte superior
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
            ) { data ->
                Snackbar(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoveryScreenPreview() {
    EcoGuiaMobileTheme {
        RecoveryScreen(AuthViewModel(), {})
    }
}
