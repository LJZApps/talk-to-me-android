package de.ljz.talktome.old.settings

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.ljz.talktome.R
import de.ljz.talktome.old.main.Home

/*  Implement Fingerprint Authentication Callback to get access to Fingerprint methods  */
@TargetApi(Build.VERSION_CODES.M)
class FingerPrintHandler(
    private val context: Context,
    private val authText: TextView,
    private val authImage: ImageView,
    private val lockedText: TextView,
    private val lockedImage: ImageView,
    private val activity: Activity,
    private val next: String,
    private val specifyNext: String
) : FingerprintManager.AuthenticationCallback() {
    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject?) {
        val cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_FINGERPRINT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
        val imageViewIcon = authImage
        val authImage2 = lockedImage
        val authText = authText
        authImage2.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.locked))
        imageViewIcon.setImageDrawable(
            ContextCompat.getDrawable(
                activity,
                R.drawable.ic_fingerprint_searching
            )
        )
        imageViewIcon.setColorFilter(Color.GRAY)
        authText.setTextColor(Color.GRAY)
        authText.text = activity.getString(R.string.appAuth_touchSensor)
    }

    /*  Method will call on Fingerprint Auth Error  */
    @Deprecated("Deprecated in Java")
    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        val authImage = authImage
        val authText = authText
        val anim_out = AnimationUtils.loadAnimation(
            activity, R.anim.auth_fade_in
        )
        val anim_in = AnimationUtils.loadAnimation(
            activity, R.anim.auth_fade_out
        )
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                authImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_fingerprint_locked
                    )
                )
                authImage.setColorFilter(Color.RED)
                authText.text = errString
                authText.setTextColor(Color.RED)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                authImage.startAnimation(anim_in)
                authText.startAnimation(anim_in)
            }
        })
        authImage.startAnimation(anim_out)
        authText.startAnimation(anim_out)
    }

    /*  Method will call on Fingerprint Auth have some help  */
    @Deprecated("Deprecated in Java")
    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        val imageViewIcon = authImage
        val authText = authText
        val anim_out = AnimationUtils.loadAnimation(
            activity, R.anim.auth_fade_in
        )
        val anim_in = AnimationUtils.loadAnimation(
            activity, R.anim.auth_fade_out
        )
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imageViewIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_fingerprint_help
                    )
                )
                imageViewIcon.setColorFilter(Color.YELLOW)
                authText.text = helpString
                authText.setTextColor(Color.YELLOW)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                imageViewIcon.startAnimation(anim_in)
                authText.startAnimation(anim_in)
            }
        })
        imageViewIcon.startAnimation(anim_out)
        authText.startAnimation(anim_out)
    }

    /*  Method will call on Fingerprint Auth Failed  */
    @Deprecated("Deprecated in Java")
    override fun onAuthenticationFailed() {
        val imageViewIcon = authImage
        val authText = authText
        val anim_out = AnimationUtils.loadAnimation(
            activity, R.anim.auth_fade_in
        )
        val anim_in = AnimationUtils.loadAnimation(
            activity, R.anim.auth_fade_out
        )
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imageViewIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_fingerprint_failed
                    )
                )
                imageViewIcon.setColorFilter(Color.RED)
                authText.setText(R.string.appAuth_noMatch)
                authText.setTextColor(Color.RED)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                imageViewIcon.startAnimation(anim_in)
                authText.startAnimation(anim_in)
            }
        })
        imageViewIcon.startAnimation(anim_out)
        authText.startAnimation(anim_out)
    }

    /*  Method will call on Fingerprint Auth Succeeded  */
    @Deprecated("Deprecated in Java")
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        Log.d("Authentication", "Fingerprint Authentication successful.")
        onAuthSuccess()
    }

    /*  Trigger this method on FingerPrint get Success  */
    private fun onAuthSuccess() {
        val imageViewIcon = authImage
        val authText = authText
        val authTextLocked = lockedText
        val authImage2 = lockedImage
        val anim_out = AnimationUtils.loadAnimation(context, R.anim.auth_fade_in)
        val anim_in = AnimationUtils.loadAnimation(context, R.anim.auth_fade_out)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imageViewIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_fingerprint_succesfull
                    )
                )
                imageViewIcon.setColorFilter(Color.GREEN)
                authText.setText(R.string.appAuth_welcome)
                authText.setTextColor(Color.GREEN)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                imageViewIcon.startAnimation(anim_in)
                authText.startAnimation(anim_in)
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(context, Home::class.java)
                    if (next != "") {
                        intent.putExtra("next", next)
                    }
                    if (specifyNext != "") {
                        intent.putExtra("specifyNext", specifyNext)
                    }
                    context.startActivity(intent)
                    activity.finish()
                    //activity.overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
                }, 120)
            }
        })
        imageViewIcon.startAnimation(anim_out)
        authText.startAnimation(anim_out)
        authImage2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.unlocked))
        authTextLocked.setText(R.string.appAuth_appUnlocked)
    }
}