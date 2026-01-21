package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Actividad(
    val id: String = "",
    val tipo: TipoActividad,
    val fecha: Long, // Timestamp en milisegundos
    val nombreBancal: String,
    val detalle: String, // Ejemplo: "Tomates" o "3 celdas"
    val usuarioId: String = ""
)

@Serializable
enum class TipoActividad {
    RIEGO,
    SIEMBRA,
    COSECHA,
    TRATAMIENTO
}