package com.example.proyecto_huerto.util

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/**
 * Puente expect/actual usando Long para evitar conflictos de tipos Instant entre plataformas.
 */
expect fun getCurrentEpochMillis(): Long

/**
 * Función que usa toda la aplicación para obtener el tiempo actual.
 */
@OptIn(ExperimentalTime::class)
fun getCurrentInstant(): Instant {
    return Instant.fromEpochMilliseconds(getCurrentEpochMillis())
}