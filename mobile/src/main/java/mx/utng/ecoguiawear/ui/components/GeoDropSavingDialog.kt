/**
 * Archivo: GeoDropSavingDialog.kt
 * Autor: Zahir Rodriguez / EcoGuia Team
 * Fecha de creación: 2026-08-05
 * Descripción: Dialog modal a pantalla completa para mostrar la progresión
 * del guardado de un GeoDrop en Firebase Storage y PostgreSQL con animaciones e íconos.
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

data class SaveStepInfo(
    val stepNumber: Int,
    val title: String,
    val detail: String,
    val icon: ImageVector
)

@Composable
fun GeoDropSavingDialog(
    currentStep: Int // 1: Ubicación, 2: Foto Firebase, 3: Guardando BD, 4: Éxito
) {
    val steps = listOf(
        SaveStepInfo(1, "Triangulando coordenadas estelares", "Localizando posición exacta en la Tierra", Icons.Default.Language),
        SaveStepInfo(2, "Transmitiendo fotografía a la nube", "Optimizando y enviando captura a Firebase", Icons.Default.CloudUpload),
        SaveStepInfo(3, "Sellando cápsula cultural", "Registrando historia y datos en la red", Icons.Default.Lock),
        SaveStepInfo(4, "¡GeoDrop listo en el mapa!", "Tu cápsula ya es parte del patrimonio", Icons.Default.CheckCircle)
    )

    val activeStep = steps.firstOrNull { it.stepNumber == currentStep } ?: steps[0]

    // Animación de pulso para el ícono central
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F2027).copy(alpha = 0.95f),
                            Color(0xFF203A43).copy(alpha = 0.95f),
                            Color(0xFF2C5364).copy(alpha = 0.95f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.DeepBlue.copy(alpha = 0.92f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EcoGuiaColors.Jade.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ícono Principal Pulsante
                    Box(
                        modifier = Modifier
                            .scale(if (currentStep < 4) pulseScale else 1f)
                            .size(76.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EcoGuiaColors.Jade.copy(alpha = 0.4f),
                                        EcoGuiaColors.Jade.copy(alpha = 0.1f)
                                    )
                                ),
                                CircleShape
                            )
                            .border(2.dp, EcoGuiaColors.Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = activeStep.icon,
                            contentDescription = null,
                            tint = if (currentStep == 4) EcoGuiaColors.Gold else Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Título y Subtítulo de Paso Activo
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeStep.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeStep.detail,
                            color = EcoGuiaColors.Gold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Barra de Progreso
                    val progressFloat = (currentStep.coerceAtMost(4) / 4f)
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EcoGuiaColors.Jade,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    // Lista de Pasos (Checklist)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        steps.take(3).forEach { step ->
                            val isCompleted = currentStep > step.stepNumber
                            val isCurrent = currentStep == step.stepNumber

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrent) EcoGuiaColors.Jade.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            when {
                                                isCompleted -> EcoGuiaColors.Jade
                                                isCurrent -> EcoGuiaColors.Gold
                                                else -> Color.White.copy(alpha = 0.2f)
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = step.stepNumber.toString(),
                                            color = if (isCurrent) EcoGuiaColors.DeepBlue else Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = step.title,
                                    color = when {
                                        isCompleted -> Color.White
                                        isCurrent -> EcoGuiaColors.Gold
                                        else -> Color.White.copy(alpha = 0.5f)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
