/**
 * Archivo: SiteRegistrationScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Pantalla inicial para dar de alta un sitio histórico (Paso 1).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteRegistrationScreen(
    viewModel: SiteRegistrationViewModel,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var siteName by viewModel.name
    var category by viewModel.siteType
    var customCategory by viewModel.customCategory
    var address by viewModel.address
    
    val categories by viewModel.categories
    val isLoadingCategories by viewModel.isLoadingCategories
    val suggestions by viewModel.addressSuggestions
    
    var expanded by remember { mutableStateOf(false) }

    // Reintentar carga si está vacío al entrar
    LaunchedEffect(Unit) {
        if (categories.isEmpty()) {
            viewModel.loadCategories()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ... (Header and Card remain similar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Alta de sitio", color = Color.White, fontSize = 14.sp)
                Text("Datos básicos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (siteName.isEmpty()) "Nuevo sitio" else siteName, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Ruta principal para visitantes locales y turistas.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Form Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Detalles del lugar", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    EcoTextField(
                        value = siteName, 
                        onValueChange = { siteName = it }, 
                        label = "NOMBRE DEL SITIO",
                        placeholder = "Ej. Parroquia de Dolores"
                    )
                }
                
                item {
                    // Selector de Categoría (Menú desplegable corregido)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        EcoTextField(
                            value = if (isLoadingCategories) "Cargando..." else category,
                            onValueChange = {},
                            label = "CATEGORÍA",
                            placeholder = "Selecciona una opción",
                            readOnly = true
                        )
                        
                        // Caja invisible para capturar el clic y evitar conflicto con el foco del TextField
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { 
                                    if (!isLoadingCategories) {
                                        android.util.Log.d("SiteRegScreen", "Click en categoría - Expandiendo menú")
                                        expanded = true 
                                    }
                                }
                        )
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(EcoGuiaColors.Surface)
                        ) {
                            if (categories.isEmpty() && !isLoadingCategories) {
                                DropdownMenuItem(
                                    text = { Text("No hay categorías", color = Color.Gray) },
                                    onClick = { expanded = false }
                                )
                            }
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name, color = Color.White) },
                                    onClick = {
                                        android.util.Log.d("SiteRegScreen", "Categoría seleccionada: ${cat.name}")
                                        category = cat.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                if (category == "Otro") {
                    item {
                        EcoTextField(
                            value = customCategory, 
                            onValueChange = { customCategory = it }, 
                            label = "ESPECIFICAR CATEGORÍA",
                            placeholder = "Ej. Monumento Natural"
                        )
                    }
                }
                
                item {
                    Column {
                        EcoTextField(
                            value = address, 
                            onValueChange = { 
                                address = it
                                viewModel.searchAddress(context, it)
                            }, 
                            label = "DIRECCIÓN",
                            placeholder = "Ej. Zacatecas 6, Centro, Dolores Hidalgo"
                        )
                        
                        if (suggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column {
                                    suggestions.forEach { suggestion ->
                                        Text(
                                            text = suggestion.getFullText(null).toString(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.onAddressSelected(suggestion) }
                                                .padding(12.dp),
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Guardar datos",
                onClick = onNext
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteRegistrationScreenPreview() {
    EcoGuiaMobileTheme {
        SiteRegistrationScreen(SiteRegistrationViewModel(), {})
    }
}

