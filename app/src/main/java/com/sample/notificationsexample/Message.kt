package com.sample.notificationsexample

class Message(val text: CharSequence?, val sender: CharSequence?) {
    val timestamp: Long = System.currentTimeMillis()
}