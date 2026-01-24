package com.example.proyecto_huerto.notifications

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSTimeInterval

actual class NotificationScheduler {
    actual fun scheduleRiegoNotification(plantName: String, daysDelay: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("¡Toca regar!")
            setBody("Es hora de regar tu $plantName")
            // setSound(UNNotificationSound.defaultSound()) // Opcional
        }

        // Convertir días a segundos para el trigger de iOS
        val seconds = daysDelay * 24.0 * 60.0 * 60.0
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, false)

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = plantName + "_riego",
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("ERROR_IOS_NOTIF: ${error.localizedDescription}")
            } else {
                println("DEBUG_IOS_NOTIF: Notificación programada para $plantName en $daysDelay días")
            }
        }
    }
}