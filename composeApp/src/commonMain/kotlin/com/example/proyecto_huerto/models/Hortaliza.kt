package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa la información maestra de un tipo de planta (ej. Tomate, Lechuga).
 * Define las características botánicas y las relaciones de compañía entre plantas.
 */
@Serializable
data class Hortaliza(
    val nombre: String = "", // Identificador único y nombre visual
    val icono: String = "", // Emoji o recurso gráfico representativo
    val descripcion: String = "", // Información sobre la planta
    val consejos: String = "", // Tips de cultivo específicos
    val compatibles: List<String> = emptyList(), // Lista de nombres de hortalizas que benefician su crecimiento
    val incompatibles: List<String> = emptyList() // Lista de nombres de hortalizas que perjudican su crecimiento
)