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
                    put("name", "Soporte Eco-Guía")
                    put("email", "cesarenriquegaraygarcia50@gmail.com")
                }
                putJsonArray("to") {
                    add(buildJsonObject {
                        put("email", toEmail)
                    })
                }
                put("subject", "Recuperación de contraseña - EcoGuía")
                put("htmlContent", """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f4f7f6; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333;">
                        <div style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
                            <div style="background: linear-gradient(135deg, #004D40, #00796B); padding: 30px; text-align: center;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 24px; letter-spacing: 1px;">Eco-Guía</h1>
                            </div>
                            <div style="padding: 40px; text-align: center;">
                                <h2 style="color: #004D40; margin-top: 0; font-size: 22px;">Recuperación de Acceso</h2>
                                <p style="font-size: 16px; line-height: 1.6; color: #555555; margin-bottom: 30px;">
                                    Hola, hemos recibido una solicitud para restablecer la contraseña de tu cuenta en Eco-Guía.
                                </p>
                                <a href="$recoveryLink" style="display: inline-block; padding: 14px 32px; background-color: #D4AF37; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 30px; box-shadow: 0 4px 6px rgba(212, 175, 55, 0.3);">
                                    Restablecer Contraseña
                                </a>
                                <p style="font-size: 14px; line-height: 1.5; color: #999999; margin-top: 30px;">
                                    Si no fuiste tú quien solicitó este cambio, por favor ignora este correo. Tu cuenta sigue estando segura.
                                </p>
                            </div>
                            <div style="background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eeeeee;">
                                <p style="margin: 0; font-size: 12px; color: #aaaaaa;">
                                    &copy; 2026 Eco-Guía. Todos los derechos reservados.
                                </p>
                            </div>
                        </div>
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

    suspend fun sendOtpEmail(toEmail: String, username: String, otp: String): Boolean {
        if (apiKey == "TU_BREVO_API_KEY_AQUI") {
            android.util.Log.e("EmailService", "Falta configurar la API Key de Brevo.")
            kotlinx.coroutines.delay(2000)
            return true 
        }

        try {
            val payload = buildJsonObject {
                putJsonObject("sender") {
                    put("name", "Eco-Guía")
                    put("email", "cesarenriquegaraygarcia50@gmail.com")
                }
                putJsonArray("to") {
                    add(buildJsonObject {
                        put("email", toEmail)
                    })
                }
                put("subject", "Tu código de verificación - Eco-Guía")
                put("htmlContent", """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f4f7f6; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333;">
                        <div style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
                            <div style="background: linear-gradient(135deg, #004D40, #00796B); padding: 40px; text-align: center;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 28px; letter-spacing: 1px;">Verificación de Cuenta</h1>
                            </div>
                            <div style="padding: 40px; text-align: center;">
                                <h2 style="color: #004D40; margin-top: 0; font-size: 22px;">Hola, $username</h2>
                                <p style="font-size: 16px; line-height: 1.6; color: #555555; margin-bottom: 30px;">
                                    Para continuar con tu registro en Eco-Guía, por favor ingresa el siguiente código de 6 dígitos en la aplicación:
                                </p>
                                <div style="display: inline-block; padding: 20px 40px; background-color: #e8f5e9; border: 2px dashed #00796B; border-radius: 12px; margin-bottom: 30px;">
                                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #004D40;">$otp</span>
                                </div>
                                <p style="font-size: 14px; line-height: 1.5; color: #999999;">
                                    Si no solicitaste crear una cuenta, puedes ignorar este correo sin problema.
                                </p>
                            </div>
                            <div style="background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eeeeee;">
                                <p style="margin: 0; font-size: 12px; color: #aaaaaa;">
                                    &copy; 2026 Eco-Guía. El mundo en tus manos.
                                </p>
                            </div>
                        </div>
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
            android.util.Log.e("EmailService", "Error al enviar correo de OTP: ${e.message}")
            return false
        }
    }
}
