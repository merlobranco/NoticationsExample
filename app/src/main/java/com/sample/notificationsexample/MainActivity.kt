package com.sample.notificationsexample

import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.se.omapi.Session
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var editTextTitle: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationManager = NotificationManagerCompat.from(this)

        editTextTitle = findViewById(R.id.edit_text_title)
        editTextMessage = findViewById(R.id.edit_text_message)

        // Providing background color effect, choosing colors from bits from the provided image
        mediaSession = MediaSessionCompat(this, "MediaSession")
    }

    fun sendOnChannel1(v: View) {
        val title = editTextTitle.text.toString()
        val message = editTextMessage.text.toString()

        // We cannot pass an intent to our notification
        val activityIntent = Intent(this, MainActivity::class.java)
        // Instead we should pass an pending intent, wrapper around the normal intent
        // (allows hand it to the notification manager and execute out intent)
        //      requestCode: If we allow the user to update or cancel this pending intent
        //      flags: Defines what happens when we recreate this pending Intent with a new intent
        val contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0)

        // Starting the Broadcast receiver instead of the activity
        val broadcastIntent = Intent(this, NotificationReceiver::class.java)
        broadcastIntent.putExtra("toastMessage", message)

        // this time the flag is required in order to update the message included in the broadcast intent
        val actionIntent =
            PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val picture = BitmapFactory.decodeResource(resources, R.drawable.dassie)

        // The notification channel configuration could/should be set here if we are working with an API lower than Oreo
        // Since here we could override the notification channel properties
        // The provided channel ID will be ignored in APIs lower than Oreo
        // (No version checking is required, it won't crash)
        var notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_one)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(picture)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText(getString(R.string.long_dummy_text))
//                    .setBigContentTitle(getString(R.string.big_content_title))
//                    .setSummaryText(getString(R.string.summary_text))
//            )
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(picture)
                    .bigLargeIcon(null) // Makes disappear the previous set large icon
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(Color.BLUE) // Setting the color of the notification
            .setContentIntent(contentIntent)
            .setAutoCancel(true) // The notification is tapped if we automatically dismisses it
            .setOnlyAlertOnce(true) // The notification will pop and sound the alarm the first time is triggered
            .addAction(R.mipmap.ic_launcher, "Toast", actionIntent) // Adding action button
            .build()

        // If we want to send different notifications at the same time we should provide different ids
        notificationManager.notify(1, notification)
    }

    fun sendOnChannel2(v: View) {
        val title = editTextTitle.text.toString()
        val message = editTextMessage.text.toString()

        // We could put a whole media player notifier (video, audio) instead of a simple image
        val artwork = BitmapFactory.decodeResource(resources, R.drawable.valiant)

        var notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(artwork)
            // Adding button that will trigger actions. These intents should be configured properly
            .addAction(R.drawable.ic_dislike, "Dislike", null)
            .addAction(R.drawable.ic_previous, "Previous", null)
            .addAction(R.drawable.ic_pause, "Pause", null)
            .addAction(R.drawable.ic_next, "Next", null)
            .addAction(R.drawable.ic_like, "Like", null)
//            .setStyle(
//                NotificationCompat.InboxStyle()
//                    .addLine("This is line 1")
//                    .addLine("This is line 2")
//                    .addLine("This is line 3")
//                    .addLine("This is line 4")
//                    .addLine("This is line 5")
//                    .addLine("This is line 6")
//                    .addLine("This is line 7")
//                    .setBigContentTitle(getString(R.string.big_content_title))
//                    .setSummaryText(getString(R.string.summary_text))
//            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    // Passing previous configure actions
                    .setShowActionsInCompactView(1, 2, 3)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setSubText("Sub Text")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Providing a new id in order to not override the Notification One
        notificationManager.notify(2, notification)

    }
}
