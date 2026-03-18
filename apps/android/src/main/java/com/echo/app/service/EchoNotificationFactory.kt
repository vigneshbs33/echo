package com.echo.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object EchoNotificationFactory {
    const val CHANNEL_ID = "echo_background_service"
    const val NOTIFICATION_ID = 7

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(com.echo.app.R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps Echo available to polish your text via the bubble overlay."
        }
        manager.createNotificationChannel(channel)
    }

    fun build(context: Context, isIncognito: Boolean = false): Notification {
        val intent = android.content.Intent(context, com.echo.app.service.EchoForegroundService::class.java).apply {
            action = "TOGGLE_INCOGNITO"
        }
        val pendingIntent = android.app.PendingIntent.getService(context, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val actionText = if (isIncognito) "Resume Listening" else "Pause (Incognito)"

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.echo.app.R.drawable.ic_echo_logo)
            .setContentTitle(context.getString(com.echo.app.R.string.notification_title))
            .setContentText(if (isIncognito) "Echo paused directly from notification" else context.getString(com.echo.app.R.string.notification_text))
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, actionText, pendingIntent)
            .build()
    }
}
