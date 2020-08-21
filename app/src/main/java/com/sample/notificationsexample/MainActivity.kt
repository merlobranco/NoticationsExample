package com.sample.notificationsexample

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.view.Menu
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


const val PROGRESS_MAX = 100

class MainActivity : AppCompatActivity() {

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val activityJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass activityJob, we can cancel all coroutines launched by coroutineScope by calling
     * activityJob.cancel()
     */
    private val coroutineScope = CoroutineScope(activityJob + Dispatchers.Main)

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var editTextTitle: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var mediaSession: MediaSessionCompat

    // The collection of messages shouldn't be declared here, in this way, but is just for testing purposes
    // We are using the companion object in order to declare it like a static java property
    companion object {
        val messages: MutableList<Message> = ArrayList()

        /**
         * Messaging Style and Direct Reply
         */
        fun sendOnChannel1Notification(context: Context) {
            val activityIntent = Intent(context, MainActivity::class.java)
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

        messages.add(Message("Good morning!", "Jim"))
        messages.add(Message("Hello", null))
        messages.add(Message("Hi!", "Jenny"))
    }

    /**
     * Send on Channel 1 Custom notification
     */
    fun sendOnChannel1Cn(v: View) {
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

    /**
     * Send on Channel 2 Custom notification
     */
    fun sendOnChannel2Cn(v: View) {
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

    /**
     * Send on Channel 1 Large Icon and Big Text styles
     * Opening the App from the Notification
     */
    fun sendOnChannel1LiBtAo(v: View) {
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

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.dassie)

        var notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_one)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(largeIcon)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.long_dummy_text))
                    .setBigContentTitle(getString(R.string.big_content_title))
                    .setSummaryText(getString(R.string.summary_text))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(Color.BLUE) // Setting the color of the notification
            .setContentIntent(contentIntent)
            .setAutoCancel(true) // The notification is tapped if we automatically dismisses it
            .setOnlyAlertOnce(true) // The notification will pop and sound the alarm the first time is triggered
            .addAction(R.mipmap.ic_launcher, "Toast", actionIntent) // Adding action button
            .build()

