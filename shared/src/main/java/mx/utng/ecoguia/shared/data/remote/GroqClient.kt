package mx.utng.ecoguia.shared.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.statement.*

class GroqClient(
    private val apiKey: String = "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"
) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    private val baseUrl = "https://api.groq.com/openai/v1/chat/completions"

    suspend fun chat(messages: List<GroqMessage>, model: String = "llama-3.3-70b-versatile"): String {
        return try {
            val response: HttpResponse = client.post(baseUrl) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(GroqChatRequest(model = model, messages = messages))
            }

            if (response.status.isSuccess()) {
                val chatResponse: GroqChatResponse = response.body()
                chatResponse.choices.firstOrNull()?.message?.content ?: "Sin respuesta del General."
            } else {
                val errorBody = response.bodyAsText()
                "Error en Groq API (${response.status}): $errorBody"
            }
        } catch (e: Exception) {
            "Excepción al contactar a la inteligencia: ${e.message}"
        }
    }

    fun close() {
        client.close()
    }
}
