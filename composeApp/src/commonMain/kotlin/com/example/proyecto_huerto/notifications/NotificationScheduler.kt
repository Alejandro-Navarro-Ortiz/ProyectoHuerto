package com.example.proyecto_huerto.notifications

expect class NotificationScheduler {
    fun scheduleRiegoNotification(plantName: String, daysDelay: Int)
}