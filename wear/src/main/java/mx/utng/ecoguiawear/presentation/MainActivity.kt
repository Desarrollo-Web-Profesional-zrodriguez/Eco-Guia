package mx.utng.ecoguiawear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.repository.DemoRadarRepository
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.data.wear.WearMessageListener
import mx.utng.ecoguiawear.presentation.navigation.EcoGuiaWearNavGraph
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var messageListener: WearMessageListener

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val repository = DemoRadarRepository(applicationContext)
        messageListener = WearMessageListener(repository)

        Wearable.getMessageClient(this).addListener(this)

        val factory = RadarViewModelFactory(
            repository = repository,
            hapticController = HapticController(applicationContext),
            phoneMessageClient = PhoneMessageClient(applicationContext)
        )

        setContent {
            val radarViewModel: RadarViewModel = viewModel(factory = factory)
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
        Wearable.getMessageClient(this).removeListener(this)
    }
}
