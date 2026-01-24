package com.example.proyecto_huerto.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class NotificationScheduler(private val context: Context) {
    actual fun scheduleRiegoNotification(plantName: String, daysDelay: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Verificar permisos para alarmas exactas en Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
                // Nota: En una app real, deberías informar al usuario antes de mandarlo a ajustes
            }
        }

        val intent = Intent(context, RiegoReceiver::class.java).apply {
            putExtra("plantName", plantName)
        }

        // Usamos el hash del nombre de la planta como ID para permitir múltiples recordatorios
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plantName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (daysDelay * 24L * 60 * 60 * 1000)
        // PARA PRUEBAS RÁPIDAS (puedes descomentar esta línea para que salte en 10 segundos):
        // val triggerTime = System.currentTimeMillis() + 10000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        
        println("DEBUG_NOTIF: Programada notificación para $plantName en $daysDelay días")
    }
}