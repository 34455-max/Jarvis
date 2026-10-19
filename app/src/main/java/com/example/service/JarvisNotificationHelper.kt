package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class JarvisNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_CRITICAL = "jarvis_critical_channel"
        const val CHANNEL_BRIEFINGS = "jarvis_briefings_channel"
        const val CHANNEL_REMINDERS = "jarvis_reminders_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "JARVIS Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority urgent messages and security alerts"
                enableVibration(true)
            }

            val briefingChannel = NotificationChannel(
                CHANNEL_BRIEFINGS,
                "JARVIS Daily Briefings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning and evening intelligence summaries"
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "JARVIS Deadlines & Follow-ups",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Impending commitments and schedule reminders"
            }

            manager.createNotificationChannel(criticalChannel)
            manager.createNotificationChannel(briefingChannel)
            manager.createNotificationChannel(remindersChannel)
        }
    }

    fun postCriticalAlert(title: String, message: String, importanceScore: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("JARVIS Alert: $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Importance Score: $importanceScore/100\n$message"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Handled if POST_NOTIFICATIONS is not yet granted
        }
    }

    fun postDailyBriefing(title: String, overview: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BRIEFINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(overview)
            .setStyle(NotificationCompat.BigTextStyle().bigText(overview))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(2001, notification)
        } catch (e: SecurityException) {
            // Handled
        }
    }
}
