package de.ljz.talktome.old.group

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.ljz.talktome.R
import de.ljz.talktome.old.colors.Colors
import de.ljz.talktome.old.colors.Colors.setToolbarColor
import de.ljz.talktome.old.customThings.MemberPreference
import de.ljz.talktome.old.profile.OpenProfile

class GroupMembers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.group_members)
        groupKey = intent.extras!!["groupKey"].toString()
        context = baseContext
        val toolbar = findViewById<Toolbar>(R.id.groupMembersToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.groupMembersFrame, groupMembersLoader())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setLightMode() {
        setTheme(R.style.contactsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.contactsDark)
    }

    class groupMembersLoader : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.members, rootKey)
            loadMembers()
        }

        fun loadMembers() {
            val database = FirebaseDatabase.getInstance().reference
            val screen = preferenceScreen
            database.child("groups/" + groupKey + "/members")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val name = snapshot.key
                        database.child("users/$name")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot2: DataSnapshot) {
                                    if (snapshot2.exists()) {
                                        val name2 =
                                            snapshot2.child("informations/name").value.toString()
                                        val memberPreference = MemberPreference(screen.context)
                                        if (isDarkMode) {
                                            memberPreference.layoutResource =
                                                R.layout.group_member_preference_layout_dark
                                        } else {
                                            memberPreference.layoutResource =
                                                R.layout.group_member_preference_layout_light
                                        }
                                        memberPreference.declarePreference(memberPreference)
                                        memberPreference.setUsername(name)
                                        memberPreference.title = name2
                                        memberPreference.declareGroupName(groupKey)
                                        memberPreference.onPreferenceClickListener =
                                            Preference.OnPreferenceClickListener { preference: Preference? ->
                                                val intent =
                                                    Intent(activity, OpenProfile::class.java)
                                                intent.putExtra("username", name)
                                                    .putExtra("comeFrom", "group")
                                                startActivity(intent)
                                                false
                                            }
                                        screen.addPreference(memberPreference)
                                    } else {
                                        // Delete non-existed member
                                        snapshot2.ref.removeValue()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
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

        val isDarkMode: Boolean
            get() {
                var darkMode = false
                val theme = PreferenceManager.getDefaultSharedPreferences(
                    Companion.context!!
                ).getString("app_theme", "system")
                if ("system" == theme) {
                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> darkMode = true
                        Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> darkMode =
                            false
                    }
                } else if ("light" == theme) {
                    darkMode = false
                } else if ("dark" == theme) {
                    darkMode = true
                }
                return darkMode
            }
    }

    companion object {
        var groupKey: String? = null
        var context: Context? = null
    }
}