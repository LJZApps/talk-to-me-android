package com.lnzpk.chat_app.old.profile

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.lnzpk.chat_app.R
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class OpenProfilePicture : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.open_profile_picture)

        val toolbar = findViewById<Toolbar>(R.id.openProfilePictureToolbar)
        val username = intent.extras!!["username"].toString()
        val Pb = findViewById<ImageView>(R.id.openProfilePic)
        val window = window

        setSupportActionBar(toolbar)
        toolbar.title = username

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.black)

        try {
            val picture = File("$cacheDir/profilePictures/$username.jpg")

            if (picture.exists()) {
                val is1: InputStream = FileInputStream(picture)
                val size1 = is1.available()
                val picBuffer = ByteArray(size1)

                is1.read(picBuffer)
                is1.close()

                val bmp = BitmapFactory.decodeByteArray(picBuffer, 0, picBuffer.size)
                val d: Drawable = BitmapDrawable(resources, bmp)

                Pb.setImageDrawable(d)
            } else {
                Pb.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_no_profile_picture
                    )
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}