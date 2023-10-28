package com.lnzpk.chat_app.old.service

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.text.format.DateFormat
import androidx.preference.PreferenceManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class StatusJob : JobService() {
    private val hdlr = Handler()
    var database: DatabaseReference? = null
    var username: String? = null
    var settings: SharedPreferences? = null
    override fun onStartJob(params: JobParameters): Boolean {
        database = FirebaseDatabase.getInstance().reference
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        username = preferences.getString("username", "UNKNOWN")
        val updateStatus: Runnable = object : Runnable {
            override fun run() {
                settings = PreferenceManager.getDefaultSharedPreferences(this@StatusJob)
                if (settings!!.getBoolean("login", false)) {
                    if (Helper.isAppRunning(this@StatusJob, "com.lnzpk.chat_app")) {
                        setOnline()
                    } else {
                        goOffline()
                    }
                    hdlr.postDelayed(this, 100)
                } else {
                    if (settings!!.getBoolean("loggedOut", false)) {
                        goOffline()
                    } else if (settings!!.getBoolean("deleted", false)) {
                        goFinalOffline()
                    }
                }
            }
        }
        updateStatus.run()
        return false
    }

    fun goFinalOffline() {
        database!!.child("users/$username/informations/status").setValue("offline")
            .addOnSuccessListener { unused: Void? ->
                database!!.child(
                    "users/$username"
                ).ref.removeValue().addOnSuccessListener {
                    settings!!.edit().clear().apply()
                    stopSelf()
                }
            }
    }

    fun setOnline() {
        database!!.child("users/$username/informations/status").setValue("online")
            .addOnSuccessListener { unused: Void? -> }
        try {
            val rightNow1 = Calendar.getInstance()
            val date3 = rightNow1.time
            val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val formattedDate1 = format1.format(date3)
            val date2 = format1.parse(formattedDate1)
            rightNow1.time = date2
            rightNow1.timeZone = TimeZone.getDefault()
            val year3 = DateFormat.format("yyyy", date2) as String
            val month3 = DateFormat.format("MM", date2) as String
            val day3 = DateFormat.format("dd", date2) as String
            val hour3 = DateFormat.format("HH", date2) as String
            val min3 = DateFormat.format("mm", date2) as String
            //new date for lastLogin
            val newDate1 = "$year3-$month3-$day3 $hour3:$min3"

            //set current time as lastLogin
            settings!!.edit().putString("lastLogin", newDate1).apply()
            database!!.child("users/$username/informations/lastOnline").setValue(newDate1)
                .addOnFailureListener { e: Exception ->

                }
        } catch (e: Exception) {
        }
    }

    fun goOffline() {
        database!!.child("users/$username/informations/status").setValue("offline")
            .addOnSuccessListener { unused: Void? -> stopSelf() }
    }

    object Helper {
        fun isAppRunning(context: Context, packageName: String): Boolean {
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val procInfos = activityManager.runningAppProcesses
            if (procInfos != null) {
                for (processInfo in procInfos) {
                    if (processInfo.processName == packageName) {
                        return processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    }
                }
            }
            return false
        }
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }
}