package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.JarvisApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JarvisNotificationListenerService : NotificationListenerService() {

    private val TAG = "JarvisNotificationSvc"
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return
        val extras = sbn.notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val sourceApp = when {
            pkg.contains("android.gm") || pkg.contains("email") -> "Gmail"
            pkg.contains("whatsapp") -> "WhatsApp"
            pkg.contains("instagram") -> "Instagram"
            pkg.contains("facebook.orca") || pkg.contains("messenger") -> "Messenger"
            pkg.contains("slack") -> "Slack"
            pkg.contains("mms") || pkg.contains("messaging") -> "SMS"
            else -> return // Only ingest supported communication channels
        }

        Log.d(TAG, "Ingesting incoming notification from $sourceApp: $title")

        // Parse and persist through repository
        val app = application as? JarvisApplication
        app?.repository?.let { repo ->
            scope.launch {
                val analyzed = repo.analyzeAndStoreMessage(
                    sourceApp = sourceApp,
                    sender = title.ifBlank { "Notification Contact" },
                    subject = title,
                    rawContent = text,
                    isMock = false
                )
                // If critical, trigger alert sound or notification if enabled
                if (analyzed.urgency == "CRITICAL") {
                    app.notificationHelper.postCriticalAlert(
                        title = "${analyzed.sourceApp}: ${analyzed.sender}",
                        message = analyzed.summary,
                        importanceScore = analyzed.importanceScore
                    )
                }
            }
        }
    }
}
