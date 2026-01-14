package com.example.proyecto_huerto.models

data class Bancal(
    val id: String = "",
    val nombre: String,
    val cultivos: List<String> = emptyList(),
    val fechaSiembra: String? = null,
    val historico: List<String> = emptyList(),
    val planificacion: String? = null
)
