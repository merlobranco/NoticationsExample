package com.sample.notificationsexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var editTextTitle: EditText
    private lateinit var editTextMessage: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationManager = NotificationManagerCompat.from(this)

        editTextTitle = findViewById(R.id.edit_text_title)
        editTextMessage = findViewById(R.id.edit_text_message)
    }

    fun sendOnChannel1(v: View) {
        val title = editTextTitle.text.toString()
        val message = editTextMessage.text.toString()

        // The notification channel configuration could/should be set here if we are working with an API lower than Oreo
        // Since here we could override the notification channel properties
        // The provided channel ID will be ignored in APIs lower than Oreo
        // (No version checking is required, it won't crash)
        var notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_one)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        // If we want to send different notifications at the same time we should provide different ids
        notificationManager.notify(1, notification)
    }

    fun sendOnChannel2(v: View) {
        val title = editTextTitle.text.toString()
        val message = editTextMessage.text.toString()

        var notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Providing a new id in order to not override the Notification One
        notificationManager.notify(2, notification)

    }
}
