/**
 * Archivo: SiteContentScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Gestión del contenido histórico y descriptivo de un sitio (Paso 2).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SiteContentScreen(
    viewModel: SiteRegistrationViewModel,
    onNext: () -> Unit
) {
    var historyTitle by viewModel.historyTitle
    var shortDesc by viewModel.shortDesc
    var detailedDesc by viewModel.historyDesc

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            Column {
                Text("Contenido", color = Color.White, fontSize = 12.sp)
                Text("Historia del sitio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text("V", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Context Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Texto turístico curado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Secciones necesarias para datos históricos de la guía.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }

        // Form Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Descripción histórica", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            EcoTextField(value = historyTitle, onValueChange = { historyTitle = it }, label = "TÍTULO DE SECCIÓN")
            EcoTextField(
                value = shortDesc, 
                onValueChange = { shortDesc = it }, 
                label = "RESUMEN CORTO",
                modifier = Modifier.height(80.dp)
            )
            EcoTextField(
                value = detailedDesc, 
                onValueChange = { detailedDesc = it }, 
                label = "RELATO DETALLADO",
                modifier = Modifier.height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Box(modifier = Modifier.padding(16.dp)) {
            EcoButton(
                text = "Actualizar contenido",
                onClick = onNext
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteContentScreenPreview() {
    EcoGuiaMobileTheme {
        SiteContentScreen(SiteRegistrationViewModel(), {})
    }
}

