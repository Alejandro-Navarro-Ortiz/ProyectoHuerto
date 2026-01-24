package com.example.proyecto_huerto.notifications

/**
 * Declaración 'expect' para el programador de notificaciones.
 * Esta clase se implementa de forma nativa en Android e iOS para manejar
 * los sistemas de alarmas y notificaciones locales de cada plataforma.
 */
expect class NotificationScheduler {
    /**
     * Programa un recordatorio de riego para una planta específica.
     * @param plantName Nombre de la hortaliza a regar.
     * @param daysDelay Cantidad de días en el futuro para disparar la alerta.
     */
    fun scheduleRiegoNotification(plantName: String, daysDelay: Int)
}