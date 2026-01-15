package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable // <--- ¡IMPRESCINDIBLE!
data class Bancal(
    val id: String = "", // Valor por defecto necesario
    val nombre: String = "",
    val ancho: Double = 0.0,
    val largo: Double = 0.0,
    val cultivos: List<String> = emptyList(),
    val notas: String = "",
    val historico: List<String> = emptyList()
)