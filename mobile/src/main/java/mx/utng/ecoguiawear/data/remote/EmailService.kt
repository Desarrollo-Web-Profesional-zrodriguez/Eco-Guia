package mx.utng.ecoguiawear.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.Json

class EmailService {
    
    // REEMPLAZAR con la API KEY real proporcionada por Brevo
    private val apiKey = "TU_BREVO_API_KEY_AQUI" 
    private val apiUrl = "https://api.brevo.com/v3/smtp/email"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun sendPasswordRecoveryEmail(toEmail: String, recoveryLink: String): Boolean {
        if (apiKey == "TU_BREVO_API_KEY_AQUI") {
            android.util.Log.e("EmailService", "Falta configurar la API Key de Brevo.")
            // Para fines de prueba y demostración sin API KEY configurada
            kotlinx.coroutines.delay(2000)
            return true 
        }

        try {
            val payload = buildJsonObject {
                putJsonObject("sender") {
                    put("name", "Soporte EcoGuía")
                    put("email", "soporte@ecoguia.com")
                }
                putJsonArray("to") {
                    add(buildJsonObject {
                        put("email", toEmail)
                    })
                }
                put("subject", "Recuperación de contraseña - EcoGuía")
                put("htmlContent", """
                    <html>
                        <body>
                            <h2>Recuperación de Acceso</h2>
                            <p>Hola, has solicitado recuperar tu contraseña en EcoGuía.</p>
                            <p>Haz clic en el siguiente enlace seguro para restablecerla:</p>
                            <a href="$recoveryLink" style="padding: 10px 20px; background-color: #004D40; color: white; text-decoration: none; border-radius: 5px;">
                                Restablecer Contraseña
                            </a>
                            <p><br>Si no fuiste tú, ignora este correo.</p>
                        </body>
                    </html>
                """.trimIndent())
            }

            val response: HttpResponse = client.post(apiUrl) {
                header("api-key", apiKey)
                header("accept", "application/json")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            return response.status.isSuccess()
        } catch (e: Exception) {
            android.util.Log.e("EmailService", "Error al enviar correo: ${e.message}")
            return false
        }
    }
}
