/**
 * Archivo: SiteRegistrationViewModel.kt
 *
 * Gestiona el formulario wizard de registro de nuevos sitios turísticos e históricos en 4 pasos,
 * autocompletado de direcciones con Google Places y captura de coordenadas GPS.
 *
 * @since 2026-08-05
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

/**
 * ViewModel que preserva el estado del formulario de alta de sitios y orquesta su persistencia en la base de datos remota.
 *
 * @param repository Repositorio de datos para consulta de categorías y creación de sitios históricos.
 */
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
    var historyTitle = mutableStateOf("")
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

    // Asignación de Propietario (Solamente cuentas con rol museum_hotel)
    var selectedOwnerUserId = mutableStateOf<String?>(null)
    private val _museumUsers = mutableStateOf<List<mx.utng.ecoguia.shared.domain.model.RemoteUser>>(emptyList())
    val museumUsers: State<List<mx.utng.ecoguia.shared.domain.model.RemoteUser>> = _museumUsers

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving

    init {
        loadCategories()
        loadMuseumUsers()
    }

    fun loadMuseumUsers() {
        viewModelScope.launch {
            try {
                _museumUsers.value = repository.getUsersByRole("museum_hotel")
            } catch (e: Exception) {
                android.util.Log.e("SiteRegVM", "Error al cargar usuarios museum_hotel: ${e.message}", e)
            }
        }
    }

    /**
     * Consulta el catálogo de categorías disponibles desde la base de datos remota.
     */
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
     * Busca sugerencias de dirección en tiempo real usando Google Places Autocomplete API.
     *
     * @param context Contexto de la aplicación.
     * @param query Texto de búsqueda ingresado.
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

    /**
     * Aplica la predicción de dirección seleccionada por el usuario.
     *
     * @param prediction Predicción seleccionada de la lista autocompletada.
     */
    fun onAddressSelected(prediction: AutocompletePrediction) {
        address.value = prediction.getFullText(null).toString()
        _addressSuggestions.value = emptyList()
    }

    /**
     * Captura las coordenadas GPS actuales del dispositivo en alta precisión para asignarlas al nuevo sitio.
     *
     * @param context Contexto de la aplicación.
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
     * Limpia completamente todos los campos del formulario para un nuevo registro.
     */
    fun resetForm() {
        name.value = ""
        siteType.value = ""
        customCategory.value = ""
        address.value = ""
        _addressSuggestions.value = emptyList()
        historyTitle.value = ""
        shortDesc.value = ""
        historyDesc.value = ""
        latitude.value = 0.0
        longitude.value = 0.0
        radiusM.value = 50
        hours.value = ""
        cost.value = ""
        selectedAccessibility.value = emptySet()
    }

    /**
     * Envía los datos finales al repositorio para persistencia asignando el creador activo.
     */
    fun registerSite(ownerUserId: String? = null, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            
            // Si la categoría es "Otro", usamos la personalizada
            val finalCategory = if (siteType.value == "Otro") customCategory.value else siteType.value
            val finalAccessibility = selectedAccessibility.value.joinToString(", ")
            val finalHistoryDesc = if (historyTitle.value.isNotBlank()) "${historyTitle.value.trim()}\n\n${historyDesc.value}" else historyDesc.value

            val targetOwnerUserId = selectedOwnerUserId.value ?: ownerUserId
            val createdId = repository.createHistoricalSite(
                name = name.value,
                siteType = finalCategory,
                address = address.value,
                shortDesc = shortDesc.value,
                historyDesc = finalHistoryDesc,
                lat = latitude.value,
                lng = longitude.value,
                radiusM = radiusM.value,
                hours = hours.value,
                cost = cost.value,
                accessibility = finalAccessibility,
                ownerUserId = targetOwnerUserId
            )
            _isSaving.value = false
            if (createdId.isNotBlank()) {
                resetForm()
                onSuccess(createdId)
            } else {
                onError("Error al guardar en el servidor.")
            }
        }
    }

}
