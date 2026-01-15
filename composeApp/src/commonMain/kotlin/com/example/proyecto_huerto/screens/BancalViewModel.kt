package com.example.proyecto_huerto.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.models.Bancal
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BancalViewModel : ViewModel() {
    private val _bancales = MutableStateFlow<List<Bancal>>(emptyList())
    val bancales = _bancales.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenToBancales()
    }

    private fun listenToBancales() {
        viewModelScope.launch {
            auth.authStateChanged.flatMapLatest { user ->
                if (user != null) {
                    db.collection("usuarios").document(user.uid).collection("bancales").snapshots
                } else {
                    flowOf(null)
                }
            }.catch { e ->
                println("ERROR FIRESTORE: ${e.message}")
            }.collect { snapshot ->
                if (snapshot != null) {
                    _bancales.value = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.data<Bancal>().copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else {
                    _bancales.value = emptyList()
                }
            }
        }
    }

    fun addBancal(nombre: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                // Usamos un mapa para asegurar que Firestore acepte los datos siempre
                val data = mapOf(
                    "nombre" to nombre,
                    "cultivos" to emptyList<String>(),
                    "ancho" to 0.0,
                    "largo" to 0.0,
                    "notas" to ""
                )
                db.collection("usuarios").document(user.uid).collection("bancales").add(data)
            } catch (e: Exception) {
                println("FALLO AL AÑADIR: ${e.message}")
            }
        }
    }

    fun updateBancal(updatedBancal: Bancal) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(updatedBancal.id)
                    .set(updatedBancal)
            } catch (e: Exception) {
                println("FALLO AL ACTUALIZAR: ${e.message}")
            }
        }
    }

    fun getBancalById(id: String): Bancal? = _bancales.value.find { it.id == id }
}