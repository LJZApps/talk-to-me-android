package com.lnzpk.chat_app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.FirebaseDatabase

class NotificationActions : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.extras!!["action"].toString()
        when (action) {
            "markAsRead" -> {
                val messageKey = intent.extras!!["messageKey"].toString()
                val myUsername = intent.extras!!["myUsername"].toString()
                val otherUsername = intent.extras!!["otherUsername"].toString()
                val notificationId = intent.extras!!["notificationId"].toString()
                val notificationManager = NotificationManagerCompat.from(context)
                val database = FirebaseDatabase.getInstance().reference
                database.child("users/$otherUsername/chats/$myUsername/messages/$messageKey/status").setValue("READ").addOnSuccessListener { unused: Void? ->
                    database.child("users/$myUsername/chats/$otherUsername/messages/$messageKey/status").setValue("READ")
                            .addOnFailureListener { e: Exception ->
                                Toast.makeText(context, """Error:${e.message}""".trimIndent(), Toast.LENGTH_SHORT).show()
                            }
                            .addOnSuccessListener { notificationManager.cancel(notificationId.toInt()) }
                }
            }
            "requestAccept" -> Toast.makeText(context, "Akzeptiert", Toast.LENGTH_SHORT).show()
            "requestDecline" -> Toast.makeText(context, "Abgelehnt", Toast.LENGTH_SHORT).show()
        }
    }
}