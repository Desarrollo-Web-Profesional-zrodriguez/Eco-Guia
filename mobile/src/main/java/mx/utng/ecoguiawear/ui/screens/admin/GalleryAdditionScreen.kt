/**
 * Archivo: GalleryAdditionScreen.kt
 *
 * Pantalla para agregar nuevas fotografías y metadatos a la galería oficial del sitio histórico.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.AdminBottomBar
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Pantalla composable para subir nuevos recursos fotográficos a la galería de administración.
 *
 * @param onAddClick Callback invocado para guardar la fotografía y sus etiquetas.
 * @param onNavigate Callback para navegar entre secciones de administración.
 */
@Composable
fun GalleryAdditionScreen(
    onAddClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var altText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            AdminBottomBar(currentRoute = "capsule_gallery", onNavigate = onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EcoGuiaColors.DeepBlue)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Column {
                    Text("Galería Oficial", color = EcoGuiaColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Carga de Medios", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp).align(Alignment.CenterEnd)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                // Image Picker Placeholder
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
                        onClick = { /* Pick image */ }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    null,
                                    tint = EcoGuiaColors.Jade,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Toca para seleccionar imagen", color = Color.White, fontWeight = FontWeight.Medium)
                                Text("JPG, PNG hasta 10MB", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Detalles del archivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    EcoTextField(
                        value = altText,
                        onValueChange = { altText = it },
                        label = "TEXTO ALTERNATIVO"
                    )
                }

                item {
                    EcoTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = "ETIQUETAS (SEPARADAS POR COMAS)"
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    EcoButton(
                        text = "Publicar en Galería",
                        onClick = onAddClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryAdditionScreenPreview() {
    EcoGuiaMobileTheme {
        GalleryAdditionScreen(onAddClick = {}, onNavigate = {})
    }
}
