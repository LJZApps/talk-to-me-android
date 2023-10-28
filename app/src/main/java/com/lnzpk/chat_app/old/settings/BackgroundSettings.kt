package com.lnzpk.chat_app.old.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.colors.Colors
import com.lnzpk.chat_app.old.colors.Colors.setButtonColor
import com.lnzpk.chat_app.old.colors.Colors.setToolbarColor
import java.io.*
import kotlin.math.roundToInt


class BackgroundSettings : AppCompatActivity() {
    lateinit var currentBgButton: Button
    lateinit var removeBg: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.background_settings)
        val toolbar = findViewById<Toolbar>(R.id.backgroundToolbar)
        setSupportActionBar(toolbar)
        setToolbarColor(this, this, toolbar)
        bg
    }

    val bg: Unit
        get() {
            val currentBg = findViewById<ImageView>(R.id.currentBgImage)
            currentBgButton = findViewById<Button>(R.id.currentBgButton)
            setButtonColor(this, currentBgButton)
            removeBg = findViewById<Button>(R.id.removeBackground)
            setButtonColor(this, removeBg)
            currentBgButton.setOnClickListener { v: View? ->
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
            }
            removeBg.setOnClickListener { v: View? ->
                val bgPath = "$filesDir/pics/background.jpg"
                val bgFile = File(bgPath)
                if (bgFile.exists()) {
                    bgFile.delete()
                    Toast.makeText(
                        this@BackgroundSettings,
                        "Hintergrund erfolgreich entfernt!",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (Colors.isDarkMode(this)) {
                        currentBg.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@BackgroundSettings,
                                R.drawable.background_picture
                            )
                        )
                    } else {
                        currentBg.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@BackgroundSettings,
                                R.drawable.background_light
                            )
                        )
                    }
                } else {
                    Toast.makeText(
                        this@BackgroundSettings,
                        "Kein Hintergrund vorhanden!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            val yourFilePath = "$filesDir/pics/background.jpg"
            val yourFile = File(yourFilePath)
            if (yourFile.exists()) {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeFile(yourFilePath, options)
                currentBg.setImageBitmap(bitmap)
            } else {
                if (Colors.isDarkMode(this)) {
                    currentBg.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@BackgroundSettings,
                            R.drawable.background_picture
                        )
                    )
                } else {
                    currentBg.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@BackgroundSettings,
                            R.drawable.background_light
                        )
                    )
                }
            }
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                val inflater = layoutInflater
                @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.new_background, null)
                view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
                setContentView(view)
                val imageUri = data!!.data
                val imageStream = contentResolver.openInputStream(imageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                val width = displayMetrics.widthPixels
                val newBitmap = Bitmap.createScaledBitmap(selectedImage, width, height, false)

                val save = findViewById<Button>(R.id.newBgSet)
                val choose = findViewById<Button>(R.id.newBgChoose)

                save.visibility = View.GONE
                choose.visibility = View.GONE

                val cropLayout = findViewById<ConstraintLayout>(R.id.cropLayout)
                if(cropLayout != null){
                    cropLayout.visibility = View.VISIBLE
                }
                val cropImageButton = findViewById<Button>(R.id.cropImageButton)
                cropImageButton.visibility = View.VISIBLE

                setButtonColor(this, cropImageButton)

                cropImageButton.setOnClickListener {
                    cropImageButton.visibility = View.GONE
                    cropLayout.visibility = View.GONE

                    save.visibility = View.VISIBLE
                    choose.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@BackgroundSettings, e.message, Toast.LENGTH_LONG).show()
                AlertDialog.Builder(this@BackgroundSettings)
                    .setTitle("An error has occured.")
                    .setMessage(e.stackTraceToString())
                    .show()
            } catch (e: Error) {
                Toast.makeText(this@BackgroundSettings, e.message, Toast.LENGTH_LONG).show()
                Toast.makeText(this@BackgroundSettings, e.message, Toast.LENGTH_LONG).show()
                AlertDialog.Builder(this@BackgroundSettings)
                    .setTitle("An error has occured.")
                    .setMessage(e.stackTraceToString())
                    .show()
            }
        } else {
            if (resultCode == 0) {
            } else {
                Toast.makeText(this, "ERROR-CODE: $resultCode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun newBg(bg: Bitmap) {
        val save = findViewById<Button>(R.id.newBgSet)
        val choose = findViewById<Button>(R.id.newBgChoose)
        setButtonColor(this, save)
        setButtonColor(this, choose)

        save.setOnClickListener { v: View? ->
            val myDir = File("$filesDir/pics")
            myDir.mkdir()
            val fName = "background.jpg"
            val file = File(myDir, fName)
            try {
                val out = FileOutputStream(file)
                val baos = ByteArrayOutputStream()
                bg.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val pic = baos.toByteArray()
                out.flush()
                out.write(pic)
                out.close()
                Toast.makeText(this@BackgroundSettings, "Hintergrund gespeichert.", Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@BackgroundSettings, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        choose.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, RESULT_FIRST_USER)
        }
        val toolbar = findViewById<Toolbar>(R.id.backgroundToolbat)
        setToolbarColor(this, this, toolbar)
        setButtonColor(this, save)
        setButtonColor(this, choose)
        val newBgImage = findViewById<ImageView>(R.id.newBgImage)
        newBgImage.setImageBitmap(bg)
    }

    private fun dpToPx(dp: Int): Int {
        val density = applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}