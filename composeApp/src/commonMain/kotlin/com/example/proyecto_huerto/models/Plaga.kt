package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa una plaga o enfermedad que puede afectar a los cultivos del huerto.
 * Este modelo se utiliza para la base de conocimientos y la identificación de problemas.
 */
@Serializable
data class Plaga(
    val id: String = "",
    val name: String = "", // Nombre común de la plaga
    val scientificName: String = "", // Nombre científico para precisión técnica
    val description: String = "", // Descripción general de la plaga
    val symptoms: String = "", // Signos visibles en las plantas afectadas
    val organicTreatment: String = "" // Sugerencias de remedios ecológicos o naturales
)