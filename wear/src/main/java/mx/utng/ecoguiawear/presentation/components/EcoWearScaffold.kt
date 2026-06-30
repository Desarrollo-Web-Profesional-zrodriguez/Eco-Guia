package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun EcoWearScaffold(
    modifier: Modifier = Modifier,
    content: ScalingLazyListScope.() -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 28.dp),
        modifier = modifier
            .fillMaxSize()
            .background(EcoGuiaColors.Background),
        content = content
    )
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = EcoGuiaColors.Gold,
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = EcoGuiaColors.Muted,
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
