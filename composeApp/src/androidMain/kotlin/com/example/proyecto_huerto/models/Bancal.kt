package com.example.proyecto_huerto.models

data class Bancal(
    val id: String,
    val nombre: String,
    val cultivos: List<String>,
    val fechaSiembra: String,
    val historico: List<String> = emptyList()
)
