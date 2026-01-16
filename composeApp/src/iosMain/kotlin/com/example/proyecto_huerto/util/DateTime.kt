package com.example.proyecto_huerto.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Implementación iOS para obtener el Instant actual. Es idéntica a la de Android,
 * pero debe estar en su propio sourceset para la compilación de KMP.
 */
actual fun getCurrentInstant(): Instant {
    return Clock.System.now()
}
