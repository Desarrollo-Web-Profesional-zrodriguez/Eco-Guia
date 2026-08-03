@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.tv.ui.screens.components.*

@Composable
fun LobbyScreen(
    onNavigateToHeatmap: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToPortal360: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }

    // Código PIN persistente por dispositivo TV (solo cambia al desvincular o cerrar sesión)
    var pairingCode by remember {
        val savedPin = prefs.getString("saved_pairing_code", null)
        val initialPin = if (!savedPin.isNullOrBlank()) {
            savedPin
        } else {
            val freshPin = (100000..999999).random().toString()
            prefs.edit().putString("saved_pairing_code", freshPin).apply()
            freshPin
        }
        mutableStateOf(initialPin)
    }

    var loggedUser by remember { mutableStateOf<RemoteUser?>(null) }
    var assignedSite by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    var isPairingSuccess by remember { mutableStateOf(prefs.getBoolean("saved_is_paired", false)) }
    var isRestoringSession by remember { mutableStateOf(true) }
    var availableSites by remember { mutableStateOf<List<RemoteHistoricalSite>>(emptyList()) }
    var isAdmin by remember { mutableStateOf(false) }
    var showSiteSelector by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Consultar sesión guardada o en Neon DB y escuchar comandos de transmisión vía MQTT instantáneo
    LaunchedEffect(pairingCode) {
        isRestoringSession = true
        // Suscribirse de inmediato al canal MQTT de la TV para responder a comandos remotos en < 100ms
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            mx.utng.ecoguia.shared.data.remote.HiveMQManager.subscribeToTvCommands(pairingCode) { command ->
                android.util.Log.d("TVLobby", "Comando MQTT recibido en TV callback: $command")
                mainHandler.post {
                    when (command) {
                        "public" -> onNavigateToPortal360()
                        "ranking" -> onNavigateToHeatmap()
                        "logout" -> {
                            prefs.edit()
                                .remove("saved_is_paired")
                                .remove("saved_paired_user_id")
                                .remove("saved_paired_user_email")
                                .remove("saved_paired_user_name")
                                .remove("saved_paired_user_role")
                                .remove("saved_selected_site_id")
                                .remove("saved_pairing_code")
                                .apply()
                            val newPin = (100000..999999).random().toString()
                            prefs.edit().putString("saved_pairing_code", newPin).apply()
                            pairingCode = newPin
                            loggedUser = null
                            assignedSite = null
                            availableSites = emptyList()
                            isAdmin = false
                            isPairingSuccess = false
                        }
                        else -> onNavigateToGallery()
                    }
                }
            }
        }

        // Si ya estaba vinculada previamente, restaurar datos del usuario
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedUserEmail = prefs.getString("saved_paired_user_email", null)
        if (savedUserId != null && savedUserEmail != null) {
            val savedRole = prefs.getString("saved_paired_user_role", "museum_hotel") ?: "museum_hotel"
            isAdmin = savedRole == "admin"
            loggedUser = RemoteUser(
                id = savedUserId,
                email = savedUserEmail,
                displayName = prefs.getString("saved_paired_user_name", "Usuario") ?: "Usuario",
                role = savedRole
            )
            isPairingSuccess = true
            // Cargar sitio activo guardado previamente o el del propietario
            val savedSiteId = prefs.getString("saved_selected_site_id", null)
            val sites = repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin)
            availableSites = sites
            assignedSite = if (savedSiteId != null) {
                sites.find { it.id == savedSiteId } ?: repository.getSiteByOwner(savedUserId)
            } else {
                repository.getSiteByOwner(savedUserId)
            }
        }
        isRestoringSession = false

        while (true) {
            try {
                if (!isPairingSuccess) {
                    val pairedUser = repository.getPairingStatus(pairingCode)
                    if (pairedUser != null) {
                        val userRole = pairedUser.role ?: "museum_hotel"
                        isAdmin = userRole == "admin"
                        loggedUser = pairedUser
                        isPairingSuccess = true
                        prefs.edit()
                            .putBoolean("saved_is_paired", true)
                            .putString("saved_paired_user_id", pairedUser.id)
                            .putString("saved_paired_user_email", pairedUser.email)
                            .putString("saved_paired_user_name", pairedUser.displayName)
                            .putString("saved_paired_user_role", userRole)
                            .putString("saved_pairing_code", pairingCode)
                            .apply()
                        val sites = repository.getSitesByOwnerOrAdmin(pairedUser.id, isAdmin)
                        availableSites = sites
                        assignedSite = sites.firstOrNull() ?: repository.getSiteByOwner(pairedUser.id)
                    }
                } else {
                    // Verificar si la sesión fue desvinculada remotamente desde el móvil
                    val checkId = loggedUser?.id ?: pairingCode
                    val currentStatus = repository.getPairingStatus(checkId)
                    if (currentStatus == null) {
                        android.util.Log.d("TVLobby", "Sesión desvinculada remotamente desde el móvil.")
                        prefs.edit()
                            .remove("saved_is_paired")
                            .remove("saved_paired_user_id")
                            .remove("saved_paired_user_email")
                            .remove("saved_paired_user_name")
                            .remove("saved_paired_user_role")
                            .remove("saved_selected_site_id")
                            .remove("saved_pairing_code")
                            .apply()
                        pairingCode = (100000..999999).random().toString()
                        loggedUser = null
                        assignedSite = null
                        availableSites = emptyList()
                        isAdmin = false
                        isPairingSuccess = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TVLobby", "Error en sondeo de TV: ${e.message}")
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    // ── Función de Cierre de Sesión ──────────────────────────────────────────
    fun performLogout() {
        coroutineScope.launch {
            repository.unlinkTvSession(pairingCode)
            prefs.edit()
                .remove("saved_is_paired")
                .remove("saved_paired_user_id")
                .remove("saved_paired_user_email")
                .remove("saved_paired_user_name")
                .remove("saved_paired_user_role")
                .remove("saved_selected_site_id")
                .remove("saved_pairing_code")
                .apply()
            pairingCode = (100000..999999).random().toString()
            loggedUser = null
            assignedSite = null
            availableSites = emptyList()
            isAdmin = false
            isPairingSuccess = false
            showLogoutDialog = false
        }
    }

    // ── UI Principal ──────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        if (isRestoringSession) {
            TvLoadingRestorationState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header del sitio
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = assignedSite?.name ?: "Eco-Guía Smart TV",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = assignedSite?.address ?: "Dolores Hidalgo - Museo & Cuna de la Independencia",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isPairingSuccess && loggedUser != null) {
                    TvConnectedStateCard(
                        loggedUser = loggedUser,
                        assignedSite = assignedSite,
                        pairingCode = pairingCode,
                        availableSites = availableSites,
                        isAdmin = isAdmin,
                        onChangeSiteClick = {
                            coroutineScope.launch {
                                val userId = loggedUser?.id ?: return@launch
                                val sites = repository.getSitesByOwnerOrAdmin(userId, isAdmin)
                                availableSites = sites
                                showSiteSelector = true
                            }
                        }
                    )

                    if (showSiteSelector) {
                        SiteSelectorDialog(
                            sites = availableSites,
                            currentSiteId = assignedSite?.id,
                            isAdmin = isAdmin,
                            onSiteSelected = { selected ->
                                assignedSite = selected
                                prefs.edit().putString("saved_selected_site_id", selected.id).apply()
                                showSiteSelector = false
                            },
                            onDismiss = { showSiteSelector = false }
                        )
                    }
                } else {
                    TvPairingCodeBlock(pairingCode = pairingCode)
                    Spacer(modifier = Modifier.height(28.dp))
                    TvPreviewNavigationButtons(
                        onNavigateToGallery = onNavigateToGallery,
                        onNavigateToPortal360 = onNavigateToPortal360,
                        onNavigateToHeatmap = onNavigateToHeatmap
                    )
                }
            }

            // Overlay botón de cerrar sesión
            if (isPairingSuccess) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        colors = IconButtonDefaults.colors(
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.9f),
                            focusedContainerColor = Color(0xFFEF4444).copy(alpha = 0.95f)
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        androidx.tv.material3.Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Diálogo confirmación de cierre de sesión
            if (showLogoutDialog) {
                TvLogoutConfirmDialog(
                    onConfirmLogout = { performLogout() },
                    onDismiss = { showLogoutDialog = false }
                )
            }
        }
    }
}
