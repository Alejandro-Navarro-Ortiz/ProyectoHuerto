package com.example.proyecto_huerto.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.proyecto_huerto.MainActivity
import com.example.proyecto_huerto.data.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio encargado de gestionar las notificaciones remotas de Firebase (FCM).
 * Recibe mensajes del servidor y gestiona la actualización de los tokens de dispositivo.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    /**
     * Se ejecuta cuando se recibe un mensaje de Firebase mientras la app está en segundo plano o abierta.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Extraemos el título y el cuerpo del mensaje, priorizando la notificación sobre los datos planos
        val title = message.notification?.title ?: message.data["title"] ?: "Aviso del Huerto"
        val body = message.notification?.body ?: message.data["body"] ?: "Tienes una actualización."
        
        showNotification(title, body)
    }

    /**
     * Se llama cuando Firebase genera un nuevo token (por ejemplo, en la primera instalación).
     * Sincronizamos este token con nuestra base de datos para poder enviar notificaciones a este usuario.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM Token", "Refreshed token: $token")
        
        scope.launch {
            UserRepository.updateFcmToken(token)
        }
    }

    /**
     * Crea y muestra una notificación visual en el sistema Android.
     */
    private fun showNotification(title: String, message: String) {
        val channelId = "huerto_fcm_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Configuración necesaria para Android 8.0 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones del Huerto",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intención para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Usamos un ID basado en el tiempo para que las notificaciones no se sobrescriban
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}