package com.sample.notificationsexample

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput

class MainActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var editTextTitle: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var mediaSession: MediaSessionCompat

    // The collection of messages shouldn't be declared here, in this way, but is just for testing purposes
    // We are using the companion object in order to declare it like a static java property
    companion object {
        val messages: MutableList<Message> = ArrayList()
        fun sendOnChannel1Notification(context: Context) {

            // We cannot pass an intent to our notification
            val activityIntent = Intent(context, MainActivity::class.java)
            // Instead we should pass an pending intent, wrapper around the normal intent
            // (allows hand it to the notification manager and execute out intent)
            //      requestCode: If we allow the user to update or cancel this pending intent
            //      flags: Defines what happens when we recreate this pending Intent with a new intent
            val contentIntent = PendingIntent.getActivity(context, 0, activityIntent, 0)


            val remoteInput = RemoteInput.Builder(CHAT_KEY)
                .setLabel("Your answer...")
                .build()

            // Starting the Broadcast receiver instead of the activity
            var replyIntent: Intent
            // this time the flag is required in order to update the message included in the broadcast intent
            var replyPendingIntent: PendingIntent? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                replyIntent = Intent(context, DirectReplyReceiver::class.java)
                replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, 0)
            }
            // For API lower than Android Nougat we should open the Application chat
            // since there is no support for answering in the notification channel
            else {
                // Start Chat activity instead (PendingIntent.getActivity)
                // Cancel notification with notificationManagerCompat.cancel(notificationID)
            }

            val replyAction =
                NotificationCompat.Action.Builder(R.drawable.ic_reply, "Reply", replyPendingIntent)
                    .addRemoteInput(remoteInput).build()

            val messageOwner = Person.Builder().setKey("${Math.random()}").setName("Me").build()
            val messagingStyle = NotificationCompat.MessagingStyle(messageOwner)
            messagingStyle.conversationTitle = "Group chat"

            for (message in messages) {
                val person: Person =
                    if (message.sender != null) Person.Builder().setKey("${Math.random()}")
                        .setName(message.sender).build() else messageOwner
                val notificationMessage =
                    NotificationCompat.MessagingStyle.Message(
                        message.text,
                        message.timestamp,
                        person
                    )
                messagingStyle.addMessage(notificationMessage)
            }


            // The notification channel configuration could/should be set here if we are working with an API lower than Oreo
            // Since here we could override the notification channel properties
            // The provided channel ID will be ignored in APIs lower than Oreo
            // (No version checking is required, it won't crash)
            var notification = NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setStyle(messagingStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE) // Setting the color of the notification
                .setContentIntent(contentIntent)
                .setAutoCancel(true) // The notification is tapped if we automatically dismisses it
                .setOnlyAlertOnce(true) // The notification will pop and sound the alarm the first time is triggered
                .addAction(replyAction) // Adding Reply action button
                .build()

            // If we want to send different notifications at the same time we should provide different ids
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, notification)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationManager = NotificationManagerCompat.from(this)

        editTextTitle = findViewById(R.id.edit_text_title)
        editTextMessage = findViewById(R.id.edit_text_message)

        // Providing background color effect, choosing colors from bits from the provided image
        mediaSession = MediaSessionCompat(this, "MediaSession")

        messages.add(Message("Good morning!", "Jim"))
        messages.add(Message("Hello", null))
        messages.add(Message("Hi!", "Jenny"))
    }

    fun sendOnChannel1(v: View) {
        sendOnChannel1Notification(this)
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
