package mx.utng.ecoguia.shared.data.remote

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory

object HiveMQManager {

    private const val BROKER_HOST = "ssl://58652dc77c064ac1bef99055dab2e757.s1.eu.hivemq.cloud:8883"
    private const val USERNAME = "ecoguia"
    private const val PASSWORD = "12345678"

    private var mqttClient: MqttClient? = null

    @Synchronized
    private fun getOrCreateClient(): MqttClient {
        if (mqttClient == null || !mqttClient!!.isConnected) {
            val clientId = "EcoGuia_" + System.currentTimeMillis()
            val client = MqttClient(BROKER_HOST, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                userName = USERNAME
                password = PASSWORD.toCharArray()
                socketFactory = SSLSocketFactory.getDefault()
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 30
            }
            client.connect(options)
            mqttClient = client
        }
        return mqttClient!!
    }

    /**
     * Publica un comando de programa hacia un tópico de TV (ej: ecoguia/tv/595613/command)
     */
    fun publishProgramCommand(pairingCode: String, programType: String) {
        try {
            val client = getOrCreateClient()
            val topic = "ecoguia/tv/$pairingCode/command"
            val message = MqttMessage(programType.toByteArray(Charsets.UTF_8)).apply {
                qos = 1
            }
            client.publish(topic, message)
            android.util.Log.d("HiveMQManager", "Comando MQTT publicado con éxito en $topic: $programType")
        } catch (e: Exception) {
            android.util.Log.e("HiveMQManager", "Error al publicar comando MQTT: ${e.message}", e)
        }
    }

    /**
     * Suscribe a la Smart TV a su tópico único para recibir comandos de transmisión en tiempo real
     */
    fun subscribeToTvCommands(pairingCode: String, onCommandReceived: (String) -> Unit) {
        try {
            val client = getOrCreateClient()
            val topic = "ecoguia/tv/$pairingCode/command"
            client.subscribe(topic, 1) { _, message ->
                val command = String(message.payload, Charsets.UTF_8)
                android.util.Log.d("HiveMQManager", "Comando MQTT recibido en TV ($topic): $command")
                onCommandReceived(command)
            }
            android.util.Log.d("HiveMQManager", "Suscrito con éxito a tópico MQTT: $topic")
        } catch (e: Exception) {
            android.util.Log.e("HiveMQManager", "Error al suscribirse a tópico MQTT: ${e.message}", e)
        }
    }
}
