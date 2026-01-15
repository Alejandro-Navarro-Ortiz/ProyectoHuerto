package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Hortaliza(
    val nombre: String,
    val icono: String // Usaremos emojis o nombres de recursos
)

// Lista de hortalizas que el usuario podrá plantar
val hortalizasDisponibles = listOf(
    Hortaliza("Tomate", "🍅"),
    Hortaliza("Lechuga", "🥬"),
    Hortaliza("Zanahoria", "🥕"),
    Hortaliza("Pimiento", "🌶️"),
    Hortaliza("Cebolla", "🧅"),
    Hortaliza("Berenjena", "🍆"),
    Hortaliza("Patata", "🥔"),
    Hortaliza("Calabacín", "🥒"),
)
