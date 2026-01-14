package com.example.proyecto_huerto.screens

import androidx.lifecycle.ViewModel
import com.example.proyecto_huerto.models.Bancal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BancalViewModel : ViewModel() {
    private val _bancales = MutableStateFlow<List<Bancal>>(listOf(
        Bancal(id = "1", nombre = "Bancal 1", cultivos = emptyList())
    ))
    val bancales = _bancales.asStateFlow()

    fun addBancal(nombre: String) {
        val nuevoBancal = Bancal(
            id = (bancales.value.size + 1).toString(),
            nombre = nombre,
            cultivos = emptyList()
        )
        _bancales.update { it + nuevoBancal }
    }
}
