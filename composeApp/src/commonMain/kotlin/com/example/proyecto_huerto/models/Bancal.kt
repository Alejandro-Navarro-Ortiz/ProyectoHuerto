package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Bancal(
    val id: String = "",
    val nombre: String = "",
    val ancho: Int = 0, // Representa el número de columnas
    val largo: Int = 0, // Representa el número de filas
    // Un mapa para guardar el cultivo en cada celda. La clave es la posición "x-y"
    val cultivos: Map<String, String> = emptyMap(),
    val notas: String = "",
    val historico: List<String> = emptyList()
)
