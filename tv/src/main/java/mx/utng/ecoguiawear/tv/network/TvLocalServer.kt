/**
 * Servidor HTTP local para recibir transmisiones directas y comandos del teléfono móvil en la Smart TV.
 *
 * Utiliza Ktor Server con motor Netty en el puerto local 8080 (por defecto) con el endpoint `/upload`
 * para permitir la recepción instantánea de capturas fotográficas y datos en tiempo real dentro
 * de la misma red de área local (LAN), funcionando como alternativa o complemento a la conectividad MQTT.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Servidor HTTP embebido en la Smart TV para comunicación local directa.
 *
 * Expone un flujo de estado reactivo con las imágenes recibidas y provee utilidades
 * para el descubrimiento de la dirección IP local de la TV.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
object TvLocalServer {
    private val _receivedImage = MutableStateFlow<Bitmap?>(null)

    /**
     * Flujo de estado observable con el último [Bitmap] recibido a través del servidor local.
     */
    val receivedImage: StateFlow<Bitmap?> = _receivedImage.asStateFlow()

    private var server: io.ktor.server.engine.ApplicationEngine? = null

    /**
     * Inicia el servidor HTTP embebido en el puerto especificado.
     *
     * Configura el plugin de CORS para permitir solicitudes entrantes y define la ruta `/upload`
     * para decodificar los bytes de imagen entrantes y emitirlos en [receivedImage].
     *
     * @param port Puerto TCP en el que escuchará el servidor (por defecto 8080).
     */
    fun startServer(port: Int = 8080) {
        if (server != null) return
        server = embeddedServer(Netty, port = port) {
            install(CORS) {
                anyHost()
            }
            routing {
                post("/upload") {
                    try {
                        val bytes = call.receive<ByteArray>()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            _receivedImage.value = bitmap
                            call.respond(HttpStatusCode.OK, "Image received successfully")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid image data")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                    }
                }
            }
        }.start(wait = false)
    }

    /**
     * Detiene el servidor HTTP y libera los sockets y recursos asociados.
     *
     * También reinicia el estado de [receivedImage] a `null`.
     */
    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
        _receivedImage.value = null // Limpiar estado al detener
    }

    /**
     * Obtiene la dirección IPv4 local asignada a la Smart TV en la red Wi-Fi o Ethernet.
     *
     * @param context Contexto de la aplicación utilizado para consultar el [WifiManager].
     * @return Cadena con la dirección IP local (ej. "192.168.1.50") o `null` si no fue posible determinarla.
     */
    fun getLocalIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
            // Fallback: buscar en las interfaces de red (Ethernet en emuladores de TV)
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
