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
import mx.utng.ecoguiawear.data.repository.DemoRadarRepository
import mx.utng.ecoguiawear.data.wear.LocationHelper
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.data.wear.SensorHelper
import mx.utng.ecoguiawear.data.wear.WearMessageListener
import mx.utng.ecoguiawear.presentation.navigation.EcoGuiaWearNavGraph
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var messageListener: WearMessageListener
    private lateinit var locationHelper: LocationHelper
    private lateinit var sensorHelper: SensorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val repository = DemoRadarRepository(applicationContext)
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
                permissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }

            EcoGuiaWearTheme {
                EcoGuiaWearNavGraph(viewModel = radarViewModel)
            }
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (::messageListener.isInitialized) {
            messageListener.onMessageReceived(event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopUpdates()
        sensorHelper.stop()
        Wearable.getMessageClient(this).removeListener(this)
    }
}
