/**
 * Archivo: FirebaseStorageRepository.kt
 * Autor: ZahirAndres / CesarEnrique
 * Fecha de última actualización: 2026-07-27
 * Descripción: Repositorio para la gestión de la subida y almacenamiento de imágenes en Firebase Storage.
 */

package mx.utng.ecoguiawear.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
}
