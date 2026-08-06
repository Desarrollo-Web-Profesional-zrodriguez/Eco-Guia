/**
 * Archivo: TvPairingRepositoryExt.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-30
 * Descripción: Extensiones de repositorio para vinculación de Smart TVs por QR/PIN y comandos MQTT.
 */
package mx.utng.ecoguia.shared.data.repository.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteDevice
import mx.utng.ecoguia.shared.domain.model.RemoteUser

suspend fun EcoGuiaRepositoryImpl.getUserDevicesExt(userId: String): List<RemoteDevice> {
    val query = "SELECT id, user_id, type::text, name, device_identifier, is_active, last_seen_at::text FROM devices WHERE user_id::text = $1 AND is_active = TRUE ORDER BY created_at DESC"
    return try {
        neonClient.executeQuery(query, listOf(userId))
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al obtener dispositivos del usuario: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.registerDeviceExt(userId: String, name: String, type: String, deviceIdentifier: String): Boolean {
    val query = """
        INSERT INTO devices (user_id, name, type, device_identifier, is_active, last_seen_at)
        VALUES ($1::uuid, $2, $3::device_type, $4, TRUE, NOW())
        ON CONFLICT DO NOTHING
    """.trimIndent()
    return try {
        val rows = neonClient.executeCommand(query, listOf(userId, name, type, deviceIdentifier))
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al registrar dispositivo: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.unlinkDeviceExt(deviceId: String): Boolean {
    return try {
        val queryGetDev = "SELECT id, user_id, type, name, device_identifier, is_active FROM devices WHERE id::text = $1 LIMIT 1"
        val devRows: List<RemoteDevice> = neonClient.executeQuery(queryGetDev, listOf(deviceId))
        val rawId = devRows.firstOrNull()?.deviceIdentifier.orEmpty()
        val pairingCode = if (rawId.contains("TV-PIN-")) rawId.substringAfter("TV-PIN-") else rawId

        val queryDelete = "DELETE FROM devices WHERE id::text = $1"
        val rows = neonClient.executeCommand(queryDelete, listOf(deviceId))

        if (pairingCode.isNotBlank()) {
            val queryPairing = "DELETE FROM device_pairings WHERE pairing_code = $1 OR pairing_code LIKE $1 || ':%'"
            neonClient.executeCommand(queryPairing, listOf(pairingCode))
            
            CoroutineScope(Dispatchers.IO).launch {
                mx.utng.ecoguia.shared.data.remote.HiveMQManager.publishProgramCommand(pairingCode, "logout")
            }
        }
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al desvincular dispositivo: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.unlinkTvSessionExt(pairingCode: String): Boolean {
    CoroutineScope(Dispatchers.IO).launch {
        mx.utng.ecoguia.shared.data.remote.HiveMQManager.publishProgramCommand(pairingCode, "logout")
    }

    val deletePairing = """
        DELETE FROM device_pairings
        WHERE pairing_code = $1 OR pairing_code LIKE $1 || ':%'
    """.trimIndent()
    val deleteDevice = """
        DELETE FROM devices
        WHERE device_identifier = $1
    """.trimIndent()
    return try {
        neonClient.executeCommand(deletePairing, listOf(pairingCode))
        neonClient.executeCommand(deleteDevice, listOf("TV-PIN-$pairingCode"))
        true
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al cerrar sesión TV: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.deactivateAllUserPairingsExt(userId: String): Boolean {
    val queryPairings = "DELETE FROM device_pairings WHERE user_id::text = $1"
    val queryDevices = "DELETE FROM devices WHERE user_id::text = $1 AND type::text = 'tv'"
    return try {
        neonClient.executeCommand(queryPairings, listOf(userId))
        neonClient.executeCommand(queryDevices, listOf(userId))
        true
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al eliminar vinculaciones del usuario: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.pairDeviceByCodeExt(userId: String, pairingCode: String): Boolean {
    val checkQuery = "SELECT pairing_code FROM device_pairings WHERE (pairing_code = $1 OR pairing_code LIKE $1 || ':%') LIMIT 1"
    
    val insertTvDevice = """
        INSERT INTO devices (user_id, name, type, device_identifier, is_active, last_seen_at)
        VALUES ($1::uuid, 'Smart TV - Emisión Lobby', 'tv'::device_type, 'TV-PIN-' || $2, TRUE, NOW())
        ON CONFLICT DO NOTHING
    """.trimIndent()

    val deletePrevious = """
        DELETE FROM device_pairings
        WHERE (pairing_code = $1::text OR pairing_code LIKE $1::text || ':%')
    """.trimIndent()

    val insertPairing = """
        INSERT INTO device_pairings (user_id, pairing_code, is_active)
        VALUES ($1::uuid, $2::text, TRUE)
    """.trimIndent()

    return try {
        val existing: List<Map<String, String>> = neonClient.executeQuery(checkQuery, listOf(pairingCode))
        val isValidPinFormat = pairingCode.length == 6 && pairingCode.all { it.isDigit() }
        if (existing.isEmpty() && !isValidPinFormat) {
            return false
        }

        if (userId.isNotBlank()) {
            neonClient.executeCommand(insertTvDevice, listOf(userId, pairingCode))
        }
        neonClient.executeCommand(deletePrevious, listOf(pairingCode))
        neonClient.executeCommand(insertPairing, listOf(if (userId.isBlank()) "2603b469-aa27-4eed-a6aa-dbce7fc145f5" else userId, pairingCode))
        true
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al registrar vinculación QR: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.getPairingStatusExt(pairingCode: String): RemoteUser? {
    val query = """
        SELECT u.id, u.email, u.display_name, u.role::text, u.avatar_url, u.created_at::text
        FROM device_pairings dp
        JOIN users u ON dp.user_id = u.id
        WHERE (dp.pairing_code = $1 OR dp.user_id::text = $1) AND dp.is_active = TRUE
        LIMIT 1
    """.trimIndent()
    return try {
        val users: List<RemoteUser> = neonClient.executeQuery(query, listOf(pairingCode))
        users.firstOrNull()
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al obtener estado de pairing: ${e.message}", e)
        null
    }
}

suspend fun EcoGuiaRepositoryImpl.setTvTransmissionProgramExt(pairingCode: String, programType: String): Boolean {
    return try {
        CoroutineScope(Dispatchers.IO).launch {
            mx.utng.ecoguia.shared.data.remote.HiveMQManager.publishProgramCommand(pairingCode, programType)
        }
        true
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al transmitir programa vía MQTT a la TV $pairingCode: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.getTvActiveProgramExt(pairingCode: String): String? {
    return null
}
