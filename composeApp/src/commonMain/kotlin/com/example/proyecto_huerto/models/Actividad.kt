package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa una entrada en el diario del huerto.
 * Registra acciones realizadas por el usuario para su seguimiento histórico.
 */
@Serializable
data class Actividad(
    val id: String = "",
    val tipo: TipoActividad, // Categoría de la acción (Riego, Siembra, etc.)
    val fecha: Long, // Marca de tiempo (Epoch Millis) de cuándo ocurrió
    val nombreBancal: String, // Nombre del bancal donde se realizó la acción
    val detalle: String, // Texto descriptivo (ej: "Se usó jabón potásico" o "Variedad Cherry")
    val usuarioId: String = "", // Referencia al UID del usuario propietario del registro
    // NUEVO: Campo para registrar la cantidad de acciones en un solo evento (ej. regar 5 plantas).
    val cantidad: Int = 1
)

/**
 * Define las categorías permitidas de acciones en el huerto.
 */
@Serializable
enum class TipoActividad {
    RIEGO,
    SIEMBRA,
    COSECHA,
    TRATAMIENTO,
    ABONADO
}
