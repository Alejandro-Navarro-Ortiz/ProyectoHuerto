package com.example.proyecto_huerto

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform