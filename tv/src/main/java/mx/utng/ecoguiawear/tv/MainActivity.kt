@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Actividad principal del módulo Smart TV de Eco-Guía Dolores Hidalgo.
 *
 * Sirve como punto de entrada de la aplicación en dispositivos Android TV / Google TV,
 * configurando el contenedor raíz con el tema visual personalizado y delegando la
 * orquestación del flujo de pantallas al host de navegación de TV.
 *
 * Se utiliza la anotación `@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)`
 * para habilitar de manera global las APIs experimentales de Compose for TV (Material 3 para TV),
 * requeridas para componentes optimizados con foco direccional (D-Pad).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.utng.ecoguiawear.tv.ui.theme.EcoGuiaTVTheme
import mx.utng.ecoguiawear.tv.ui.navigation.SmartTVNavHost

/**
 * Actividad principal para la experiencia en pantalla grande de Eco-Guía.
 *
 * Hereda de [ComponentActivity] para proporcionar compatibilidad nativa y ligera
 * con Jetpack Compose en entornos Android TV sin la sobrecarga del framework de fragmentos clásico.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class MainActivity : ComponentActivity() {

    /**
     * Inicializa la actividad y establece la jerarquía de composables.
     *
     * Envuelve la aplicación dentro de [EcoGuiaTVTheme] y renderiza [SmartTVNavHost]
     * dentro de un contenedor que ocupa la totalidad de la pantalla.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoGuiaTVTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SmartTVNavHost()
                }
            }
        }
    }
}
