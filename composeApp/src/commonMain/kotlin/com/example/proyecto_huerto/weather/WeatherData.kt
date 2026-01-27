package com.example.proyecto_huerto.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("current_weather")
    val currentWeather: CurrentWeather
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    @SerialName("weathercode")
    val weatherCode: Int
)

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val temperature: Double, val weatherCode: Int) : WeatherState()
    data class Error(val message: String) : WeatherState()
}
