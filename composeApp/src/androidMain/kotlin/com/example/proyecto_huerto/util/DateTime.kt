package com.example.proyecto_huerto.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementación Android para obtener el Instant actual usando kotlinx.datetime.Clock.
 */
actual fun getCurrentInstant(): Instant {
    return Clock.System.now()
}
