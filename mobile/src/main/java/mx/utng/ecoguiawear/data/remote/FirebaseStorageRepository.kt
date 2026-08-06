/**
 * Archivo: FirebaseStorageRepository.kt
 *
 * Repositorio para la gestión de subida, almacenamiento y obtención de URLs públicas de imágenes
 * capturadas (cápsulas GeoDrop, sitios históricos y fotos de perfil) en Firebase Storage.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Cliente de almacenamiento en la nube respaldado por Firebase Cloud Storage.
 */
class FirebaseStorageRepository {

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Sube un archivo de imagen capturado localmente (Uri) a Firebase Storage.
     * @param imageUri Uri de la imagen tomada con la cámara o seleccionada de la galería.
     * @param folder Carpeta destino ('geo_drops', 'sites', etc.). Por defecto 'geo_drops'.
     * @return La URL pública HTTPS de la imagen subida en Firebase Storage, o null si ocurrió un error.
     */
    suspend fun uploadImage(imageUri: Uri, folder: String = "geo_drops"): String? {
        return try {
            val filename = "$folder/${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child(filename)
            
            // Subir archivo a la nube de Firebase
            fileRef.putFile(imageUri).await()
            
            // Recuperar URL pública firmada de descarga
            val downloadUrl = fileRef.downloadUrl.await().toString()
            android.util.Log.d("FirebaseStorage", "Imagen subida exitosamente a Firebase Storage: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("FirebaseStorage", "Error al subir imagen a Firebase Storage: ${e.message}", e)
            null
        }
    }

    /**
     * Elimina un archivo de Firebase Storage dada su URL pública de descarga.
     * @param imageUrl URL pública firmada de descarga (ej: https://firebasestorage.googleapis.com/...)
     * @return true si se eliminó exitosamente de Firebase Storage, false en caso contrario.
     */
    suspend fun deleteImageFromUrl(imageUrl: String): Boolean {
        if (imageUrl.isBlank() || !imageUrl.contains("firebasestorage.googleapis.com")) return false
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            android.util.Log.d("FirebaseStorage", "Imagen eliminada exitosamente de Firebase Storage: $imageUrl")
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseStorage", "Error al eliminar imagen de Firebase Storage ($imageUrl): ${e.message}", e)
            false
        }
    }
}


