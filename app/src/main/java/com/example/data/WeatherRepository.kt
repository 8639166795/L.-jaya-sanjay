package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(private val db: WeatherDatabase) {
    private val dao = db.savedCityDao()

    val savedCitiesFlow: Flow<List<SavedCity>> = dao.getAllCitiesFlow()

    suspend fun preloadDefaultCitiesIfEmpty() {
        if (dao.getCityCount() == 0) {
            val defaults = listOf(
                SavedCity(name = "London", latitude = 51.5074, longitude = -0.1278, country = "United Kingdom", admin1 = "England"),
                SavedCity(name = "New York", latitude = 40.7128, longitude = -74.0060, country = "United States", admin1 = "New York"),
                SavedCity(name = "Tokyo", latitude = 35.6762, longitude = 139.6503, country = "Japan", admin1 = "Tokyo"),
                SavedCity(name = "Paris", latitude = 48.8566, longitude = 2.3522, country = "France", admin1 = "Île-de-France"),
                SavedCity(name = "Sydney", latitude = -33.8688, longitude = 151.2093, country = "Australia", admin1 = "New South Wales")
            )
            for (city in defaults) {
                dao.insertCity(city)
            }
        }
    }

    suspend fun searchCity(query: String): List<GeocodingResult> {
        return try {
            val response = WeatherApiClient.geocodingService.searchCity(query)
            response.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getForecast(lat: Double, lon: Double): ForecastResponse {
        return WeatherApiClient.weatherService.getForecast(lat, lon)
    }

    suspend fun saveCity(name: String, latitude: Double, longitude: Double, country: String?, admin1: String?) {
        val city = SavedCity(
            name = name,
            latitude = latitude,
            longitude = longitude,
            country = country,
            admin1 = admin1
        )
        dao.insertCity(city)
    }

    suspend fun deleteCity(id: Int) {
        dao.deleteCityById(id)
    }

    suspend fun toggleCitySaved(name: String, lat: Double, lon: Double, country: String?, admin1: String?): Boolean {
        val all = dao.getAllCities()
        // Check if matching coordinates
        val existing = all.find {
            it.name.equals(name, ignoreCase = true) &&
            Math.abs(it.latitude - lat) < 0.05 &&
            Math.abs(it.longitude - lon) < 0.05
        }
        return if (existing != null) {
            dao.deleteCityById(existing.id)
            false
        } else {
            val city = SavedCity(
                name = name,
                latitude = lat,
                longitude = lon,
                country = country,
                admin1 = admin1
            )
            dao.insertCity(city)
            true
        }
    }

    suspend fun isCitySaved(name: String, lat: Double, lon: Double): Boolean {
        val all = dao.getAllCities()
        return all.any {
            it.name.equals(name, ignoreCase = true) &&
            Math.abs(it.latitude - lat) < 0.05 &&
            Math.abs(it.longitude - lon) < 0.05
        }
    }
}
