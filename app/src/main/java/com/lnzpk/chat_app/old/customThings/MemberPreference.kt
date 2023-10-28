package com.lnzpk.chat_app.old.customThings

import android.content.Context
import android.content.DialogInterface
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.colors.Colors.setButtonColor
import com.lnzpk.chat_app.old.group.NewGroup
import java.text.SimpleDateFormat
import java.util.*

class MemberPreference(context: Context?) : Preference(context!!) {
    var preference: Preference? = null
    var username: String? = null
    var groupName: String? = null
    var adminMember: Button? = null
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        layoutResource = if (Colors.isDarkMode(context)) {
            R.layout.group_member_preference_layout_dark
        } else {
            R.layout.group_member_preference_layout_light
        }
        val removeMember = holder.itemView.findViewById<Button>(R.id.removeGroupMember)
        setButtonColor(context, removeMember)
        adminMember = holder.itemView.findViewById(R.id.adminGroupMember)
        setButtonColor(context, adminMember!!)
        removeMember.setOnClickListener {
            AlertDialog.Builder(context)
                .setMessage("Benutzer entfernen?")
                .setPositiveButton("Entfernen") { _: DialogInterface?, _: Int ->
                    removeMember(
                        username,
                        preference,
                        preferenceManager.preferenceScreen
                    )
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }

        val database = FirebaseDatabase.getInstance().reference
        database.child("groups/$groupName/members/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    when (snapshot.value.toString().trim { it <= ' ' }) {
                        "1" -> {
                            adminMember!!.text = "Adminrechte entfernen"
                            adminMember!!.setOnClickListener {
                                removeAdmin(
                                    username,
                                    preference,
                                    preferenceManager.preferenceScreen
                                )
                            }
                        }
                        "0" -> {
                            adminMember!!.text = "Adminrechte hinzufügen"
                            adminMember!!.setOnClickListener {
                                adminMember(
                                    username,
                                    preference,
                                    preferenceManager.preferenceScreen
                                )
                            }
                        }
                        else -> {
                            adminMember!!.text = "ERROR"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    @JvmName("setUsername1")
    fun setUsername(newUsername: String?) {
        username = newUsername
    }

    fun declarePreference(newPreference: Preference?) {
        preference = newPreference
    }

    fun declareGroupName(declaredGroupName: String?) {
        groupName = declaredGroupName
    }

    private fun removeMember(username: String?, member: Preference?, screen: PreferenceScreen) {
        val database = FirebaseDatabase.getInstance().reference
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.wait)
            .setCancelable(false)
        val alert = builder.show()
        database.child("groups/$groupName/members/$username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.ref.removeValue().addOnSuccessListener {
                        val date = Date()
                        val time = SimpleDateFormat("HH:mm").format(date)
                        val messageDate = SimpleDateFormat("dd.MM.yyyy").format(date)
                        val mDatabase = FirebaseDatabase.getInstance().reference
                        val push = mDatabase.push().key
                        val notifyId = generateRandom(15).toInt().toString()
                        val groupMessage = NewGroup.Message(
                            "system",
                            "$username wurde aus der Gruppe entfernt",
                            time,
                            messageDate,
                            "SENT",
                            notifyId
                        )
                        mDatabase.child("groups/$groupName/messages").child(push!!)
                            .setValue(groupMessage).addOnSuccessListener {
                                alert.cancel()
                            screen.removePreference(member!!)
                            Toast.makeText(context, "Benutzer entfernt.", Toast.LENGTH_SHORT).show()
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

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Es ist ein Fehler aufgetreten: " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun adminMember(username: String?, request: Preference?, screen: PreferenceScreen?) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("groups/$groupName/members/$username").setValue("1")
            .addOnSuccessListener {
                adminMember!!.text = "Adminrechte entfernen"
                adminMember!!.setOnClickListener {
                    removeAdmin(
                        username,
                        preference,
                        preferenceManager.preferenceScreen
                    )
                }
            }
    }

    fun removeAdmin(username: String?, request: Preference?, screen: PreferenceScreen?) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("groups/$groupName/members/$username").setValue("0")
            .addOnSuccessListener {
                adminMember!!.text = "Adminrechte hinzufügen"
                adminMember!!.setOnClickListener {
                    adminMember(
                        username,
                        preference,
                        preferenceManager.preferenceScreen
                    )
                }
            }
    }

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