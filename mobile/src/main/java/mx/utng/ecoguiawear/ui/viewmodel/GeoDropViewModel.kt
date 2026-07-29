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

    private val _targetSiteId = mutableStateOf<String?>(null)
    val targetSiteId: State<String?> = _targetSiteId

    private val _targetSiteName = mutableStateOf<String?>(null)
    val targetSiteName: State<String?> = _targetSiteName

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving


    private val _nearbyGeoDrops = mutableStateOf<List<RemoteGeoDrop>>(emptyList())
    val nearbyGeoDrops: State<List<RemoteGeoDrop>> = _nearbyGeoDrops

    val collectedGeoDropIds = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()

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
     * Verifica si un GeoDrop específico ya fue capturado/guardado por el usuario.
     */
    fun checkGeoDropStatus(userId: String, dropId: String) {
        if (dropId.isBlank()) return
        viewModelScope.launch {
            try {
                val isCollected = repository.isGeoDropCollected(userId, dropId)
                collectedGeoDropIds[dropId] = isCollected
            } catch (e: Exception) {
                android.util.Log.e("GeoDropVM", "Error verificando GeoDrop status: ${e.message}")
            }
        }
    }

    /**
     * Actualiza la distancia GPS hacia todos los Geo-Drops cercanos y selecciona el más próximo.
     */
    fun updateProximity(userLocation: Location?, userId: String = "") {
        if (userLocation == null || _geoDrops.value.isEmpty()) return

        val nearbyList = mutableListOf<RemoteGeoDrop>()
        var minDistance = Float.MAX_VALUE
        var closest: RemoteGeoDrop? = _closestGeoDrop.value

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
            val radius = drop.detectionRadiusM
            if (dist <= radius) {
                nearbyList.add(drop)
                if (userId.isNotBlank()) {
                    checkGeoDropStatus(userId, drop.id.orEmpty())
                }
            }

            if (dist < minDistance) {
                minDistance = dist
                if (closest == null) {
                    closest = drop
                }
            }
        }

        _nearbyGeoDrops.value = nearbyList
        if (closest == null && _geoDrops.value.isNotEmpty()) {
            closest = _geoDrops.value.first()
        }
        if (_closestGeoDrop.value == null || !nearbyList.contains(_closestGeoDrop.value)) {
            _closestGeoDrop.value = nearbyList.firstOrNull() ?: closest
        }
        _distanceToClosest.value = if (minDistance != Float.MAX_VALUE) minDistance.toInt() else null
    }

    fun selectGeoDrop(drop: RemoteGeoDrop, userId: String = "") {
        _closestGeoDrop.value = drop
        val dropId = drop.id
        if (userId.isNotBlank() && !dropId.isNullOrBlank()) {
            checkGeoDropStatus(userId, dropId)
        }
    }



    /**
     * Guarda en memoria la fotografía capturada y el sitio destino obligatorio.
     */
    fun setCapturedPhoto(file: File, siteId: String? = null, siteName: String? = null) {
        _capturedPhoto.value = file
        if (siteId != null) _targetSiteId.value = siteId
        if (siteName != null) _targetSiteName.value = siteName
    }

    /**
     * Establece el sitio obligatorio al que pertenecerá el Geo-Drop.
     */
    fun setTargetSite(siteId: String, siteName: String) {
        _targetSiteId.value = siteId
        _targetSiteName.value = siteName
    }

    private val firebaseStorageRepo = mx.utng.ecoguiawear.data.remote.FirebaseStorageRepository()

    /**
     * Ancla la fotografía capturada subiéndola a Firebase Storage e insertándola en Neon con su site_id obligatorio.
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

        val siteId = _targetSiteId.value
        if (siteId.isNullOrBlank()) {
            onError("La cápsula debe estar asociada obligatoriamente a un sitio histórico.")
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val photoFile = _capturedPhoto.value
                var mediaUrl: String? = null

                if (photoFile != null && photoFile.exists()) {
                    val uri = android.net.Uri.fromFile(photoFile)
                    // 1. Subir fotografía capturada a Firebase Storage
                    mediaUrl = firebaseStorageRepo.uploadImage(uri, folder = "geo_drops")
                }

                // 2. Guardar registro en Neon PostgreSQL asignando explícitamente el siteId
                val success = repository.createGeoDrop(
                    title = title,
                    description = description,
                    lat = location.latitude,
                    lng = location.longitude,
                    userId = userId,
                    siteId = siteId,
                    mediaUrl = mediaUrl
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



    /**
     * Guarda un Geo-Drop público detectado en el entorno a la colección personal del usuario.
     */
    fun saveExistingGeoDropToCollection(
        userId: String,
        geoDrop: RemoteGeoDrop,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val success = repository.saveGeoDropToCollection(
                    userId = userId,
                    geoDropId = geoDrop.id.orEmpty(),
                    siteId = geoDrop.siteId
                )
                if (success) {
                    onSuccess()
                } else {
                    onError("No se pudo agregar el Geo-Drop a tu colección.")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

