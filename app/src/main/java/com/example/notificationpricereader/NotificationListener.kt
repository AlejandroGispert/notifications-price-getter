package com.example.notificationpricereader

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationText = sbn.notification?.extras?.getCharSequence("android.text")?.toString()
        val packageName = sbn.packageName

        if (notificationText != null) {
            Log.d("NotificationListener", "Notification from $packageName: $notificationText")

            // Broadcast notification to MainActivity
            val intent = Intent("com.example.notificationpricereader.NOTIFICATION") // Fixed the intent action here
            intent.putExtra("text", notificationText)
            intent.putExtra("package", packageName)
            sendBroadcast(intent)
        }
    }
}
