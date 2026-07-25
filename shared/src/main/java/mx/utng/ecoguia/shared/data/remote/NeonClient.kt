package mx.utng.ecoguia.shared.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

import io.ktor.client.statement.*

class NeonClient(
    private val host: String = "ep-damp-frost-aua5pkkj-pooler.c-10.us-east-1.aws.neon.tech",
    @PublishedApi internal val connectionString: String = "postgresql://neondb_owner:npg_qfB5HVji7SWv@ep-damp-frost-aua5pkkj-pooler.c-10.us-east-1.aws.neon.tech/neondb"
) {
    @PublishedApi internal val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    @PublishedApi internal val sqlUrl = "https://$host/sql"

    /**
     * Executes a raw SQL query and returns the rows as a list of [T].
     */
    suspend inline fun <reified T> executeQuery(
        query: String,
        params: List<String>? = null
    ): List<T> {
        val httpResponse = client.post(sqlUrl) {
            header("Neon-Connection-String", connectionString)
            contentType(ContentType.Application.Json)
            setBody(NeonSqlRequest(query = query, params = params))
        }

        if (!httpResponse.status.isSuccess()) {
            val errorBody = httpResponse.bodyAsText()
            android.util.Log.e("NeonClient", "Error en Neon API: $errorBody")
            throw Exception("Neon API Error (${httpResponse.status}): $errorBody")
        }

        val responseBody = httpResponse.bodyAsText()
        android.util.Log.d("NeonClient", "Respuesta recibida: $responseBody")
        
        val response: NeonSqlResponse = httpResponse.body()
        val json = Json { ignoreUnknownKeys = true }
        return response.rows.map { json.decodeFromJsonElement<T>(it) }
    }

    /**
     * Executes a raw SQL command (like INSERT, UPDATE, DELETE) and returns the number of affected rows.
     */
    suspend fun executeCommand(
        query: String,
        params: List<String>? = null
    ): Int {
        val httpResponse = client.post(sqlUrl) {
            header("Neon-Connection-String", connectionString)
            contentType(ContentType.Application.Json)
            setBody(NeonSqlRequest(query = query, params = params))
        }
        
        if (!httpResponse.status.isSuccess()) {
            val errorBody = httpResponse.bodyAsText()
            throw Exception("Neon API Error (${httpResponse.status}): $errorBody")
        }

        val response: NeonSqlResponse = httpResponse.body()
        return response.rowCount ?: 0
    }

    fun close() {
        client.close()
    }
}
