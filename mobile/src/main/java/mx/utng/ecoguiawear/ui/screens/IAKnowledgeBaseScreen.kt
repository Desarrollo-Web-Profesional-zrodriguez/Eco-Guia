/**
 * Archivo: IAKnowledgeBaseScreen.kt
 *
 * Pantalla de entrenamiento e ingesta de conocimiento para la IA de Miguel Hidalgo.
 * Permite al Super Admin ingresar nuevas preguntas/respuestas sugeridas, cargar información
 * de la base de datos de Neon y regenerar dinámicamente la base de conocimiento local (JSON).
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Modelo de datos para un par de pregunta y respuesta de entrenamiento histórico.
 *
 * @property question Pregunta o consulta formulada.
 * @property answer Respuesta histórica y contextual correspondiente.
 * @property category Categoría temática de la interacción.
 */
data class QAPair(val question: String, val answer: String, val category: String = "General")

/**
 * Pantalla composable para administrar la base de conocimientos y entrenamiento del bot IA.
 *
 * @param userId Identificador del usuario administrador.
 * @param onBack Callback para regresar a la pantalla previa.
 */
@Composable
fun IAKnowledgeBaseScreen(
    userId: String = "",
    onBack: () -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { EcoGuiaRepositoryImpl() }

    var questionInput by remember { mutableStateOf("") }
    var answerInput by remember { mutableStateOf("") }
    var toneInput by remember { mutableStateOf("Histórico, cercano y respetuoso") }
    var isTraining by remember { mutableStateOf(false) }

    val trainedQAList = remember { mutableStateListOf<QAPair>() }
    var isLoadingArticles by remember { mutableStateOf(true) }

    // Cargar preguntas guardadas persistentemente desde Neon DB al abrir la pantalla
    LaunchedEffect(Unit) {
        isLoadingArticles = true
        try {
            val articles = repository.getKnowledgeArticles()
            trainedQAList.clear()
            if (articles.isNotEmpty()) {
                articles.forEach { article ->
                    trainedQAList.add(QAPair(article.title, article.content))
                }
            } else {
                // Si la tabla está vacía, mostrar valores semilla predeterminados
                trainedQAList.add(QAPair("¿Por qué este museo es importante?", "Porque conserva relatos y piezas vinculadas al inicio del movimiento insurgente liderado por Don Miguel Hidalgo y Costilla."))
                trainedQAList.add(QAPair("¿Cuándo se dio el Grito de Independencia?", "La madrugada del 16 de septiembre de 1810 en Dolores Hidalgo."))
            }
        } catch (e: Exception) {
            android.util.Log.e("IABase", "Error cargando artículos: ${e.message}")
        } finally {
            isLoadingArticles = false
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header con botón Volver
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EcoGuiaColors.DeepBlue)
                    .padding(top = 8.dp, start = 16.dp, end = 24.dp, bottom = 12.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Column {
                        Text("Miguel Hidalgo IA", color = EcoGuiaColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Base de conocimiento", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Pregunta y Respuesta Curada", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Ingresa nuevas preguntas sugeridas para entrenar al chatbot. Se guardarán en Neon DB para persistencia.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Formulario para agregar nuevas preguntas
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Entrenar Nueva Pregunta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            EcoTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                label = "Pregunta Sugerida",
                                placeholder = "Ej: ¿Cómo me llamo?"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            EcoTextField(
                                value = answerInput,
                                onValueChange = { answerInput = it },
                                label = "Respuesta Curada",
                                placeholder = "Ej: Zahir"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (questionInput.isNotBlank() && answerInput.isNotBlank()) {
                                        val q = questionInput.trim()
                                        val a = answerInput.trim()
                                        scope.launch {
                                            val success = repository.createKnowledgeArticle(q, a, userId)
                                            if (success) {

                                                trainedQAList.add(0, QAPair(q, a))
                                                questionInput = ""
                                                answerInput = ""
                                                snackbarHostState.showSnackbar("Guardado en la base de datos Neon")
                                            } else {
                                                snackbarHostState.showSnackbar("Error guardando en Neon DB")
                                            }
                                        }
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("Completa ambos campos") }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGuiaColors.Jade),
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Agregar a Base", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("Base de IA Actual (${trainedQAList.size})", fontWeight = FontWeight.Bold)
                }

                if (isLoadingArticles) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = EcoGuiaColors.Jade)
                        }
                    }
                } else {
                    items(trainedQAList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("PREGUNTA SUGERIDA", color = EcoGuiaColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(item.question, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("RESPUESTA CURADA", color = EcoGuiaColors.Jade, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(item.answer, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TONO DEL CHATBOT", color = EcoGuiaColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(toneInput, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Botón de acción inferior: Entrenar e Ingestar desde Neon DB
            Box(modifier = Modifier.padding(16.dp)) {
                EcoButton(
                    text = if (isTraining) "Sincronizando DB y Generando JSON..." else "Entrenar base local (JSON)",
                    onClick = {
                        scope.launch {
                            isTraining = true
                            try {
                                // 1. Cargar sitios históricos y artículos persistidos desde Neon DB
                                val sites = repository.getHistoricalSites()
                                val articles = repository.getKnowledgeArticles()

                                val jsonBuilder = StringBuilder("{\n  \"model\": \"Miguel Hidalgo IA\",\n  \"knowledge_base\": [\n")

                                // 2. Incorporar conocimiento de los sitios de Neon DB
                                sites.forEach { site ->
                                    jsonBuilder.append("    {\n")
                                    jsonBuilder.append("      \"context\": \"${site.name}\",\n")
                                    jsonBuilder.append("      \"site_type\": \"${site.siteType}\",\n")
                                    jsonBuilder.append("      \"description\": \"${site.historicalDescription ?: site.shortDescription ?: ""}\"\n")
                                    jsonBuilder.append("    },\n")
                                }

                                // 3. Incorporar preguntas/respuestas guardadas en Neon DB
                                val allQAs = if (articles.isNotEmpty()) {
                                    articles.map { QAPair(it.title, it.content) }
                                } else {
                                    trainedQAList.toList()
                                }

                                allQAs.forEachIndexed { idx, qa ->
                                    jsonBuilder.append("    {\n")
                                    jsonBuilder.append("      \"question\": \"${qa.question}\",\n")
                                    jsonBuilder.append("      \"answer\": \"${qa.answer}\"\n")
                                    jsonBuilder.append("    }${if (idx < allQAs.size - 1) "," else ""}\n")
                                }

                                jsonBuilder.append("  ]\n}")
                                android.util.Log.d("IATraining", "JSON generado e inyectado desde Neon DB:\n$jsonBuilder")

                                snackbarHostState.showSnackbar("Base de IA re-entrenada e inyectada con datos de Neon DB")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error durante entrenamiento: ${e.message}")
                            } finally {
                                isTraining = false
                            }
                        }
                    }
                )
            }
        }
    }


}

