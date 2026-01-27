package com.example.proyecto_huerto.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Cultivo
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.Actividad
import com.example.proyecto_huerto.models.TipoActividad
import com.example.proyecto_huerto.notifications.NotificationScheduler
import com.example.proyecto_huerto.util.getCurrentEpochMillis
import com.example.proyecto_huerto.util.getCurrentInstant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.messaging.messaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la gestión de los bancales y cultivos.
 * Maneja la lógica de negocio, la sincronización con Firestore y las notificaciones de riego.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BancalViewModel(
    private val notificationScheduler: NotificationScheduler? = null
) : ViewModel() {
    private val _bancales = MutableStateFlow<List<Bancal>>(emptyList())
    val bancales = _bancales.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenToBancales()
        saveNotificationToken()
    }

    /**
     * Obtiene y guarda el token de FCM del usuario actual para habilitar notificaciones push.
     */
    private fun saveNotificationToken() {
        viewModelScope.launch {
            auth.authStateChanged.collectLatest { user ->
                if (user != null) {
                    try {
                        val token = Firebase.messaging.getToken()
                        db.collection("usuarios").document(user.uid)
                            .set(mapOf("fcmToken" to token), merge = true)
                        println("DEBUG: Token FCM sincronizado con éxito")
                    } catch (e: Exception) {
                        println("ERROR: Error al registrar token en Firestore: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Escucha en tiempo real los cambios en la colección de bancales del usuario en Firestore.
     */
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
                            doc.data<Bancal>().copy(id = doc.id)
                        } catch (e: Exception) {
                            println("ERROR AL LEER BANCAL (${doc.id}): ${e.message}")
                            null
                        }
                    }
                } else {
                    _bancales.value = emptyList()
                }
            }
        }
    }

    /**
     * Registra una nueva actividad (siembra, riego, etc.) en el diario del usuario.
     */
    private fun registrarActividad(tipo: TipoActividad, nombreBancal: String, detalle: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val actividad = Actividad(
                    tipo = tipo,
                    fecha = getCurrentEpochMillis(),
                    nombreBancal = nombreBancal,
                    detalle = detalle,
                    usuarioId = user.uid
                )
                db.collection("usuarios").document(user.uid).collection("actividades").add(actividad)
            } catch (e: Exception) {
                println("Error al registrar actividad: ${e.message}")
            }
        }
    }

    /**
     * Crea un nuevo bancal en la base de datos.
     */
    fun addBancal(nombre: String, ancho: Int, largo: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val nuevo = Bancal(nombre = nombre, ancho = ancho, largo = largo)
                db.collection("usuarios").document(user.uid).collection("bancales").add(nuevo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Actualiza la información de los cultivos en posiciones específicas de un bancal.
     * Al sembrar, se establece una frecuencia de riego por defecto de 2 días.
     */
    fun updateCultivos(bancal: Bancal, posiciones: List<String>, hortaliza: Hortaliza) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val nuevosCultivos = bancal.cultivos.toMutableMap()
                val nuevoCultivo = Cultivo(
                    hortalizaId = hortaliza.nombre, // ASIGNACIÓN CORRECTA: Vital para mostrar el icono
                    nombreHortaliza = hortaliza.nombreMostrado,
                    descripcion = hortaliza.descripcion,
                    frecuenciaRiegoDias = 2,
                    ultimoRiego = getCurrentInstant()
                )
                posiciones.forEach { nuevosCultivos[it] = nuevoCultivo }
                val bancalActualizado = bancal.copy(cultivos = nuevosCultivos)
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                val nombreHortaliza = hortaliza.nombreMostrado["es"] ?: hortaliza.nombre
                registrarActividad(
                    tipo = TipoActividad.SIEMBRA,
                    nombreBancal = bancal.nombre,
                    detalle = "Sembrado: $nombreHortaliza (${posiciones.size} celdas)"
                )
            } catch (e: Exception) {
                println("ERROR AL ACTUALIZAR EL CULTIVO: ${e.message}")
            }
        }
    }

    /**
     * Actualiza la fecha de último riego para los cultivos seleccionados y
     * programa una notificación de recordatorio para el próximo riego.
     */
    fun regarCultivos(bancal: Bancal, posiciones: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val cultivosActualizados = bancal.cultivos.toMutableMap()
                val ahora = getCurrentInstant()

                posiciones.forEach { pos ->
                    cultivosActualizados[pos]?.let { cultivo ->
                        cultivosActualizados[pos] = cultivo.copy(ultimoRiego = ahora)

                        val nombrePlanta = cultivo.nombreHortaliza["es"] ?: ""
                        notificationScheduler?.scheduleRiegoNotification(
                            plantName = nombrePlanta,
                            daysDelay = cultivo.frecuenciaRiegoDias
                        )
                    }
                }

                val bancalActualizado = bancal.copy(cultivos = cultivosActualizados)
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                registrarActividad(
                    tipo = TipoActividad.RIEGO,
                    nombreBancal = bancal.nombre,
                    detalle = "Regadas ${posiciones.size} secciones"
                )
            } catch (e: Exception) {
                println("ERROR AL REGAR LOS CULTIVOS: ${e.message}")
            }
        }
    }

    /**
     * Elimina un bancal por su ID.
     */
    fun deleteBancal(bancalId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancalId)
                    .delete()
            } catch (e: Exception) {
                println("ERROR AL ELIMINAR BANCAL: ${e.message}")
            }
        }
    }

    fun getBancalById(id: String): Bancal? = _bancales.value.find { it.id == id }
}