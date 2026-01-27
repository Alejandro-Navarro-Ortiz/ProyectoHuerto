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
 * Gestiona el estado vital de la planta, específicamente su ciclo de riego.
 */
@Serializable
data class Cultivo(
    // ID único de la hortaliza para facilitar búsquedas y relaciones
    val hortalizaId: String = "",
    // Mapas para soportar múltiples idiomas en la información guardada
    val nombreHortaliza: Map<String, String> = emptyMap(),
    val descripcion: Map<String, String> = emptyMap(),
    val frecuenciaRiegoDias: Int, // Cada cuántos días se recomienda regar
    @Contextual
    val ultimoRiego: Instant? = null // Fecha y hora exacta del último riego registrado
) {
    /**
     * Lógica de negocio para determinar si la planta requiere agua.
     */
    val necesitaRiego: Boolean
        get() {
            val ultimoRiego = this.ultimoRiego ?: return true
            if (frecuenciaRiegoDias <= 0) return false
            val ahora = getCurrentInstant()
            val proximoRiego = ultimoRiego + frecuenciaRiegoDias.days
            return ahora > proximoRiego
        }

    /**
     * Propiedad auxiliar para la interfaz de usuario.
     */
    val estaSeco: Boolean
        get() = necesitaRiego
}
