package de.ljz.talktome.old.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import de.ljz.talktome.old.chat.OpenChat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class NotificationReply : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val myUsername = intent.extras!!["myUsername"].toString()
        val otherUsername = intent.extras!!["otherUsername"].toString()
        val notificationId = intent.extras!!["notificationId"].toString()
        val date = Date()
        val time = SimpleDateFormat("HH:mm").format(date)
        val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val notificationManager = NotificationManagerCompat.from(context)
        val notifyId = generateRandom(15).toInt().toString()
        if (remoteInput != null) {
            val replyText = remoteInput.getCharSequence("key_text_reply")
            val database = FirebaseDatabase.getInstance().reference
            val push = database.push().key
            val messageFrom = (replyText as String?)?.let {
                OpenChat.Message(
                    myUsername,
                    it,
                    time,
                    messageDate,
                    "SENT",
                    notifyId,
                    "test"
                )
            }
            database.child("users/$myUsername/chats/$otherUsername/messages").child(push!!)
                .setValue(messageFrom).addOnSuccessListener { aVoid: Void? ->
                notificationManager.cancel(notificationId.toLong().toInt())
                val messageTo = replyText?.let {
                    OpenChat.Message(myUsername, it, time, messageDate, "SENT", notifyId, "test")
                }
                database.child("users/$otherUsername/chats/$myUsername/messages").child(push).setValue(messageTo)
                    .addOnSuccessListener { aVoid12: Void? ->
                    notificationManager.cancel(notificationId.toLong().toInt())
                }
            }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        context, """
     Nachricht konnte nicht gesendet werden! 
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    @IgnoreExtraProperties
    class Message(
        var from: String,
        var message: String,
        var time: String,
        var date: String,
        var status: String
    )

    companion object {
        fun generateRandom(length: Int): Long {
            val random = Random()
            val digits = CharArray(length)
            digits[0] = (random.nextInt(9) + '1'.code).toChar()
            for (i in 1 until length) {
                digits[i] = (random.nextInt(10) + '0'.code).toChar()
            }
            return String(digits).toLong()
        }
    }
}