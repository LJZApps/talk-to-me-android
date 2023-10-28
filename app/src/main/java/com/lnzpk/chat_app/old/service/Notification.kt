package com.lnzpk.chat_app.old.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.main.StartIcon
import com.lnzpk.chat_app.old.newDatabase.DBHelper
import java.util.Random

class Notification : Service() {
    var database = FirebaseDatabase.getInstance().reference
    var username: String? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        val db = DBHelper(this, null)
        database.onDisconnect().cancel()
        username = db.getCurrentUsername()
        chatNotifications
        requestNotifications
    }

    private val requestNotifications: Unit get() {
        val db = DBHelper(this, null)
            database.child("users/$username/requests")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot1: DataSnapshot, previousChildName: String?) {
                        val requestFrom = snapshot1.key
                        if (db.isSomeoneLoggedIn()) {
                            database.child("users/$requestFrom")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        database.child("users/$username/informations/status")
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    // preferences!!.getBoolean("notiRequests", false) && preferences!!.getString("notificationCheck", "notSet") == "accepted"
                                                    if (db.getSettingBoolean("notiRequests", false) && db.getSettingString("notificationCheck", "notSet") == "accepted") {
                                                        val status = snapshot.value.toString()
                                                        if (status == "offline") {
                                                            val resultIntent = Intent(this@Notification, StartIcon::class.java)
                                                            resultIntent.putExtra("next", "friends")
                                                                .putExtra("specifyNext", "request")
                                                            val stackBuilder = TaskStackBuilder.create(this@Notification)
                                                            stackBuilder.addNextIntentWithParentStack(resultIntent)
                                                            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                                            createRequestChannel()
                                                            val notifyId = snapshot1.value.toString().toInt()
                                                            val builder = NotificationCompat.Builder(this@Notification, CHANNEL_ID)
                                                                    .setSmallIcon(R.drawable.ic_notification)
                                                                    .setContentTitle("Neue Freundschafts-Anfrage")
                                                                    .setContentText("Du hast eine neue Anfrage von $requestFrom")
                                                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                                    .setStyle(NotificationCompat.BigTextStyle().bigText("Du hast eine neue Anfrage von $requestFrom"))
                                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                                    .setContentIntent(resultPendingIntent)
                                                                    .setChannelId("requests")
                                                            val notificationManager = NotificationManagerCompat.from(this@Notification)
                                                            val manager = getSystemService(NotificationManager::class.java)
                                                            val notifications = manager.activeNotifications
                                                            for (notification in notifications) {
                                                                if (notification.id != notifyId) {
                                                                    notificationManager.notify(notifyId, builder.build())
                                                                }
                                                            }

                                                            //Send the notification
                                                            notificationManager.notify(notifyId, builder.build())
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

    private val chatNotifications: Unit
        get() {
            val db = DBHelper(this, null)
            database.child("users/$username/chats").addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot1: DataSnapshot, previousChildName: String?) {
                        if (db.isSomeoneLoggedIn()) {
                            database.child("users/" + username + "/chats/" + snapshot1.key + "/messages").addChildEventListener(object : ChildEventListener {
                                    override fun onChildAdded(snapshot3: DataSnapshot, previousChildName: String?) {
                                        val map = snapshot3.value as Map<*, *>?
                                        if (snapshot3.child("from").exists() && snapshot3.child("from").value.toString() != username) {
                                            if (snapshot3.child("status").exists() && snapshot3.child("message").exists()) {
                                                val status = snapshot3.child("status").value.toString()
                                                if (status == "SENT") {
                                                    database.child("users/$username/informations/status").addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                if (snapshot.exists()) {
                                                                    database.onDisconnect().cancel()
                                                                    val userStatus = snapshot.value.toString()
                                                                    //preferences!!.getBoolean("notiMessages", false) && preferences!!.getString("notificationCheck", "notSet") == "accepted"
                                                                    if (db.getSettingBoolean("notiMessages", false) && db.getSettingString("notificationCheck", "notSet") == "accepted") {
                                                                        if (userStatus == "offline") {
                                                                            val from = map!!["from"].toString()
                                                                            //String message = map.get("message").toString();
                                                                            //String messageKey = snapshot3.getKey();
                                                                            val notifyId = map["notifyId"].toString().toLong().toInt()

                                                                            //Open the chat on notification-click
                                                                            val resultIntent =
                                                                                Intent(this@Notification, StartIcon::class.java)
                                                                            resultIntent.putExtra("next", "chat")
                                                                                .putExtra("specifyNext", from)
                                                                            val stackBuilder = TaskStackBuilder.create(this@Notification)
                                                                            stackBuilder.addNextIntentWithParentStack(resultIntent)
                                                                            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                                                            createChatsChannel()
                                                                            val builder = NotificationCompat.Builder(this@Notification, CHANNEL_ID)
                                                                                    .setSmallIcon(R.drawable.ic_notification)
                                                                                    .setContentTitle("Neue Chat-Nachricht")
                                                                                    .setContentText("Du hast eine neue Nachricht von $from")
                                                                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                                                    .setStyle(NotificationCompat.BigTextStyle().bigText("Du hast eine neue Nachricht von $from"))
                                                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                                                    .setContentIntent(resultPendingIntent)
                                                                                    .setChannelId("chats")
                                                                            val notificationManager = NotificationManagerCompat.from(this@Notification)
                                                                            val manager = getSystemService(NotificationManager::class.java)

                                                                            //Send the notification
                                                                            val notifications = manager.activeNotifications
                                                                            for (notification in notifications) {
                                                                                if (notification.id != notifyId) {
                                                                                    notificationManager.notify(notifyId, builder.build())
                                                                                }
                                                                            }
                                                                            notificationManager.notify(notifyId, builder.build())
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

                                    override fun onChildChanged(snapshot1: DataSnapshot, previousChildName: String?) {
                                    }

                                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        } else {
                            stopSelf()
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    //Funktionen sind kommentiert
    override fun onDestroy() {
        //super.onDestroy();
        database.child("users/$username/chats").onDisconnect().cancel()
        /*
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android

        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);
         */
    }

    private fun createRequestChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Anfragen"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("requests", name, importance)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNewFriendChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Neue Freunde"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("newFriends", name, importance)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createGroupChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Gruppen"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("groups", name, importance)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPostChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Beiträge"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("posts", name, importance)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createChatsChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Chats"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("chats", name, importance)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    //Funktionen sind kommentiert
    override fun onTaskRemoved(rootIntent: Intent) {
        database.onDisconnect().cancel()
        /*
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android

        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);
         */
    }

    companion object {
        const val CHANNEL_ID = "messages"
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