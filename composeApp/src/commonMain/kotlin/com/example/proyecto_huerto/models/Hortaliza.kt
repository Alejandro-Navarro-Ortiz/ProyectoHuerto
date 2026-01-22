package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Hortaliza(
    val nombre: String = "",
    val icono: String = "",
    val descripcion: String = "",
    val consejos: String = "",
    val compatibles: List<String> = emptyList(),
    val incompatibles: List<String> = emptyList()
)