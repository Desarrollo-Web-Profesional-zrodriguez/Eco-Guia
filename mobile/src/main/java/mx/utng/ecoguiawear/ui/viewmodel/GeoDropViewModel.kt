/**
 * Archivo: GeoDropViewModel.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: ViewModel encargado de gestionar el estado de los Geo-Drops (cápsulas digitales),
 * el cálculo dinámico de distancia GPS entre el usuario y las cápsulas en AR,
 * y la persistencia de fotografías ancladas hacia la base de datos Neon PostgreSQL.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository
import java.io.File

class GeoDropViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private val _geoDrops = mutableStateOf<List<RemoteGeoDrop>>(emptyList())
    val geoDrops: State<List<RemoteGeoDrop>> = _geoDrops

    private val _closestGeoDrop = mutableStateOf<RemoteGeoDrop?>(null)
    val closestGeoDrop: State<RemoteGeoDrop?> = _closestGeoDrop

    private val _distanceToClosest = mutableStateOf<Int?>(null)
    val distanceToClosest: State<Int?> = _distanceToClosest

    private val _capturedPhoto = mutableStateOf<File?>(null)
    val capturedPhoto: State<File?> = _capturedPhoto

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    /**
     * Carga todos los Geo-Drops registrados en la base de datos.
     */
    fun loadGeoDrops() {
        viewModelScope.launch {
            try {
                val drops = repository.getGeoDrops()
                _geoDrops.value = drops
            } catch (e: Exception) {
                android.util.Log.e("GeoDropVM", "Error al cargar Geo-Drops: ${e.message}", e)
            }
        }
    }

    /**
     * Actualiza la distancia GPS hacia el Geo-Drop más cercano en tiempo real.
     */
    fun updateProximity(userLocation: Location?) {
        if (userLocation == null || _geoDrops.value.isEmpty()) return

        var minDistance = Float.MAX_VALUE
        var closest: RemoteGeoDrop? = null

        _geoDrops.value.forEach { drop ->
            val dropLat = drop.latitude ?: return@forEach
            val dropLng = drop.longitude ?: return@forEach

            val results = FloatArray(1)
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                dropLat, dropLng,
                results
            )
            val dist = results[0]
            if (dist < minDistance) {
                minDistance = dist
                closest = drop
            }
        }

        _closestGeoDrop.value = closest
        _distanceToClosest.value = if (minDistance != Float.MAX_VALUE) minDistance.toInt() else null
    }

    /**
     * Guarda en memoria la fotografía capturada con CameraX.
     */
    fun setCapturedPhoto(file: File) {
        _capturedPhoto.value = file
    }

    /**
     * Ancla la fotografía capturada creando un nuevo Geo-Drop en la nube.
     */
    fun anchorGeoDrop(
        title: String,
        description: String,
        location: Location?,
        userId: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (location == null) {
            onError("No se pudo obtener la ubicación GPS actual.")
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val success = repository.createGeoDrop(
                    title = title,
                    description = description,
                    lat = location.latitude,
                    lng = location.longitude,
                    userId = userId
                )

                if (success) {
                    loadGeoDrops()
                    onSuccess()
                } else {
                    onError("No se pudo registrar la cápsula en la nube.")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            } finally {
                _isSaving.value = false
            }
        }
    }

}
