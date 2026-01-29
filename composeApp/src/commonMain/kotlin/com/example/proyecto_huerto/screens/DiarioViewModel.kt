package com.example.proyecto_huerto.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.models.Actividad
import com.example.proyecto_huerto.models.Tarea
import com.example.proyecto_huerto.models.TipoActividad
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

    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades = _actividades.asStateFlow()

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas = _tareas.asStateFlow()

    init {
        listenToActividades()
        listenToTareas()
    }

    private fun listenToActividades() {
        viewModelScope.launch {
            auth.authStateChanged.flatMapLatest { user ->
                if (user != null) {
                    db.collection("usuarios").document(user.uid).collection("actividades").snapshots
                } else {
                    flowOf(null)
                }
            }.collect { snapshot ->
                if (snapshot != null) {
                    // CORRECCIÓN: Asignamos el ID del documento de Firebase a la actividad
                    _actividades.value = snapshot.documents.map { doc ->
                        doc.data<Actividad>().copy(id = doc.id)
                    }
                } else {
                    _actividades.value = emptyList()
                }
            }
        }
    }

    // NUEVA FUNCIÓN: Borrado de actividades directamente desde este ViewModel
    fun deleteActividad(actividadId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(user.uid).collection("actividades")
                    .document(actividadId)
                    .delete()
            } catch (e: Exception) {
                println("Error al eliminar actividad: ${e.message}")
            }
        }
    }

    private fun listenToTareas() {
        viewModelScope.launch {
            auth.authStateChanged.flatMapLatest { user ->
                if (user != null) {
                    db.collection("usuarios").document(user.uid).collection("tareas").snapshots
                } else {
                    flowOf(null)
                }
            }.collect { snapshot ->
                if (snapshot != null) {
                    _tareas.value = snapshot.documents.map { doc ->
                        doc.data<Tarea>().copy(id = doc.id)
                    }
                } else {
                    _tareas.value = emptyList()
                }
            }
        }
    }

    fun addTarea(titulo: String, descripcion: String, fecha: Long, tipo: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val nuevaTarea = Tarea(
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = fecha,
                    tipo = tipo,
                    completada = false
                )
                db.collection("usuarios").document(user.uid).collection("tareas").add(nuevaTarea)
            } catch (e: Exception) {
                println("Error al añadir tarea: ${e.message}")
            }
        }
    }

    fun toggleTareaCompletada(tarea: Tarea) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(user.uid).collection("tareas")
                    .document(tarea.id)
                    .update("completada" to !tarea.completada)
            } catch (e: Exception) {
                println("Error al actualizar tarea: ${e.message}")
            }
        }
    }

    fun deleteTarea(tareaId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(user.uid).collection("tareas")
                    .document(tareaId)
                    .delete()
            } catch (e: Exception) {
                println("Error al eliminar tarea: ${e.message}")
            }
        }
    }
}