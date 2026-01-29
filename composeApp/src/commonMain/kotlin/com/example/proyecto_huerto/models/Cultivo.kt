@file:OptIn(ExperimentalTime::class)

package com.example.proyecto_huerto.models

import com.example.proyecto_huerto.util.getCurrentInstant
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Representa una instancia real de una planta sembrada en un bancal.
 * Gestiona el estado vital de la planta, específicamente su ciclo de riego y crecimiento.
 */
@Serializable
data class Cultivo(
    // ID único de la hortaliza para facilitar búsquedas y relaciones en Firebase
    val hortalizaId: String = "",

    // Mapas para soportar múltiples idiomas en la información guardada
    // Ej: "es" -> "Tomate", "en" -> "Tomato"
    val nombreHortaliza: Map<String, String> = emptyMap(),

    // Mapa para descripciones en diferentes idiomas
    val descripcion: Map<String, String> = emptyMap(),

    // Cada cuántos días se recomienda regar esta hortaliza específica
    val frecuenciaRiegoDias: Int = 0,

    // Fecha y hora exacta en la que se realizó la siembra
    @Contextual
    val fechaPlantado: Instant? = null,

    // Fecha y hora exacta del último riego registrado para este cultivo
    @Contextual
    val ultimoRiego: Instant? = null
) {
    /**
     * Lógica de negocio para determinar si la planta requiere agua actualmente.
     * Compara el tiempo transcurrido desde el último riego con la frecuencia establecida.
     */
    val necesitaRiego: Boolean
        get() {
            // Si nunca se ha regado, asumimos que necesita agua
            val ultimo = this.ultimoRiego ?: return true

            // Si la frecuencia es 0 o negativa, se considera que no tiene un ciclo automático
            if (frecuenciaRiegoDias <= 0) return false

            val ahora = getCurrentInstant()
            val proximoRiego = ultimo + frecuenciaRiegoDias.days

            return ahora > proximoRiego
        }

    /**
     * Propiedad auxiliar para la interfaz de usuario.
     * Indica visualmente si la planta está en estado de sequedad.
     */
    val estaSeco: Boolean
        get() = necesitaRiego
}