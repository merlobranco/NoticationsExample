package com.sample.notificationsexample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.os.Build

/**
 * Wrapping our whole application with all its all components like activities and services
 * Required for setting up at the start launch of our application no at the in a particular activity
 */

// Notification channel group
const val GROUP_1_ID = "group1"
const val GROUP_2_ID = "group2"

// Setting notification channels
const val CHANNEL_1_ID = "channel1"
const val CHANNEL_2_ID = "channel2"
const val CHANNEL_3_ID = "channel3"
const val CHANNEL_4_ID = "channel4"

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
            val group1 = NotificationChannelGroup(
                GROUP_1_ID,
                getString(R.string.group1_name)
            )

            val group2 = NotificationChannelGroup(
                GROUP_2_ID,
                getString(R.string.group2_name)
            )

            val channel1 = NotificationChannel(
                CHANNEL_1_ID,
                getString(R.string.channel1_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = getString(R.string.channel1_description)
            channel1.group = GROUP_1_ID // Once the channel group is set cannot be changed

            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                getString(R.string.channel2_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel2.description = getString(R.string.channel2_description)
            channel2.group = GROUP_1_ID

            val channel3 = NotificationChannel(
                CHANNEL_3_ID,
                getString(R.string.channel3_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel3.description = getString(R.string.channel3_description)
            channel3.group = GROUP_2_ID // Once the channel group is set cannot be changed

            val channel4 = NotificationChannel(
                CHANNEL_4_ID,
                getString(R.string.channel4_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel4.description = getString(R.string.channel4_description)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannelGroup(group1)
            notificationManager.createNotificationChannelGroup(group2)
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
            notificationManager.createNotificationChannel(channel3)
            notificationManager.createNotificationChannel(channel4)
        }
    }
}