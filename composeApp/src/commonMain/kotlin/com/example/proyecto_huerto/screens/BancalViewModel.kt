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
                println("ERROR GENERAL FIRESTORE: ${e.message}")
            }.collect { snapshot ->
                if (snapshot != null) {
                    _bancales.value = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Intentamos convertir el documento a objeto Bancal
                            doc.data<Bancal>().copy(id = doc.id)
                        } catch (e: Exception) {
                            // AQUI ESTÁ EL PROBLEMA: Antes esto devolvía null en silencio.
                            // Ahora vamos a ver por qué falla.
                            println("ERROR AL LEER BANCAL (${doc.id}): ${e.message}")
                            e.printStackTrace()
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
        val user = auth.currentUser

        // 1. Diagnóstico de Usuario
        if (user == null) {
            println("ERROR: El usuario es NULL. No se puede guardar en Firestore.")
            return
        }

        viewModelScope.launch {
            try {
                println("INTENTANDO GUARDAR: $nombre para usuario ${user.uid}")

                // Asegúrate de que tu modelo Bancal tiene valores por defecto para todo
                val nuevo = Bancal(
                    nombre = nombre,
                    id = "", // Firestore generará el ID, pero el objeto necesita este campo
                    ancho = 0.0,
                    largo = 0.0,
                    cultivos = emptyList(),
                    notas = "",
                    historico = emptyList()
                )

                // 2. Intenta guardar
                db.collection("usuarios").document(user.uid).collection("bancales").add(nuevo)
                println("GUARDADO EXITOSO")

            } catch (e: Exception) {
                // 3. Diagnóstico de Serialización o Red
                println("EXCEPCIÓN CRÍTICA AL GUARDAR: ${e.message}")
                e.printStackTrace()
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
                println("ERROR AL ACTUALIZAR: ${e.message}")
            }
        }
    }

    fun getBancalById(id: String): Bancal? = _bancales.value.find { it.id == id }
}