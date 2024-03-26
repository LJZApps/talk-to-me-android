package de.ljz.talktome.old.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReceiverCall : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Service Stops", "Ohhhhhhh")
        context.startService(Intent(context, Notification::class.java))
    }
}