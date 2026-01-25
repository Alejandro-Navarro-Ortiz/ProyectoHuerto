package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa la información maestra de un tipo de planta (ej. Tomate, Lechuga).
 * Define las características botánicas y las relaciones de compañía entre plantas.
 *
 * MODIFICACIÓN PARA INTERNACIONALIZACIÓN:
 * - 'nombre' se mantiene como un ID único y estable (ej: "tomate_rojo").
 * - Se añaden campos de tipo Map<String, String> para los textos que deben ser traducidos.
 *   La clave del mapa es el código de idioma ("es", "en") y el valor es el texto traducido.
 * - Esto afecta a 'nombreMostrado', 'descripcion', y 'consejos'.
 */
@Serializable
data class Hortaliza(
    // ID único. No se traduce. Usado para relaciones (compatibles/incompatibles).
    val nombre: String = "",
    // Mapa para los nombres a mostrar en la UI.
    val nombreMostrado: Map<String, String> = emptyMap(),
    val icono: String = "", // Emoji o recurso gráfico representativo
    // Mapa para descripciones multilingües.
    val descripcion: Map<String, String> = emptyMap(),
    // Mapa para consejos de cultivo multilingües.
    val consejos: Map<String, String> = emptyMap(),
    // Lista de 'nombre' (ID) de hortalizas que benefician su crecimiento.
    val compatibles: List<String> = emptyList(),
    // Lista de 'nombre' (ID) de hortalizas que perjudican su crecimiento.
    val incompatibles: List<String> = emptyList()
)