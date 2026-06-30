package mx.utng.ecoguia.shared.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mx.utng.ecoguia.shared.domain.model.GeoDropEntity
import mx.utng.ecoguia.shared.domain.model.RouteEntity
import mx.utng.ecoguia.shared.domain.model.ConfigEntity
import mx.utng.ecoguia.shared.domain.model.AlertEntity

@Dao
interface EcoGuiaDao {
    @Query("SELECT * FROM routes")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM geo_drops WHERE routeId = :routeId ORDER BY `order` ASC")
    fun getGeoDropsForRoute(routeId: String): Flow<List<GeoDropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeoDrops(geoDrops: List<GeoDropEntity>)

    @Update
    suspend fun updateGeoDrop(geoDrop: GeoDropEntity)

    @Query("UPDATE geo_drops SET isVisited = 1 WHERE id = :id")
    suspend fun markAsVisited(id: String)

    @Query("SELECT * FROM config WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): ConfigEntity?

    @Query("SELECT * FROM config WHERE `key` = :key")
    fun getConfigFlow(key: String): Flow<ConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfigEntity)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)
}

@Database(entities = [RouteEntity::class, GeoDropEntity::class, ConfigEntity::class, AlertEntity::class], version = 2)
abstract class EcoGuiaDatabase : RoomDatabase() {
    abstract fun dao(): EcoGuiaDao

    companion object {
        @Volatile
        private var INSTANCE: EcoGuiaDatabase? = null

        fun getDatabase(context: android.content.Context): EcoGuiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    EcoGuiaDatabase::class.java,
                    "eco_guia_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
