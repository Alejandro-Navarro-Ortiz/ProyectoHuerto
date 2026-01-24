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
    val ultimoRiego: Instant? = null
) {
    /**
     * Determina si el cultivo necesita ser regado.
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
     * Alias para necesitaRiego para compatibilidad con la UI.
     */
    val estaSeco: Boolean
        get() = necesitaRiego
}
