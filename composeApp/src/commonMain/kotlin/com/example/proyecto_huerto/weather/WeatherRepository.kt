package com.example.proyecto_huerto.weather

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class WeatherRepository {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherState {
        return try {
            val response: WeatherResponse = httpClient.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", lat)
                parameter("longitude", lon)
                parameter("current_weather", true)
            }.body()
            
            WeatherState.Success(
                temperature = response.currentWeather.temperature,
                weatherCode = response.currentWeather.weatherCode
            )
        } catch (e: Exception) {
            WeatherState.Error("No se pudo obtener el clima: ${e.message}")
        }
    }
}
