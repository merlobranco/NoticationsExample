package com.sample.notificationsexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

class DirectReplyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)

        if (remoteInput != null) {
            val replyText = remoteInput.getCharSequence(CHAT_KEY)
            val answer = Message(replyText, null)
            MainActivity.messages.add(answer)
            MainActivity.sendOnChannel1Notification(context!!)
        }
    }
}