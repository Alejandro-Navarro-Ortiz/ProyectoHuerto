package com.example.proyecto_huerto.util

/**
 * Obtiene la marca de tiempo actual en milisegundos desde la época Unix.
 * Implementación nativa para Android usando System.
 */
actual fun getCurrentEpochMillis(): Long {
    return System.currentTimeMillis()
}