/**
 * Archivo: LoginScreen.kt
 *
 * Interfaz de usuario para el inicio de sesión. Permite al usuario ingresar sus credenciales,
 * autenticarse en la base de datos Neon y navegar hacia registro o recuperación de contraseña.
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
 * Pantalla composable de autenticación e inicio de sesión.
 * Incluye validaciones de correo, contraseña oculta con icono de visibilidad, alertas y diseño responsivo.
 *
 * @param viewModel ViewModel de autenticación del usuario.
 * @param onLoginSuccess Callback invocado tras la autenticación exitosa.
 * @param onSignUpClick Callback para navegar a la pantalla de creación de cuenta.
 * @param onRecoverClick Callback para navegar a la pantalla de recuperación de contraseña.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onRecoverClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val authState by viewModel.authState
    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Manejo de éxito de login
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    EcoBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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

                        if (authState is AuthState.Error) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = Color.Red,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = EcoGuiaColors.Jade
                            )
                        } else {
                            EcoButton(
                                text = "Iniciar sesión",
                                onClick = {
                                    keyboardController?.hide()
                                    val trimmedEmail = email.trim()
                                    val errorMsg = when {
                                        trimmedEmail.isEmpty() -> "Por favor ingresa tu correo electrónico."
                                        !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "El correo electrónico no tiene un formato válido."
                                        password.isEmpty() -> "Por favor ingresa tu contraseña."
                                        else -> null
                                    }

                                    if (errorMsg != null) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(errorMsg)
                                        }
                                    } else {
                                        viewModel.login(trimmedEmail, password)
                                    }
                                }
                            )

                            EcoButton(
                                text = "Crear cuenta nueva",
                                onClick = onSignUpClick,
                                useGradient = false
                            )
                        }

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

            // Snackbar Verde de Alerta en la parte superior (evita traslape con teclado)
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
fun LoginScreenPreview() {
    EcoGuiaMobileTheme {
        LoginScreen(AuthViewModel(), {}, {}, {})
    }
}
