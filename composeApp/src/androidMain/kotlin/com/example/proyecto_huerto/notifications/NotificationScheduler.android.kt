package com.example.proyecto_huerto.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Implementación de Android para programar notificaciones locales usando AlarmManager.
 * Permite que los recordatorios de riego funcionen incluso si la app está cerrada.
 */
actual class NotificationScheduler(private val context: Context) {

    /**
     * Programa una alarma que enviará un Broadcast al RiegoReceiver.
     */
    actual fun scheduleRiegoNotification(plantName: String, daysDelay: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12 (API 31) requiere permisos especiales para alarmas exactas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }

        // Intención que se disparará cuando expire el tiempo
        val intent = Intent(context, RiegoReceiver::class.java).apply {
            putExtra("plantName", plantName)
        }

        // Usamos el hashCode del nombre para que cada planta tenga su propia notificación pendiente
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plantName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cálculo del tiempo: milisegundos actuales + días de espera convertidos a milisegundos
        val triggerTime = System.currentTimeMillis() + (daysDelay * 24L * 60 * 60 * 1000)

        // Aseguramos que la alarma se dispare con precisión, incluso en modo Doze (ahorro de energía)
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