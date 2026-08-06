/**
 * Archivo: EditProfileScreen.kt
 *
 * Pantalla de edición de perfil. Permite al usuario modificar su nombre completo y datos de biografía.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * Pantalla composable para editar el perfil del usuario.
 *
 * @param user Datos actuales del usuario remoto.
 * @param onSaveClick Callback invocado con el nuevo nombre completo y biografía para guardar cambios.
 */
@Composable
fun EditProfileScreen(
    user: RemoteUser?,
    onSaveClick: (String, String) -> Unit
) {
    var fullName by remember { mutableStateOf(user?.displayName ?: "") }
    var publicUser by remember { mutableStateOf("@${user?.displayName?.lowercase()?.replace(" ", "_") ?: "usuario"}") }
    var bio by remember { mutableStateOf(user?.bio ?: "Me gusta descubrir detalles históricos y compartir cápsulas.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
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
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            item {
                EcoTextField(value = fullName, onValueChange = { fullName = it }, label = "NOMBRE COMPLETO")
            }
            
            item {
                EcoTextField(value = publicUser, onValueChange = { publicUser = it }, label = "USUARIO PÚBLICO")
            }
            
            item {
                EcoTextField(
                    value = bio, 
                    onValueChange = { bio = it }, 
                    label = "BIOGRAFÍA",
                    modifier = Modifier.height(100.dp)
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            item {
                EcoButton(
                    text = "Guardar cambios",
                    onClick = { if (fullName.isNotBlank()) onSaveClick(fullName, bio) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EcoGuiaMobileTheme {
        EditProfileScreen(null, { _, _ -> })
    }
}

