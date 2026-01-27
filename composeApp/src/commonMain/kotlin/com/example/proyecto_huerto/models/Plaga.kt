package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa una plaga o enfermedad que puede afectar a los cultivos del huerto.
 */
@Serializable
data class Plaga(
    val id: String = "",
    val name: Map<String, String> = emptyMap(),
    val scientificName: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val symptoms: Map<String, String> = emptyMap(),
    val organicTreatment: Map<String, String> = emptyMap()
)

/**
 * Función de utilidad para limpiar strings que vienen de Firestore con formatos residuales
 * como "{es=Valor}" o "[Texto]".
 */
fun String.sanitizeFirestoreMap(): String {
    var result = this.trim()

    // Eliminar formato de Mapa convertido a String: {es=...}
    if (result.startsWith("{") && result.contains("=") && result.endsWith("}")) {
        result = result.substringAfter("=").substringBeforeLast("}")
    }

    // Eliminar corchetes de placeholders: [Texto]
    if (result.startsWith("[") && result.endsWith("]")) {
        result = result.substring(1, result.length - 1)
    }

    return result.trim()
}

/**
 * Obtiene el valor del mapa para el idioma dado o el español por defecto,
 * aplicando la limpieza de formato necesaria.
 */
fun Map<String, String>.getLocalizedSanitized(lang: String): String {
    val rawValue = this[lang] ?: this["es"] ?: ""
    return rawValue.sanitizeFirestoreMap()
}