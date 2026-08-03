@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import mx.utng.ecoguiawear.tv.ui.screens.MapViewType
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

@Composable
fun MapStyleSelectorDialog(
    selectedMapType: MapViewType,
    onSelectMapType: (MapViewType) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogButtonFocusRequester = remember { FocusRequester() }

    androidx.activity.compose.BackHandler(enabled = true) {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        dialogButtonFocusRequester.requestFocus()
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                onClick = {},
                modifier = Modifier
                    .width(500.dp)
                    .padding(24.dp),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar Estilo del Mapa 3D",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Elige la representación visual para la transmisión en pantalla:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue,
                        contentColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedContentColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onSelectMapType(MapViewType.MINIMAL_WHITE) },
                            colors = tvButtonColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(dialogButtonFocusRequester)
                        ) {
                            Text(if (selectedMapType == MapViewType.MINIMAL_WHITE) "✓ 1. Maqueta 3D Blanca (Minimalista)" else "1. Maqueta 3D Blanca (Minimalista)")
                        }

                        Button(
                            onClick = { onSelectMapType(MapViewType.DARK_MODE) },
                            colors = tvButtonColors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedMapType == MapViewType.DARK_MODE) "✓ 2. Modo Oscuro Neón (Futurista)" else "2. Modo Oscuro Neón (Futurista)")
                        }

                        Button(
                            onClick = { onSelectMapType(MapViewType.SATELLITE_CITY) },
                            colors = tvButtonColors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedMapType == MapViewType.SATELLITE_CITY) "✓ 3. Vista Satelital (Entorno Real)" else "3. Vista Satelital (Entorno Real)")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        colors = tvButtonColors
                    ) {
                        Text("Cerrar")
                    }

                }
            }
        }
    }
}
