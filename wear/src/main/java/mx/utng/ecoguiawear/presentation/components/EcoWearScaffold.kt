/**
 * Estructura visual base (Scaffold) y soporte para bisel rotatorio (Rotary Input) en Wear OS.
 *
 * Envuelve las pantallas dentro de un [androidx.wear.compose.material3.ScreenScaffold] con soporte
 * para [androidx.wear.compose.foundation.lazy.ScalingLazyColumn] y fondo negro AMOLED de alta eficiencia energética.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.android.awaitFrame
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Contenedor estándar para listas escalables con soporte de corona rotatoria o bisel físico.
 *
 * @param modifier Modificador visual opcional.
 * @param requestFocus Indica si el contenedor debe solicitar el foco rotatorio al componerse.
 * @param content Bloque DSL con los elementos a renderizar dentro de [ScalingLazyColumn].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun EcoWearScaffold(
    modifier: Modifier = Modifier,
    requestFocus: Boolean = true,
    content: ScalingLazyListScope.() -> Unit
) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            awaitFrame()
            focusRequester.requestFocus()
        }
    }

    ScreenScaffold(
        scrollState = listState,
        modifier = modifier
            .fillMaxSize()
            .background(EcoGuiaColors.Background)
    ) {
        ScalingLazyColumn(
            state = listState,
            autoCentering = null,
            modifier = Modifier
                .fillMaxSize()
                .rotaryScrollable(
                    behavior = RotaryScrollableDefaults.behavior(listState),
                    focusRequester = focusRequester
                )
                .focusRequester(focusRequester),
            contentPadding = PaddingValues(start = 10.dp, top = 24.dp, end = 10.dp, bottom = 24.dp),
            content = content
        )
    }
}

/**
 * Encabezado común para títulos de sección y subtítulos en pantallas Wear OS.
 *
 * @param title Título principal de la vista.
 * @param subtitle Subtítulo o descripción breve opcional.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = EcoGuiaColors.Gold,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = EcoGuiaColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
