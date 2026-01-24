package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

/**
 * Representa una estructura física de cultivo (cajón, parcela o surco).
 * Organiza los cultivos en una cuadrícula de coordenadas.
 */
@Serializable
data class Bancal(
    val id: String = "", // ID único generado por Firestore
    val nombre: String = "", // Nombre personalizado dado por el usuario
    val ancho: Int = 0, // Número de columnas en la cuadrícula
    val largo: Int = 0, // Número de filas en la cuadrícula
    /**
     * Mapa que representa el contenido del bancal.
     * La clave es un String con formato "fila-columna" (ej. "0-2")
     * El valor es el objeto Cultivo presente en esa posición.
     */
    val cultivos: Map<String, Cultivo> = emptyMap(),
    val notas: String = "", // Observaciones generales del usuario sobre el bancal
    val historico: List<String> = emptyList() // Registro de cultivos anteriores (rotación de cultivos)
)