package com.sample.notificationsexample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Wrapping our whole application with all its all components like activities and services
 * Required for setting up at the start launch of our application no at the in a particular activity
 */

// Setting notification channels
const val CHANNEL_1_ID = "channel1"
const val CHANNEL_2_ID = "channel2"
const val CHAT_KEY = "key_text_reply"

class App: Application() {

    /**
     * This will be called before any activity starts.
     * It will start with our App.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // Checking if our SDK is greater than Oreo
        // Notification Channel class is not available in lower versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                CHANNEL_1_ID,
                getString(R.string.channel1_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = getString(R.string.channel1_description)

            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                getString(R.string.channel2_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel2.description = getString(R.string.channel2_description)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
        }
    }
}