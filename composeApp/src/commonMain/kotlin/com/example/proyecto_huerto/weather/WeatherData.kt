package com.example.proyecto_huerto.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val current: CurrentWeatherInfo
)

@Serializable
data class CurrentWeatherInfo(
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("relative_humidity_2m")
    val humidity: Int,
    @SerialName("weather_code")
    val weatherCode: Int,
    @SerialName("wind_speed_10m")
    val windSpeed: Double
)

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(
        val temperature: Double,
        val weatherCode: Int,
        val humidity: Int,
        val windSpeed: Double
    ) : WeatherState()
    data class Error(val message: String) : WeatherState()
}
