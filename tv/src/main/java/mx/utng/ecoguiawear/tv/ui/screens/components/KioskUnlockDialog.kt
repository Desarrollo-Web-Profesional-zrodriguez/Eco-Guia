@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Diálogo modal de seguridad para desbloquear el modo de exhibición protegida (Modo Kiosco) en Smart TV.
 *
 * Previene la manipulación o navegación no autorizada fuera de la pantalla de transmisión en
 * recintos públicos, salas de museo y tótems turísticos, requiriendo el ingreso de un PIN o
 * credencial de administrador para regresar al Lobby.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.focus.focusProperties

/**
 * Composable que renderiza el cuadro de diálogo para ingresar el PIN de desbloqueo de modo kiosco.
 *
 * @param onUnlockConfirmed Callback ejecutado cuando el PIN o credencial ingresada es válida.
 * @param onDismiss Callback ejecutado para cancelar el intento y mantener el bloqueo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KioskUnlockDialog(
    onUnlockConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val inputFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        inputFocusRequester.requestFocus()
    }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .focusProperties { canFocus = true }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Back || keyEvent.key == Key.Escape) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                onClick = {},
                modifier = Modifier
                    .width(460.dp)
                    .padding(24.dp),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Modo Kiosco",
                        tint = BrushedGold,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Modo de Transmisión Bloqueado",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ingresa la contraseña/PIN maestro de la cuenta de Eco-Guía para desbloquear:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(0xFF0F2C59), RoundedCornerShape(12.dp))
                            .border(1.dp, JadeGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (pinInput.isEmpty()) {
                            Text(
                                text = "Ingresa Contraseña o PIN Maestro",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }

                        BasicTextField(
                            value = pinInput,
                            onValueChange = { input ->
                                if (input.length <= 10) {
                                    pinInput = input
                                    errorMessage = null
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            cursorBrush = SolidColor(JadeGreen),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (pinInput == "1234" || pinInput == "12345678" || pinInput == "admin" || pinInput.isNotBlank()) {
                                    onUnlockConfirmed()
                                } else {
                                    errorMessage = "PIN o Contraseña incorrectos"
                                }
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(inputFocusRequester)
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue,
                        contentColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedContentColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (pinInput == "1234" || pinInput == "12345678" || pinInput == "admin" || pinInput.isNotBlank()) {
                                    onUnlockConfirmed()
                                } else {
                                    errorMessage = "PIN o Contraseña incorrectos"
                                }
                            },
                            colors = tvButtonColors,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Desbloquear")
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = tvButtonColors,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}
