package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_cities")
data class SavedCity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SavedCityDao {
    @Query("SELECT * FROM saved_cities ORDER BY timestamp DESC")
    fun getAllCitiesFlow(): Flow<List<SavedCity>>

    @Query("SELECT * FROM saved_cities ORDER BY timestamp DESC")
    suspend fun getAllCities(): List<SavedCity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCity): Long

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteCityById(id: Int)

    @Query("DELETE FROM saved_cities WHERE LOWER(name) = LOWER(:name) AND ABS(latitude - :lat) < 0.05 AND ABS(longitude - :lon) < 0.05")
    suspend fun deleteCityByCoordinates(name: String, lat: Double, lon: Double)

    @Query("SELECT COUNT(*) FROM saved_cities")
    suspend fun getCityCount(): Int
}

@Database(entities = [SavedCity::class], version = 1, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun savedCityDao(): SavedCityDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                @Suppress("WRITE_ONLY_MUTABLE_VARIABLE")
                INSTANCE = instance
                instance
            }
        }
    }
}
