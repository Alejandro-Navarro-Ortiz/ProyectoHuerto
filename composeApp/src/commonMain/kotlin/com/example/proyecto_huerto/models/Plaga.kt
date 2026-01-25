package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa una plaga o enfermedad que puede afectar a los cultivos del huerto.
 * Este modelo se utiliza para la base de conocimientos y la identificación de problemas.
 *
 * MODIFICACIÓN PARA INTERNACIONALIZACIÓN:
 * - 'id' se mantiene como un identificador único y estable.
 * - El resto de los campos de texto se convierten en mapas (Map<String, String>)
 *   para soportar múltiples idiomas. La clave del mapa es el código de idioma ("es", "en").
 */
@Serializable
data class Plaga(
    val id: String = "", // ID único, no se traduce
    // Mapa para los nombres comunes en diferentes idiomas
    val name: Map<String, String> = emptyMap(),
    // Mapa para los nombres científicos
    val scientificName: Map<String, String> = emptyMap(),
    // Mapa para las descripciones
    val description: Map<String, String> = emptyMap(),
    // Mapa para los síntomas
    val symptoms: Map<String, String> = emptyMap(),
    // Mapa para los tratamientos orgánicos
    val organicTreatment: Map<String, String> = emptyMap()
)