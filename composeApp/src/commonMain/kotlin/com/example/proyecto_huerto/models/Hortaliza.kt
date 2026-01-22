package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Hortaliza(
    val nombre: String,
    val icono: String,
    val descripcion: String = "",
    val consejos: String = "",
    val compatibles: List<String> = emptyList(), // Plantas amigas
    val incompatibles: List<String> = emptyList() // Plantas enemigas
)

val hortalizasDisponibles = listOf(
    Hortaliza(
        nombre = "Tomate",
        icono = "🍅",
        descripcion = "El rey del huerto. Requiere mucho sol y riegos regulares sin mojar las hojas.",
        consejos = "Poda los chupones para mejorar la producción y entutora la planta.",
        compatibles = listOf("Albahaca", "Zanahoria", "Cebolla"),
        incompatibles = listOf("Patata", "Hinojo")
    ),
    Hortaliza(
        nombre = "Lechuga",
        icono = "🥬",
        descripcion = "Cultivo rápido y sencillo. Ideal para principiantes.",
        consejos = "Evita el sol directo en verano para que no espigue (florezca amarga).",
        compatibles = listOf("Zanahoria", "Rábano", "Pepino"),
        incompatibles = listOf("Cebolla", "Perejil")
    ),
    Hortaliza(
        nombre = "Zanahoria",
        icono = "🥕",
        descripcion = "Hortaliza de raíz que prefiere suelos sueltos y sin piedras.",
        consejos = "Mantén la humedad constante durante la germinación.",
        compatibles = listOf("Lechuga", "Tomate", "Cebolla"),
        incompatibles = listOf("Pimiento", "Apio")
    ),
    Hortaliza(
        nombre = "Pimiento",
        icono = "🌶️",
        descripcion = "Planta exigente en calor y nutrientes.",
        consejos = "No trasplantes al exterior hasta que las noches sean cálidas.",
        compatibles = listOf("Albahaca", "Cebolla"),
        incompatibles = listOf("Zanahoria", "Hinojo")
    ),
    Hortaliza(
        nombre = "Cebolla",
        icono = "🧅",
        descripcion = "Bulbo esencial en la cocina. Muy resistente.",
        consejos = "Deja de regar unas semanas antes de la cosecha para que sequen bien.",
        compatibles = listOf("Zanahoria", "Tomate", "Pepino"),
        incompatibles = listOf("Lechuga", "Leguminosas")
    ),
    Hortaliza(
        nombre = "Berenjena",
        icono = "🍆",
        descripcion = "Planta de ciclo largo que necesita mucho calor.",
        consejos = "Corta el fruto con tijeras para no dañar la planta.",
        compatibles = listOf("Judía", "Pimiento"),
        incompatibles = listOf("Patata")
    ),
    Hortaliza(
        nombre = "Patata",
        icono = "🥔",
        descripcion = "Tubérculo productivo que requiere aporcado (cubrir tallos con tierra).",
        consejos = "No la cultives donde hubo tomates el año anterior para evitar plagas comunes.",
        compatibles = listOf("Haba", "Maíz"),
        incompatibles = listOf("Tomate", "Calabacín", "Berenjena")
    ),
    Hortaliza(
        nombre = "Calabacín",
        icono = "🥒",
        descripcion = "Planta muy productiva que ocupa bastante espacio.",
        consejos = "Cosecha los frutos jóvenes para que la planta siga produciendo.",
        compatibles = listOf("Maíz", "Judía"),
        incompatibles = listOf("Patata")
    )
)