package com.orbitmessenger.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.orbitmessenger.R

// import com.google.firebase.messaging.FirebaseMessagingService
// import com.google.firebase.messaging.RemoteMessage

// Simulated FCM Service for architecture demonstration
open class OrbitMessagingService /* : FirebaseMessagingService() */ {

    companion object {
        const val CHANNEL_ID = "orbit_messages"
    }

    // override fun onNewToken(token: String) {
    //     super.onNewToken(token)
    //     // Send this token to the Ktor backend
    // }

    // override fun onMessageReceived(message: RemoteMessage) {
    //     super.onMessageReceived(message)
    //     val title = message.notification?.title ?: "New Message"
    //     val body = message.notification?.body ?: "You have a new encrypted message"
    //     showNotification(applicationContext, title, body)
    // }

    fun showNotification(context: Context, title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming chat messages"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            // Privacy-preserving: Do not show decrypted content on lock screen without settings
            .setContentText("You have a new message")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
