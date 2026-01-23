@file:OptIn(ExperimentalTime::class)

package com.example.proyecto_huerto.models

import com.example.proyecto_huerto.util.getCurrentInstant
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@Serializable
data class Cultivo(
    val nombreHortaliza: String,
    val frecuenciaRiegoDias: Int,
    @Contextual
    val ultimoRiego: Instant? = null // Ahora puede ser nulo
) {
    /**
     * Determina si el cultivo necesita ser regado.
     */
    val necesitaRiego: Boolean
        get() {
            // Si nunca se ha regado, necesita riego.
            val ultimoRiego = this.ultimoRiego ?: return true

            // Si la frecuencia no es válida, no se puede determinar.
            if (frecuenciaRiegoDias <= 0) return false

            val ahora = getCurrentInstant()
            val proximoRiego = ultimoRiego + frecuenciaRiegoDias.days
            return ahora > proximoRiego
        }
}
