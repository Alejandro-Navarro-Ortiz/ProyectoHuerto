package com.example.proyecto_huerto.models

import com.example.proyecto_huerto.util.getCurrentInstant
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual // Import this
import kotlinx.serialization.Serializable    import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@Serializable
data class Cultivo @OptIn(ExperimentalTime::class) constructor(
    val nombreHortaliza: String,
    val frecuenciaRiegoDias: Int,
    @Contextual // Add this annotation
    val ultimaVezRegado: Instant,
) {
    @OptIn(ExperimentalTime::class)
    val estaSeco: Boolean
        get() {
            val ahora = getCurrentInstant()
            val proximoRiego = ultimaVezRegado + frecuenciaRiegoDias.days
            return ahora > proximoRiego
        }
}
    