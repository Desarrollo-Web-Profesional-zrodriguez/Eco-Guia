/**
 * Archivo: EditProfileScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla de edición de perfil. Permite al usuario modificar sus datos personales.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.ui.components.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla de edición de perfil.
 */
@Composable
fun EditProfileScreen(
    user: RemoteUser?,
    onSaveClick: () -> Unit
) {
    var name by remember { mutableStateOf(user?.displayName?.split(" ")?.getOrNull(0) ?: "") }
    var lastName by remember { mutableStateOf(user?.displayName?.split(" ")?.getOrNull(1) ?: "") }
    var publicUser by remember { mutableStateOf("@${user?.displayName?.lowercase()?.replace(" ", "_") ?: "usuario"}") }
    var bio by remember { mutableStateOf("Me gusta descubrir detalles históricos y compartir cápsulas.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F4F1))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Column {
                Text("Editar perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Datos personales", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    "Editar datos",
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.DeepBlue
                )
            }
            
            item {
                EcoTextField(value = name, onValueChange = { name = it }, label = "NOMBRE")
            }
            
            item {
                EcoTextField(value = lastName, onValueChange = { lastName = it }, label = "APELLIDO")
            }
            
            item {
                EcoTextField(value = publicUser, onValueChange = { publicUser = it }, label = "USUARIO PÚBLICO")
            }
            
            item {
                EcoTextField(
                    value = bio, 
                    onValueChange = { bio = it }, 
                    label = "BIOGRAFÍA",
                    modifier = Modifier.height(100.dp) // Simulación de textarea
                )
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            item {
                EcoButton(
                    text = "Guardar cambios",
                    onClick = onSaveClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EcoGuiaMobileTheme {
        EditProfileScreen(null, {})
    }
}
