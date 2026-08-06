/**
 * Archivo: SignUpScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-08-01
 * Descripción: Interfaz de usuario para el registro de nuevos usuarios en EcoGuía.
 * 
 * Funciones importantes:
 * - SignUpScreen: Formulario de registro con validaciones de nombre completo, correo electrónico válido,
 *   contraseña oculta con icono de visibilidad (ojo), restricción de código OTP PIN a 6 dígitos numéricos,
 *   ocultamiento automático del teclado e indicador de alertas en Snackbar verde superior.
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
 * Composable que representa la pantalla de registro con soporte para nombre completo,
 * contraseña oculta con icono de ojo, validaciones de código OTP de 6 dígitos y Snackbar superior.
 */
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var otpCode by rememberSaveable { mutableStateOf("") }
    val authState by viewModel.authState

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Manejo de éxito de registro
    LaunchedEffect(authState) {
        if (authState is AuthState.Registered) {
            viewModel.resetState()
            onBackToLogin()
        }
    }

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
                        text = if (authState is AuthState.AwaitingVerification) "Verifica tu correo" else "Crea tu cuenta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EcoGuiaColors.Text,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (authState is AuthState.AwaitingVerification)
                            "Ingresa el código PIN de 6 dígitos que enviamos a tu correo electrónico."
                        else
                            "Únete a la comunidad que descubre y comparte historia local.",
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (authState is AuthState.AwaitingVerification) {
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
                        } else {
                            EcoTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = "NOMBRE COMPLETO"
                            )

                            EcoTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "CORREO ELECTRÓNICO",
                                placeholder = "ejemplo@correo.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            EcoTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "CONTRASEÑA",
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

                            PasswordRequirementsBox(password = password)
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
                                        keyboardController?.hide()
                                        if (otpCode.length < 6) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Ingresa el código PIN completo de 6 dígitos.")
                                            }
                                        } else {
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
                                    text = "Enviar Código de Verificación",
                                    onClick = {
                                        keyboardController?.hide()
                                        val trimmedName = fullName.trim()
                                        val trimmedEmail = email.trim()
                                        val reqs = calculatePasswordRequirements(password)

                                        val errorMsg = when {
                                            trimmedName.isEmpty() -> "Por favor ingresa tu nombre completo."
                                            trimmedEmail.isEmpty() -> "Por favor ingresa tu correo electrónico."
                                            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "El correo electrónico no tiene un formato válido."
                                            !reqs.isValid -> "La contraseña no cumple con todos los requisitos de seguridad."
                                            else -> null
                                        }

                                        if (errorMsg != null) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(errorMsg)
                                            }
                                        } else {
                                            viewModel.register(trimmedName, trimmedEmail, password)
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
fun SignUpScreenPreview() {
    EcoGuiaMobileTheme {
        SignUpScreen(AuthViewModel(), {}, {})
    }
}
