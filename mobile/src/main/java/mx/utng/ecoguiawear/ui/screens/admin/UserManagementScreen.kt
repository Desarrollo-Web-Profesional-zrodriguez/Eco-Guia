/**
 * Archivo: UserManagementScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Pantalla de administración de roles de usuario. Permite al Administrador
 * consultar usuarios registrados y cambiar su rol entre Usuario Turista y Moderador Cultural.
 * Excluye explícitamente cualquier cuenta con rol Super Admin de la vista pública.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    userManagementViewModel: UserManagementViewModel = viewModel()
) {
    val users by userManagementViewModel.users
    val isLoading by userManagementViewModel.isLoading
    var selectedUserForRoleChange by remember { mutableStateOf<RemoteUser?>(null) }
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
                    "Asigna permisos de Moderador o Usuario Turista. Las cuentas Super Admin permanecen protegidas.",
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
                            onChangeRoleClick = { selectedUserForRoleChange = user }
                        )
                    }
                }
            }
        }
    }

    // Modal de Diálogo para cambiar rol
    if (selectedUserForRoleChange != null) {
        val user = selectedUserForRoleChange!!
        val isCurrentMod = user.role.lowercase() in listOf("moderator", "mod")

        AlertDialog(
            onDismissRequest = { selectedUserForRoleChange = null },
            title = { Text("Cambiar rol de usuario", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Usuario: ${user.displayName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Correo: ${user.email}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Selecciona el nuevo rol a asignar:")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newRole = if (isCurrentMod) "visitor" else "moderator"
                        userManagementViewModel.changeRole(
                            userId = user.id,
                            newRole = newRole,
                            onSuccess = {
                                snackbarMessage = "Rol cambiado exitosamente a $newRole."
                                selectedUserForRoleChange = null
                            },
                            onError = { err ->
                                snackbarMessage = err
                                selectedUserForRoleChange = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Jade)
                ) {
                    Text(if (isCurrentMod) "Degradar a Usuario Normal" else "Promover a Moderador")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForRoleChange = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserRoleCard(
    user: RemoteUser,
    onChangeRoleClick: () -> Unit
) {
    val isModerator = user.role.lowercase() in listOf("moderator", "mod")

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
                    .background(
                        if (isModerator) EcoGuiaColors.Gold.copy(alpha = 0.15f) else EcoGuiaColors.Jade.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isModerator) Icons.Default.Security else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isModerator) EcoGuiaColors.Gold else EcoGuiaColors.Jade
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
                color = if (isModerator) EcoGuiaColors.Gold.copy(alpha = 0.15f) else EcoGuiaColors.Jade.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isModerator) "MODERADOR" else "USUARIO",
                    color = if (isModerator) EcoGuiaColors.Gold else EcoGuiaColors.Jade,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            TextButton(onClick = onChangeRoleClick) {
                Text("Cambiar", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)
            }
        }
    }
}
