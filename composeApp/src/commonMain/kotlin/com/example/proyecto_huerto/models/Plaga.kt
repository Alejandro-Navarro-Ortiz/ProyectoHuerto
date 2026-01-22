package com.example.proyecto_huerto.models

import kotlinx.serialization.Serializable

@Serializable
data class Plaga(
    val id: String = "",
    val name: String = "",
    val scientificName: String = "",
    val description: String = "",
    val symptoms: String = "",
    val organicTreatment: String = ""
)
