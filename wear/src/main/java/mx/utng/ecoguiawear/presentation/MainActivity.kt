package mx.utng.ecoguiawear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wearable.Wearable
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.repository.DemoRadarRepository
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.data.wear.WearMessageListener
import mx.utng.ecoguiawear.presentation.navigation.EcoGuiaWearNavGraph
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

class MainActivity : ComponentActivity() {

    private lateinit var messageListener: WearMessageListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = DemoRadarRepository(applicationContext)
        messageListener = WearMessageListener(repository)

        Wearable.getMessageClient(this).addListener { event ->
            messageListener.onMessageReceived(event)
        }

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
}
