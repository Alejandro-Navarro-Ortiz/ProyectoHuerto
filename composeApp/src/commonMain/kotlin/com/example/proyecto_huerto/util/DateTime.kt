package com.example.proyecto_huerto.util

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

/**
 * Espera una implementación específica de la plataforma para obtener el Instant actual.
 */
@OptIn(ExperimentalTime::class)
expect fun getCurrentInstant(): Instant
