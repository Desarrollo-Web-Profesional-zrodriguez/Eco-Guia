/**
 * Actividad principal y punto de entrada para la aplicación en Wear OS.
 *
 * Configura la pantalla de bienvenida (Splash Screen), solicita permisos de ubicación en tiempo de ejecución,
 * inicializa los sensores de orientación y GPS, y establece el grafo de navegación Wear Compose.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.data.wear.LocationHelper
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.data.wear.SensorHelper
import mx.utng.ecoguiawear.data.wear.WearMessageListener
import mx.utng.ecoguiawear.presentation.navigation.EcoGuiaWearNavGraph
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Actividad única de Wear OS que implementa la escucha de mensajes Wearable en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var messageListener: WearMessageListener
    private lateinit var locationHelper: LocationHelper
    private lateinit var sensorHelper: SensorHelper

    /**
     * Inicializa componentes del sistema, dependencias de hardware y la interfaz Compose.
     *
     * @param savedInstanceState Estado previo guardado en caso de recreación.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val repository = RadarRepositoryImpl(applicationContext)
        messageListener = WearMessageListener(repository)
        
        locationHelper = LocationHelper(applicationContext) { location ->
            repository.updateCurrentLocation(location.latitude, location.longitude)
        }

        sensorHelper = SensorHelper(applicationContext) { heading ->
            repository.updateHeading(heading)
        }
        sensorHelper.start()

        Wearable.getMessageClient(this).addListener(this)

        val factory = RadarViewModelFactory(
            repository = repository,
            hapticController = HapticController(applicationContext),
            phoneMessageClient = PhoneMessageClient(applicationContext)
        )

        setContent {
            val radarViewModel: RadarViewModel = viewModel(factory = factory)
            
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    locationHelper.startUpdates()
                }
            }

            LaunchedEffect(Unit) {
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }

            EcoGuiaWearTheme {
                EcoGuiaWearNavGraph(viewModel = radarViewModel)
            }
        }
    }

    /**
     * Delega el mensaje entrante recibido en primer plano al [WearMessageListener].
     *
     * @param event Evento con la información del mensaje recibido.
     */
    override fun onMessageReceived(event: MessageEvent) {
        if (::messageListener.isInitialized) {
            messageListener.onMessageReceived(event)
        }
    }

    /**
     * Libera las suscripciones de sensores y listeners de mensajería al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopUpdates()
        sensorHelper.stop()
        Wearable.getMessageClient(this).removeListener(this)
    }
}
