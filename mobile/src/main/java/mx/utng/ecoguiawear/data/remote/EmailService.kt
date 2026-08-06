/**
 * Archivo: EmailService.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-30
 * Descripción: Servicio de envío de correos transaccionales (OTP y recuperación de contraseña) vía API v3 de Brevo usando los colores corporativos de Eco-Guía.
 */

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
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.Json

class EmailService {
    private val apiKey = mx.utng.ecoguiawear.BuildConfig.BREVO_API_KEY
    private val apiUrl = "https://api.brevo.com/v3/smtp/email"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun sendPasswordRecoveryEmail(toEmail: String, recoveryOtp: String): Boolean {
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
                put("subject", "Código de Recuperación - EcoGuía")
                put("htmlContent", """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #050B10; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #F7FAFC;">
                        <div style="max-width: 600px; margin: 40px auto; background-color: #0E2A3F; border-radius: 16px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.4); border: 1px solid #26A69A;">
                            <div style="background: linear-gradient(135deg, #05111A, #0E2A3F); padding: 35px; text-align: center; border-bottom: 2px solid #C5A059;">
                                <h1 style="color: #C5A059; margin: 0; font-size: 28px; letter-spacing: 2px;">ECO-GUÍA</h1>
                            </div>
                            <div style="padding: 40px; text-align: center;">
                                <h2 style="color: #26A69A; margin-top: 0; font-size: 22px;">Código de Restablecimiento</h2>
                                <p style="font-size: 16px; line-height: 1.6; color: #B8C6D1; margin-bottom: 30px;">
                                    Ingresa el siguiente código de 6 dígitos en la aplicación móvil para restablecer tu contraseña:
                                </p>
                                <div style="display: inline-block; padding: 18px 42px; background-color: #05111A; border: 2px dashed #C5A059; border-radius: 16px; margin-bottom: 30px;">
                                    <span style="font-size: 34px; font-weight: bold; letter-spacing: 6px; color: #C5A059;">$recoveryOtp</span>
                                </div>
                                <p style="font-size: 14px; line-height: 1.5; color: #B8C6D1;">
                                    Si no solicitaste este cambio, puedes ignorar este mensaje de forma segura.
                                </p>
                            </div>
                            <div style="background-color: #05111A; padding: 20px; text-align: center; border-top: 1px solid #0E2A3F;">
                                <p style="margin: 0; font-size: 12px; color: #B8C6D1;">
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
                    <body style="margin: 0; padding: 0; background-color: #050B10; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #F7FAFC;">
                        <div style="max-width: 600px; margin: 40px auto; background-color: #0E2A3F; border-radius: 16px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.4); border: 1px solid #26A69A;">
                            <div style="background: linear-gradient(135deg, #05111A, #0E2A3F); padding: 35px; text-align: center; border-bottom: 2px solid #C5A059;">
                                <h1 style="color: #C5A059; margin: 0; font-size: 28px; letter-spacing: 2px;">ECO-GUÍA</h1>
                            </div>
                            <div style="padding: 40px; text-align: center;">
                                <h2 style="color: #26A69A; margin-top: 0; font-size: 22px;">Verificación de Cuenta</h2>
                                <h3 style="color: #F7FAFC; font-weight: normal; margin-top: 5px;">Hola, $username</h3>
                                <p style="font-size: 16px; line-height: 1.6; color: #B8C6D1; margin-bottom: 30px;">
                                    Para continuar con tu registro en Eco-Guía, por favor ingresa el siguiente código de 6 dígitos en la aplicación:
                                </p>
                                <div style="display: inline-block; padding: 18px 42px; background-color: #05111A; border: 2px dashed #C5A059; border-radius: 16px; margin-bottom: 30px;">
                                    <span style="font-size: 34px; font-weight: bold; letter-spacing: 6px; color: #C5A059;">$otp</span>
                                </div>
                                <p style="font-size: 14px; line-height: 1.5; color: #B8C6D1;">
                                    Si no solicitaste crear una cuenta, puedes ignorar este correo sin problema.
                                </p>
                            </div>
                            <div style="background-color: #05111A; padding: 20px; text-align: center; border-top: 1px solid #0E2A3F;">
                                <p style="margin: 0; font-size: 12px; color: #B8C6D1;">
                                    &copy; 2026 Eco-Guía. El patrimonio en tus manos.
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
