package com.example.proyecto_huerto.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.location.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState

    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            val location = locationService.getCurrentLocation()
            if (location != null) {
                _weatherState.value = repository.getWeather(location.latitude, location.longitude)
            } else {
                _weatherState.value = WeatherState.Error("No se pudo obtener la ubicación")
            }
        }
    }
}
