package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface WeatherUiResult {
    object Loading : WeatherUiResult
    data class Success(
        val cityName: String,
        val country: String?,
        val admin1: String?,
        val latitude: Double,
        val longitude: Double,
        val forecast: ForecastResponse,
        val isSaved: Boolean,
        val aiInsights: String? = null,
        val isAiLoading: Boolean = false
    ) : WeatherUiResult
    data class Error(val message: String) : WeatherUiResult
}

class WeatherViewModel(
    application: Application,
    private val repository: WeatherRepository
) : AndroidViewModel(application) {

    // Saved cities reactive stream
    val savedCities: StateFlow<List<SavedCity>> = repository.savedCitiesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<WeatherUiResult>(WeatherUiResult.Loading)
    val uiState: StateFlow<WeatherUiResult> = _uiState.asStateFlow()

    // Search Results State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preloadDefaultCitiesIfEmpty()
            // Load the first saved city or London on startup
            savedCities.first { it.isNotEmpty() }.firstOrNull()?.let { firstCity ->
                selectLocation(firstCity.name, firstCity.latitude, firstCity.longitude, firstCity.country, firstCity.admin1)
            } ?: run {
                selectLocation("London", 51.5074, -0.1278, "United Kingdom", "England")
            }
        }
    }

    // Load forecast details and triggers AI recommendations
    fun selectLocation(name: String, latitude: Double, longitude: Double, country: String?, admin1: String?) {
        viewModelScope.launch {
            _uiState.value = WeatherUiResult.Loading
            try {
                val forecast = repository.getForecast(latitude, longitude)
                val isSaved = repository.isCitySaved(name, latitude, longitude)

                // Post initial success results
                _uiState.value = WeatherUiResult.Success(
                    cityName = name,
                    country = country,
                    admin1 = admin1,
                    latitude = latitude,
                    longitude = longitude,
                    forecast = forecast,
                    isSaved = isSaved,
                    aiInsights = null,
                    isAiLoading = true
                )

                // Trigger AI insights async so we do not block UI loading
                loadAiInsights(name, forecast, country, admin1, isSaved)
            } catch (e: Exception) {
                _uiState.value = WeatherUiResult.Error(e.localizedMessage ?: "Failed to load weather report.")
            }
        }
    }

    private fun loadAiInsights(name: String, forecast: ForecastResponse, country: String?, admin1: String?, isSaved: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is WeatherUiResult.Success && currentState.cityName == name) {
                val current = forecast.current
                val daily = forecast.daily
                val temp = current?.temperature2m ?: 20.0
                val apparentTemp = current?.apparentTemperature ?: temp
                val humidity = current?.relativeHumidity2m ?: 50.0
                val precipP = forecast.hourly?.precipitationProbability?.firstOrNull()?.toDouble() ?: 0.0
                val wind = current?.windSpeed10m ?: 10.0
                val uv = daily?.uvIndexMax?.firstOrNull() ?: 2.0
                val codeDesc = getWeatherDescription(current?.weatherCode ?: 0)

                val content = GeminiService.getAiInsights(
                    city = name,
                    temp = temp,
                    apparentTemp = apparentTemp,
                    humidity = humidity,
                    precipitationProbability = precipP,
                    wind = wind,
                    uvMax = uv,
                    weatherDesc = codeDesc
                )

                _uiState.value = currentState.copy(
                    aiInsights = content,
                    isAiLoading = false
                )
            }
        }
    }

    // Toggle Favorite Action
    fun toggleFavorite(name: String, latitude: Double, longitude: Double, country: String?, admin1: String?) {
        viewModelScope.launch {
            val isNowSaved = repository.toggleCitySaved(name, latitude, longitude, country, admin1)
            val currentState = _uiState.value
            if (currentState is WeatherUiResult.Success && currentState.latitude == latitude && currentState.longitude == longitude) {
                _uiState.value = currentState.copy(isSaved = isNowSaved)
            }
        }
    }

    // Live Instant Geocoding Search
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.trim().length >= 2) {
            _isSearching.value = true
            viewModelScope.launch {
                try {
                    val results = repository.searchCity(query)
                    _searchResults.value = results
                } catch (e: Exception) {
                    _searchResults.value = emptyList()
                } finally {
                    _isSearching.value = false
                }
            }
        } else {
            _searchResults.value = emptyList()
        }
    }

    // Clear search status
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    // Weather Code WMO Helper
    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy conditions"
            51, 53, 55 -> "Light Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Continuous Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snowfall"
            77 -> "Snowgrains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Intermittent Conditions"
        }
    }
}

// Factory to create VM with custom parameters
class WeatherViewModelFactory(
    private val application: Application,
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
