package com.example.daybrief.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.daybrief.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "daybrief_channel"
    private const val NOTIFICATION_ID = 1001
    const val ACTION_OPEN_BRIEFING = "com.example.daybrief.ACTION_OPEN_BRIEFING"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Morning Briefing",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily AI-generated morning briefing"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun sendBriefingNotification(context: Context, briefingSnippet: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_BRIEFING
            // SINGLE_TOP so the running activity gets onNewIntent instead of a fresh instance
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your Morning Briefing is Ready")
            .setContentText(briefingSnippet.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(briefingSnippet.take(400)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }
}
