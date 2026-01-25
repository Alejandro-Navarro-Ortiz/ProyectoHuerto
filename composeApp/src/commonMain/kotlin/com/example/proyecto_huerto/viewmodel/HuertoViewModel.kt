package com.example.proyecto_huerto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.proyecto_huerto.data.Migration // Ya no es necesario
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.Plaga
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado para la UI, maneja la carga y los posibles errores
sealed class HuertoUiState<T> {
    data class Success<T>(val data: T) : HuertoUiState<T>()
    data class Error<T>(val message: String) : HuertoUiState<T>()
    class Loading<T> : HuertoUiState<T>()
}

class HuertoViewModel : ViewModel() {

    private val firestore = Firebase.firestore

    // Flujo para las Hortalizas
    private val _hortalizasState = MutableStateFlow<HuertoUiState<List<Hortaliza>>>(HuertoUiState.Loading())
    val hortalizasState = _hortalizasState.asStateFlow()

    // Flujo para las Plagas
    private val _plagasState = MutableStateFlow<HuertoUiState<List<Plaga>>>(HuertoUiState.Loading())
    val plagasState = _plagasState.asStateFlow()

    init {
        // --- MIGRACIÓN DESACTIVADA ---
        // Migration.migrateData()

        // Cargar todos los datos al inicializar el ViewModel
        fetchHortalizas()
        fetchPlagas()
    }

    private fun fetchHortalizas() {
        viewModelScope.launch {
            _hortalizasState.value = HuertoUiState.Loading()
            try {
                val snapshot = firestore.collection("hortalizas").get()
                val hortalizas = snapshot.documents.map { it.data<Hortaliza>() }
                _hortalizasState.value = HuertoUiState.Success(hortalizas)
            } catch (e: Exception) {
                _hortalizasState.value = HuertoUiState.Error("Error al cargar hortalizas: ${e.message}")
            }
        }
    }

    private fun fetchPlagas() {
        viewModelScope.launch {
            _plagasState.value = HuertoUiState.Loading()
            try {
                val snapshot = firestore.collection("plagas").get()
                val plagas = snapshot.documents.map { it.data<Plaga>() }
                _plagasState.value = HuertoUiState.Success(plagas)
            } catch (e: Exception) {
                _plagasState.value = HuertoUiState.Error("Error al cargar plagas: ${e.message}")
            }
        }
    }
}