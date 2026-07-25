/**
 * Archivo: ChatViewModel.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Gestiona la comunicación con la API de Google Gemini para simular
 * la personalidad de Miguel Hidalgo y Costilla.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel : ViewModel() {

    private val apiKey = "AQ.Ab8RN6KsA71S9YkKyiZreIDs35JgoLBXQxZVQGlz8fz_o-7JyA"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey,
        systemInstruction = content { 
            text("Eres Don Miguel Hidalgo y Costilla, el Padre de la Patria de México. " +
                 "Responde siempre con patriotismo, sabiduría y un tono histórico colonial. " +
                 "Tu misión es educar a los visitantes sobre la historia de Dolores Hidalgo " +
                 "y la gesta de Independencia. Eres valiente, culto y apasionado por la libertad. " +
                 "Asegúrate de usar acentos y puntuación correctamente en español.")
        }
    )

    private val chat = generativeModel.startChat()

    private val _messages = mutableStateListOf<ChatMessage>(
        ChatMessage("¡Salve, patriota! Soy Miguel Hidalgo. ¿Qué deseas saber sobre la cuna de nuestra libertad?", false)
    )
    val messages: List<ChatMessage> = _messages

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        _messages.add(ChatMessage(userText, true))
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userText)
                val aiResponse = response.text ?: "Mis disculpas, la comunicación se ha visto interrumpida por las fuerzas realistas."
                _messages.add(ChatMessage(aiResponse, false))
            } catch (e: Exception) {
                android.util.Log.e("ChatVM", "Error al contactar a Miguel Hidalgo: ${e.message}")
                val friendlyError = when {
                    e.message?.contains("403") == true -> "¡Cáspita! La llave de acceso (API Key) parece ser inválida o no tiene permisos."
                    e.message?.contains("404") == true -> "El oráculo de la sabiduría (Modelo IA) no responde en esta frecuencia."
                    e.message?.contains("429") == true -> "¡Por todos los cielos! Los recursos de sabiduría (créditos) se han agotado en este proyecto."
                    else -> "¡Zafarrancho! Un error técnico nos acecha: ${e.message}"
                }
                _messages.add(ChatMessage(friendlyError, false))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
