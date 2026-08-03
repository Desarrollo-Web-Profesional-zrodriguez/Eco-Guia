/**
 * Archivo: AuthRepositoryExt.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-30
 * Descripción: Extensiones de repositorio para autenticación, registros y perfiles de usuario.
 */
package mx.utng.ecoguia.shared.data.repository.extensions

import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteUser

suspend fun EcoGuiaRepositoryImpl.loginExt(email: String, passwordHash: String): RemoteUser? {
    val query = """
        SELECT id, email, display_name, role::text, bio, avatar_url, created_at::text
        FROM users
        WHERE email = $1 AND password_hash = $2
        LIMIT 1
    """.trimIndent()
    return try {
        val users: List<RemoteUser> = neonClient.executeQuery(query, listOf(email, passwordHash))
        users.firstOrNull()
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error en login ext: ${e.message}", e)
        null
    }
}

suspend fun EcoGuiaRepositoryImpl.registerExt(
    email: String,
    passwordHash: String,
    displayName: String,
    role: String
): RemoteUser? {
    val insertUser = """
        INSERT INTO users (email, password_hash, display_name, role)
        VALUES ($1, $2, $3, $4::user_role)
        RETURNING id, email, display_name, role::text, bio, avatar_url, created_at::text
    """.trimIndent()
    return try {
        val users: List<RemoteUser> = neonClient.executeQuery(
            insertUser,
            listOf(email, passwordHash, displayName, role)
        )
        users.firstOrNull()
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error en registro ext: ${e.message}", e)
        null
    }
}

suspend fun EcoGuiaRepositoryImpl.getUserProfileExt(userId: String): RemoteUser? {
    val query = """
        SELECT id, email, display_name, role::text, bio, avatar_url, created_at::text
        FROM users
        WHERE id::text = $1
        LIMIT 1
    """.trimIndent()
    return try {
        val users: List<RemoteUser> = neonClient.executeQuery(query, listOf(userId))
        users.firstOrNull()
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo perfil ext: ${e.message}", e)
        null
    }
}

suspend fun EcoGuiaRepositoryImpl.updateUserProfileExt(userId: String, displayName: String, bio: String?, avatarUrl: String?): Boolean {
    val query = """
        UPDATE users
        SET display_name = $1, bio = $2
        WHERE id::text = $3
    """.trimIndent()

    return try {
        val rows = neonClient.executeCommand(query, listOf(displayName, bio.orEmpty(), userId))
        android.util.Log.d("EcoGuiaRepo", "Perfil y biografía actualizados en DB. Filas afectadas: $rows para userId: $userId")
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error actualizando perfil y biografía en DB: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.getUsersByRoleExt(role: String): List<RemoteUser> {
    val query = """
        SELECT id, email, display_name, role::text, avatar_url, created_at::text
        FROM users
        WHERE role::text = $1
        ORDER BY display_name ASC
    """.trimIndent()
    return try {
        neonClient.executeQuery(query, listOf(role))
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo usuarios por rol ext: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.resetUserPasswordExt(email: String, newPasswordHash: String): Boolean {
    val query = """
        UPDATE users
        SET password_hash = $1
        WHERE email = $2
    """.trimIndent()
    return try {
        val rows = neonClient.executeCommand(query, listOf(newPasswordHash, email))
        android.util.Log.d("EcoGuiaRepo", "Contraseña restablecida en DB para: $email. Filas afectadas: $rows")
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al restablecer contraseña en DB: ${e.message}", e)
        false
    }
}
