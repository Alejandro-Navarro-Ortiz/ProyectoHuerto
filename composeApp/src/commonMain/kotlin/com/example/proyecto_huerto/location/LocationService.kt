package com.example.proyecto_huerto.location

data class LocationData(val latitude: Double, val longitude: Double)

interface LocationService {
    suspend fun getCurrentLocation(): LocationData?
}
