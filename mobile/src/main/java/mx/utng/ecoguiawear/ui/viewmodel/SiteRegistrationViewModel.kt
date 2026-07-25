/**
 * Archivo: SiteRegistrationViewModel.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Gestiona el flujo de registro de un nuevo sitio histórico en 4 pasos,
 * manteniendo el estado temporal y capturando la ubicación GPS actual.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.AutocompletePrediction
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteCategory
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class SiteRegistrationViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    // Catálogos
    private val _categories = mutableStateOf<List<RemoteCategory>>(emptyList())
    val categories: State<List<RemoteCategory>> = _categories

    private val _isLoadingCategories = mutableStateOf(false)
    val isLoadingCategories: State<Boolean> = _isLoadingCategories

    // Paso 1: Datos Básicos
    var name = mutableStateOf("")
    var siteType = mutableStateOf("")
    var customCategory = mutableStateOf("")
    var address = mutableStateOf("")
    
    // Google Places Suggestions
    private val _addressSuggestions = mutableStateOf<List<AutocompletePrediction>>(emptyList())
    val addressSuggestions: State<List<AutocompletePrediction>> = _addressSuggestions

    // Paso 2: Contenido
    var shortDesc = mutableStateOf("")
    var historyDesc = mutableStateOf("")

    // Paso 3: Ubicación
    var latitude = mutableStateOf(0.0)
    var longitude = mutableStateOf(0.0)
    var radiusM = mutableStateOf(50)

    // Paso 4: Operación
    var hours = mutableStateOf("")
    var cost = mutableStateOf("")
    var selectedAccessibility = mutableStateOf(setOf<String>())

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoadingCategories.value = true
            android.util.Log.d("SiteRegVM", "Iniciando carga de categorías...")
            try {
                val result = repository.getSiteCategories()
                android.util.Log.d("SiteRegVM", "Categorías obtenidas: ${result.size}")
                if (result.isEmpty()) {
                    android.util.Log.w("SiteRegVM", "La lista de categorías regresó vacía desde el servidor.")
                }
                _categories.value = result
            } catch (e: Exception) {
                android.util.Log.e("SiteRegVM", "Error al cargar categorías: ${e.message}", e)
            } finally {
                _isLoadingCategories.value = false
            }
        }
    }

    /**
     * Busca sugerencias de dirección usando Google Places API.
     */
    fun searchAddress(context: Context, query: String) {
        if (query.length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }

        if (!Places.isInitialized()) {
            val apiKey = context.packageManager
                .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                .metaData.getString("com.google.android.geo.API_KEY")
            if (apiKey != null) Places.initialize(context, apiKey)
        }

        val placesClient = Places.createClient(context)
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                _addressSuggestions.value = response.autocompletePredictions
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SiteRegVM", "Error en Places Autocomplete: ${e.message}")
            }
    }

    fun onAddressSelected(prediction: AutocompletePrediction) {
        address.value = prediction.getFullText(null).toString()
        _addressSuggestions.value = emptyList()
    }

    /**
     * Captura la ubicación actual del dispositivo para anclar el sitio.
     */
    @SuppressLint("MissingPermission")
    fun captureCurrentLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let {
                    latitude.value = it.latitude
                    longitude.value = it.longitude
                }
            }
    }

    /**
     * Envía los datos finales al repositorio para persistencia.
     */
    fun registerSite(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            
            // Si la categoría es "Otro", usamos la personalizada
            val finalCategory = if (siteType.value == "Otro") customCategory.value else siteType.value
            val finalAccessibility = selectedAccessibility.value.joinToString(", ")

            val success = repository.createHistoricalSite(
                name = name.value,
                siteType = finalCategory,
                address = address.value,
                shortDesc = shortDesc.value,
                historyDesc = historyDesc.value,
                lat = latitude.value,
                lng = longitude.value,
                radiusM = radiusM.value,
                hours = hours.value,
                cost = cost.value,
                accessibility = finalAccessibility
            )
            _isSaving.value = false
            if (success) onSuccess() else onError("Error al guardar en el servidor.")
        }
    }
}
