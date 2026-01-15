package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Long = 0L, // Timestamp en milisegundos
    val completada: Boolean = false,
    val tipo: String = "OTRA" // RIEGO, SIEMBRA, COSECHA, TRATAMIENTO, OTRA
)