        notificationManager.notify(1, notification)
    }

    /**
     * Send on Channel 2 Inbox Style
     */
    fun sendOnChannel2In(v: View) {
        val title = editTextTitle.text.toString()
        val message = editTextMessage.text.toString()

        var notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine("This is line 1")
                    .addLine("This is line 2")
                    .addLine("This is line 3")
                    .addLine("This is line 4")
                    .addLine("This is line 5")
                    .addLine("This is line 6")
                    .addLine("This is line 7")
                    .setBigContentTitle(getString(R.string.big_content_title))
                    .setSummaryText(getString(R.string.summary_text))
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Providing a new id in order to not override the Notification One
        notificationManager.notify(2, notification)
    }

    /**
     * Send on Channel 1 Big Picture Style
     */
    fun sendOnChannel1Bp(v: View) {
        val title = editTextTitle.text.toString()
        val message = editTextMessage.text.toString()

        val activityIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0)

        val broadcastIntent = Intent(this, NotificationReceiver::class.java)
        broadcastIntent.putExtra("toastMessage", message)

        val actionIntent =
            PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val picture = BitmapFactory.decodeResource(resources, R.drawable.dassie)

        var notification = NotificationCompat.Builder(this, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_one)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(picture)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(picture)
                    .bigLargeIcon(null) // Makes disappear the previous set large icon
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(Color.BLUE)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .addAction(R.mipmap.ic_launcher, "Toast", actionIntent)
            .build()

        notificationManager.notify(1, notification)
    }

    /**
     * Send on Channel 2 Media Style
     */
    fun sendOnChannel2M(v: View) {
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

        notificationManager.notify(2, notification)
    }

    /**
     * ProgressBar Notification
     */
    fun sendOnChannel2Pb(v: View) {
        var notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(getString(R.string.download))
            .setContentText(getString(R.string.download_in_progress))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Notification cannot be dismissed(swept away) by the user
            .setOnlyAlertOnce(true) // If the notification priority were higher it will sound the alarm every time a change is triggered
            // Indeterminate means that does not have an extra progress,
            // it just have an ongoing animation
            // When indeterminate = true, progressMax and progress are ignored
            .setProgress(PROGRESS_MAX, 0, false)

        // Providing a new id in order to not override the Notification One
        notificationManager.notify(2, notification.build())

        // Implementing Runnable through SAM Conversions

        coroutineScope.launch {
            SystemClock.sleep(2000)
            for (progress in 0..PROGRESS_MAX step 10) {
                // Updating the notification progress
                notification.setProgress(PROGRESS_MAX, progress, false)
                // Triggering the notification
                notificationManager.notify(2, notification.build())
                SystemClock.sleep(1000)
            }

            notification.setContentText("Download finished")
                .setProgress(0, 0, false)
                .setOngoing(false)
            notificationManager.notify(2, notification.build())
        }
    }

    /**
     * Notification Channel Settings
     */
    fun sendOnChannel1Cs(v: View) {
        if (!notificationManager.areNotificationsEnabled()) {
            openNotificationSettings()
            return
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isChannelBlocked(CHANNEL_1_ID)) {
            openChannelSettings(CHANNEL_1_ID)
            return
        }

        sendOnChannel1Notification(this)
    }

    /**
     * Notification Groups
     */
    fun sendOnChannel2Ng(v: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            sendOnChannel2Nougat()
        else
            sendOnChannel2LowerNougat()
    }

    fun deleteNotificationChannels(v: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager
            // the channel notification channel will be recreated with the previous user channel configuration
            manager.deleteNotificationChannel(CHANNEL_3_ID)
//            manager.deleteNotificationChannelGroup(GROUP_1_ID)
        }
    }

    /**
     * Approach for Nougat SDK and for on: From the 4th notification a notification group will be created
     * And the event of opening our App by clicking on our Notification Group will be created automatically as well
     */
    private fun sendOnChannel2Nougat() {
        var notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(getString(R.string.title))
            .setContentText(getString(R.string.message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Creating a coroutine In order to avoid freezing the main thread and our app
        coroutineScope.launch {
            for (i in 1..5) {
                SystemClock.sleep(2000)
                // Providing a new id in order to not override the Notification One
                notificationManager.notify(i, notification)
            }
        }
    }

    /**
     * Approach for lower APIs than Nougat
     */
    private fun sendOnChannel2LowerNougat() {

        // Required for opening our Application when the Group notification is clicked
        val activityIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0)

        val title1 = "Title1"
        val message1 = "Message1"
        val title2 = "Title2"
        val message2 = "Message2"

        var notification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(title1)
            .setContentText(message1)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup("example_group")
            .build()


        var notification2 = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_two)
            .setContentTitle(title2)
            .setContentText(message2)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup("example_group")
            .build()

        var summaryNotification = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_reply)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine("$title2 $message2")
                    .addLine("$title1 $message1")
                    .setBigContentTitle("2 new messages")
                    .setSummaryText("user@example.com")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup("example_group")
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setContentIntent(contentIntent)
            .setGroupSummary(true)
            .build()

        // Providing a new ids in order to not override the previous Notifications
        coroutineScope.launch {
            SystemClock.sleep(2000)
            notificationManager.notify(2, notification)
            SystemClock.sleep(2000)
            notificationManager.notify(3, notification2)
            SystemClock.sleep(2000)
            notificationManager.notify(4, summaryNotification)
        }
    }

    private fun openNotificationSettings() {
        var intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            // In this way the system knows it should open the settings for this app
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            // Like opening a normal activity
            startActivity(intent)
        }
        else {
            intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    // This annotation force Android SDK version checking is equal of bigger than Oreo
    // We don't have to do it inside
    @RequiresApi(26)
    private fun isChannelBlocked(channelId: String): Boolean {
        val manager = getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelId)
        return channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE
    }

    @RequiresApi(26)
    private fun openChannelSettings(channelId: String) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        startActivity(intent)
    }


    override fun onDestroy() {
        activityJob.cancel()
        super.onDestroy()
    }
}
