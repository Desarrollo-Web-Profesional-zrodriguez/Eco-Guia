/**
 * Archivo: UserManagementScreen.kt
 *
 * Pantalla de administración de roles de usuario. Permite al Administrador
 * consultar usuarios registrados y cambiar su rol entre Usuario Turista y Moderador Cultural.
 * Excluye explícitamente cualquier cuenta con rol Super Admin de la vista pública.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.UserManagementViewModel

/**
 * Pantalla composable para gestionar cuentas de usuario, asignación y revocación de permisos de moderador.
 *
 * @param onBack Callback para regresar al panel anterior.
 * @param userManagementViewModel ViewModel de administración de usuarios.
 */
@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    userManagementViewModel: UserManagementViewModel = viewModel()
) {
    val users by userManagementViewModel.users
    val isLoading by userManagementViewModel.isLoading
    var selectedUserForRoleChange by remember { mutableStateOf<RemoteUser?>(null) }
    var selectedUserForDelete by remember { mutableStateOf<RemoteUser?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(top = 48.dp, start = 16.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Column {
                    Text("Panel de Control", color = Color.White, fontSize = 14.sp)
                    Text("Gestión de Roles", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { userManagementViewModel.loadUsers() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
            }
        }

        // Card de Información
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Administración de Usuarios", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Asigna permisos de Moderador, Museo o Turista, o elimina cuentas definitivamente del sistema.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Listado de usuarios
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Usuarios Registrados (${users.size})",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGuiaColors.Jade)
                }
            } else if (users.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron usuarios", fontWeight = FontWeight.Bold)
                        Text("No hay usuarios adicionales registrados en el sistema.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(users) { user ->
                        UserRoleCard(
                            user = user,
                            onChangeRoleClick = { selectedUserForRoleChange = user },
                            onDeleteClick = { selectedUserForDelete = user }
                        )
                    }
                }
            }
        }
    }

    // Modal de Diálogo para cambiar rol
    if (selectedUserForRoleChange != null) {
        val user = selectedUserForRoleChange!!

        AlertDialog(
            onDismissRequest = { selectedUserForRoleChange = null },
            title = { Text("Asignar rol a usuario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Usuario: ${user.displayName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Correo: ${user.email}", fontSize = 12.sp, color = Color.Gray)
                    Text("Rol actual: ${user.role.uppercase()}", fontSize = 12.sp, color = EcoGuiaColors.Jade, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selecciona el nuevo rol a asignar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Opción: Usuario Turista
                    Button(
                        onClick = {
                            userManagementViewModel.changeRole(
                                userId = user.id,
                                newRole = "visitor",
                                onSuccess = {
                                    snackbarMessage = "Rol actualizado a Turista (visitor)."
                                    selectedUserForRoleChange = null
                                },
                                onError = { err ->
                                    snackbarMessage = err
                                    selectedUserForRoleChange = null
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Jade.copy(alpha = 0.2f), contentColor = EcoGuiaColors.Jade)
                    ) {
                        Text("Turista / Visitante (visitor)")
                    }

                    // Opción: Moderador Cultural
                    Button(
                        onClick = {
                            userManagementViewModel.changeRole(
                                userId = user.id,
                                newRole = "moderator",
                                onSuccess = {
                                    snackbarMessage = "Rol actualizado a Moderador (moderator)."
                                    selectedUserForRoleChange = null
                                },
                                onError = { err ->
                                    snackbarMessage = err
                                    selectedUserForRoleChange = null
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Gold.copy(alpha = 0.2f), contentColor = EcoGuiaColors.Gold)
                    ) {
                        Text("Moderador Cultural (moderator)")
                    }

                    // Opción: Museo / Hotel / Establecimiento
                    Button(
                        onClick = {
                            userManagementViewModel.changeRole(
                                userId = user.id,
                                newRole = "museum_hotel",
                                onSuccess = {
                                    snackbarMessage = "Rol actualizado a Museo / Hotel (museum_hotel)."
                                    selectedUserForRoleChange = null
                                },
                                onError = { err ->
                                    snackbarMessage = err
                                    selectedUserForRoleChange = null
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.DeepBlue, contentColor = Color.White)
                    ) {
                        Text("Museo / Hotel (museum_hotel)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedUserForRoleChange = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Confirmación para Eliminar Usuario por el Administrador
    if (selectedUserForDelete != null) {
        val user = selectedUserForDelete!!
        AlertDialog(
            onDismissRequest = { selectedUserForDelete = null },
            title = { Text("¿Eliminar usuario?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Se eliminará definitivamente la cuenta de ${user.displayName} (${user.email}).", fontSize = 13.sp)
                    Text("Toda la información y registros asociados a este usuario serán eliminados en cascada de la base de datos.", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userManagementViewModel.deleteUser(
                            userId = user.id,
                            onSuccess = {
                                snackbarMessage = "Usuario eliminado con éxito."
                                selectedUserForDelete = null
                            },
                            onError = { err ->
                                snackbarMessage = err
                                selectedUserForDelete = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar definitivamente", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedUserForDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserRoleCard(
    user: RemoteUser,
    onChangeRoleClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val roleLower = user.role.lowercase()
    val isModerator = roleLower in listOf("moderator", "mod")
    val isMuseum = roleLower in listOf("museum_hotel", "museum", "hotel")

    val badgeText = when {
        isMuseum -> "MUSEO / HOTEL"
        isModerator -> "MODERADOR"
        else -> "TURISTA"
    }

    val badgeColor = when {
        isMuseum -> EcoGuiaColors.Gold
        isModerator -> EcoGuiaColors.Jade
        else -> Color(0xFF64B5F6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isMuseum -> Icons.Default.Security
                        isModerator -> Icons.Default.Security
                        else -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = badgeColor
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = if (isMuseum) EcoGuiaColors.DeepBlue else badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    color = if (isMuseum) Color.White else badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            TextButton(onClick = onChangeRoleClick) {
                Text("Rol", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Eliminar usuario",
                    tint = Color.Red.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

