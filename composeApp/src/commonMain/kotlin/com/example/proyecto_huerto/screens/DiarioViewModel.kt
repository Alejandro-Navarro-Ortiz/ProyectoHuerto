package com.example.proyecto_huerto.screensimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.models.Tarea
import com.example.proyecto_huerto.models.Actividad
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DiarioViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas = _tareas.asStateFlow()

    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades = _actividades.asStateFlow()

    init {
        listenData()
    }

    private fun listenData() {
        viewModelScope.launch {
            auth.authStateChanged.collectLatest { user ->
                if (user != null) {
                    // Escuchar Tareas
                    launch {
                        db.collection("usuarios").document(user.uid).collection("tareas").snapshots.collect { snap ->
                            _tareas.value = snap.documents.mapNotNull { it.data<Tarea>().copy(id = it.id) }
                        }
                    }
                    // Escuchar Actividades automáticas
                    launch {
                        db.collection("usuarios").document(user.uid).collection("actividades").snapshots.collect { snap ->
                            _actividades.value = snap.documents.mapNotNull { it.data<Actividad>().copy(id = it.id) }
                        }
                    }
                }
            }
        }
    }

    fun addTarea(titulo: String, descripcion: String, fecha: Long, tipo: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val nueva = Tarea(titulo = titulo, descripcion = descripcion, fecha = fecha, tipo = tipo)
            db.collection("usuarios").document(user.uid).collection("tareas").add(nueva)
        }
    }

    fun toggleTareaCompletada(tarea: Tarea) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            db.collection("usuarios").document(user.uid).collection("tareas").document(tarea.id)
                .set(tarea.copy(completada = !tarea.completada))
        }
    }

    fun deleteTarea(id: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            db.collection("usuarios").document(user.uid).collection("tareas").document(id).delete()
        }
    }
}