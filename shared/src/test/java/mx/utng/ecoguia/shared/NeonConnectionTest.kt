package mx.utng.ecoguia.shared

import kotlinx.coroutines.runBlocking
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import org.junit.Test

class NeonConnectionTest {

    @Test
    fun testDatabaseConnection() = runBlocking {
        val repository = EcoGuiaRepositoryImpl()
        println("Iniciando prueba de conexión a Neon...")
        
        val result = repository.testConnection()
        
        println("Resultado: $result")
        
        assert(result.contains("Connected") || result.contains("Database time")) {
            "La conexión falló: $result"
        }
    }
}
