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
 *
 * MODIFICACIÓN PARA INTERNACIONALIZACIÓN:
 * - 'nombreHortaliza' y 'descripcion' ahora son mapas para soportar múltiples idiomas.
 *   Esto permite que la información obtenida de Firebase se muestre en el idioma
 *   seleccionado por el usuario en la aplicación.
 */
@Serializable
data class Cultivo(
    // Mapa para nombres en diferentes idiomas (ej: "es" -> "Tomate", "en" -> "Tomato")
    val nombreHortaliza: Map<String, String> = emptyMap(),
    // Mapa para descripciones en diferentes idiomas
    val descripcion: Map<String, String> = emptyMap(),
    val frecuenciaRiegoDias: Int, // Cada cuántos días se recomienda regar
    @Contextual
    val ultimoRiego: Instant? = null // Fecha y hora exacta del último riego registrado
) {
    /**
     * Lógica de negocio para determinar si la planta requiere agua.
     * Compara el tiempo transcurrido desde el último riego con la frecuencia establecida.
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
     * Indica visualmente si la tierra está considerada como seca.
     */
    val estaSeco: Boolean
        get() = necesitaRiego
}