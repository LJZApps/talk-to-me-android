package com.lnzpk.chat_app.old.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.main.StartIcon

class NotificationJob : JobService() {
    var database = FirebaseDatabase.getInstance().reference
    var preferences: SharedPreferences? = null
    var username: String? = null
    override fun onStartJob(params: JobParameters): Boolean {
        database.onDisconnect().cancel()
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val user = PreferenceManager.getDefaultSharedPreferences(this)
        username = user.getString("username", "UNKNOWN")
        chatNotifications
        requestNotifications
        return false
    }

    //Send the notification
    val requestNotifications: Unit
        get() {
            database.child("users/$username/requests")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot1: DataSnapshot, previousChildName: String?) {
                        val requestFrom = snapshot1.key
                        if (preferences!!.getBoolean("login", false) == true) {
                            database.child("users/$requestFrom")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        database.child("users/$username/informations/status")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (preferences!!.getBoolean(
                                                            "notiRequests",
                                                            false
                                                        ) && preferences!!.getString(
                                                            "notificationCheck",
                                                            "notSet"
                                                        ) == "accepted"
                                                    ) {
                                                        val status = snapshot.value.toString()
                                                        if (status == "offline") {
                                                            val resultIntent = Intent(
                                                                this@NotificationJob,
                                                                StartIcon::class.java
                                                            )
                                                            resultIntent.putExtra("next", "friends")
                                                                .putExtra("specifyNext", "request")
                                                            val stackBuilder =
                                                                TaskStackBuilder.create(this@NotificationJob)
                                                            stackBuilder.addNextIntentWithParentStack(
                                                                resultIntent
                                                            )
                                                            val resultPendingIntent =
                                                                stackBuilder.getPendingIntent(
                                                                    0,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                                                )
                                                            createRequestChannel()
                                                            val notifyId =
                                                                snapshot1.value.toString().toInt()
                                                            val builder =
                                                                NotificationCompat.Builder(
                                                                    this@NotificationJob,
                                                                    CHANNEL_ID
                                                                )
                                                                    .setSmallIcon(R.drawable.ic_notification)
                                                                    .setContentTitle("Neue Freundschafts-Anfrage")
                                                                    .setContentText("Du hast eine neue Anfrage von $requestFrom")
                                                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                                    .setStyle(
                                                                        NotificationCompat.BigTextStyle()
                                                                            .bigText(
                                                                                "Du hast eine neue Anfrage von $requestFrom"
                                                                            )
                                                                    )
                                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                                    .setContentIntent(
                                                                        resultPendingIntent
                                                                    )
                                                                    .setChannelId("requests")
                                                            val notificationManager =
                                                                NotificationManagerCompat.from(this@NotificationJob)
                                                            val manager = getSystemService(
                                                                NotificationManager::class.java
                                                            )
                                                            val notifications =
                                                                manager.activeNotifications
                                                            for (notification in notifications) {
                                                                if (notification.id != notifyId) {
                                                                    notificationManager.notify(
                                                                        notifyId,
                                                                        builder.build()
                                                                    )
                                                                }
                                                            }

                                                            //Send the notification
                                                            notificationManager.notify(
                                                                notifyId,
                                                                builder.build()
                                                            )
                                                        }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        } else {
                            stopSelf()
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    //String message = map.get("message").toString();
    //String messageKey = snapshot3.getKey();
    val chatNotifications: Unit

    //Open the chat on notification-click

        //Send the notification
        get() {
            database.child("users/$username/chats")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot1: DataSnapshot, previousChildName: String?) {
                        if (preferences!!.getBoolean("login", false)) {
                            database.child("users/" + username + "/chats/" + snapshot1.key + "/messages")
                                .addChildEventListener(object : ChildEventListener {
                                    override fun onChildAdded(
                                        snapshot3: DataSnapshot,
                                        previousChildName: String?
                                    ) {
                                        val map = snapshot3.value as Map<*, *>?
                                        if (snapshot3.child("from")
                                                .exists() && snapshot3.child("from").value.toString() != username
                                        ) {
                                            if (snapshot3.child("status")
                                                    .exists() && snapshot3.child("message").exists()
                                            ) {
                                                val status =
                                                    snapshot3.child("status").value.toString()
                                                if (status == "SENT") {
                                                    database.child("users/$username/informations/status")
                                                        .addListenerForSingleValueEvent(object :
                                                            ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                if (snapshot.exists()) {
                                                                    database.onDisconnect().cancel()
                                                                    val userStatus =
                                                                        snapshot.value.toString()
                                                                    if (preferences!!.getBoolean(
                                                                            "notiMessages",
                                                                            false
                                                                        ) && preferences!!.getString(
                                                                            "notificationCheck",
                                                                            "notSet"
                                                                        ) == "accepted"
                                                                    ) {
                                                                        if (userStatus == "offline") {
                                                                            val from =
                                                                                map!!["from"].toString()
                                                                            //String message = map.get("message").toString();
                                                                            //String messageKey = snapshot3.getKey();
                                                                            val notifyId =
                                                                                map["notifyId"].toString()
                                                                                    .toLong()
                                                                                    .toInt()

                                                                            //Open the chat on notification-click
                                                                            val resultIntent =
                                                                                Intent(
                                                                                    this@NotificationJob,
                                                                                    StartIcon::class.java
                                                                                )
                                                                            resultIntent.putExtra(
                                                                                "next",
                                                                                "chat"
                                                                            )
                                                                                .putExtra(
                                                                                    "specifyNext",
                                                                                    from
                                                                                )
                                                                            val stackBuilder =
                                                                                TaskStackBuilder.create(
                                                                                    this@NotificationJob
                                                                                )
                                                                            stackBuilder.addNextIntentWithParentStack(
                                                                                resultIntent
                                                                            )
                                                                            val resultPendingIntent =
                                                                                stackBuilder.getPendingIntent(
                                                                                    0,
                                                                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                                                                )
                                                                            createChatsChannel()
                                                                            val builder =
                                                                                NotificationCompat.Builder(
                                                                                    this@NotificationJob,
                                                                                    CHANNEL_ID
                                                                                )
                                                                                    .setSmallIcon(R.drawable.ic_notification)
                                                                                    .setContentTitle(
                                                                                        "Neue Chat-Nachricht"
                                                                                    )
                                                                                    .setContentText(
                                                                                        "Du hast eine neue Nachricht von $from"
                                                                                    )
                                                                                    .setCategory(
                                                                                        NotificationCompat.CATEGORY_MESSAGE
                                                                                    )
                                                                                    .setStyle(
                                                                                        NotificationCompat.BigTextStyle()
                                                                                            .bigText(
                                                                                                "Du hast eine neue Nachricht von $from"
                                                                                            )
                                                                                    )
                                                                                    .setPriority(
                                                                                        NotificationCompat.PRIORITY_HIGH
                                                                                    )
                                                                                    .setContentIntent(
                                                                                        resultPendingIntent
                                                                                    )
                                                                                    .setChannelId("chats")
                                                                            val notificationManager =
                                                                                NotificationManagerCompat.from(
                                                                                    this@NotificationJob
                                                                                )
                                                                            val manager =
                                                                                getSystemService(
                                                                                    NotificationManager::class.java
                                                                                )

                                                                            //Send the notification
                                                                            val notifications =
                                                                                manager.activeNotifications
                                                                            for (notification in notifications) {
                                                                                if (notification.id != notifyId) {
                                                                                    notificationManager.notify(
                                                                                        notifyId,
                                                                                        builder.build()
                                                                                    )
                                                                                }
                                                                            }
                                                                            notificationManager.notify(
                                                                                notifyId,
                                                                                builder.build()
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {}
                                                        })
                                                }
                                            }
                                        }
                                    }

                                    override fun onChildChanged(
                                        snapshot1: DataSnapshot,
                                        previousChildName: String?
                                    ) {
                                    }

                                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                                    override fun onChildMoved(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?
                                    ) {
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        } else {
                            stopSelf()
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    private fun createRequestChannel() {
        val name: CharSequence = "Anfragen"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("requests", name, importance)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNewFriendChannel() {
        val name: CharSequence = "Neue Freunde"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("newFriends", name, importance)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createGroupChannel() {
        val name: CharSequence = "Gruppen"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("groups", name, importance)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createPostChannel() {
        val name: CharSequence = "Beiträge"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("posts", name, importance)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createChatsChannel() {
        val name: CharSequence = "Chats"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("chats", name, importance)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createInfoChannel() {
        val name: CharSequence = "Information"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("information", name, importance)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStopJob(params: JobParameters): Boolean {
        val resultIntent = Intent(this@NotificationJob, StartIcon::class.java)
        val stackBuilder = TaskStackBuilder.create(this@NotificationJob)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notifyId = 1
        createInfoChannel()
        val builder = NotificationCompat.Builder(this@NotificationJob, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Service ist gestoppt.")
            .setContentText("Öffne bitte die App, um den Service am laufen zu halten")
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Öffne bitte die App, um den Service am laufen zu halten")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(resultPendingIntent)
            .setOngoing(true)
            .setChannelId("information")
        val notificationManager = NotificationManagerCompat.from(this@NotificationJob)
        val manager = getSystemService(
            NotificationManager::class.java
        )

        //Send the notification
        val notifications = manager.activeNotifications
        for (notification in notifications) {
            if (notification.id != notifyId) {
                notificationManager.notify(notifyId, builder.build())
            }
        }
        notificationManager.notify(notifyId, builder.build())

        // return true to restart the job
        return true
    }

    companion object {
        private const val CHANNEL_ID = "messages"
    }
}