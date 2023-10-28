package com.lnzpk.chat_app.old.settings

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.old.main.Home
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class AppAuthentication : AppCompatActivity() {
    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null
    var next = ""
    var specifyNext = ""
    private var keyStore: KeyStore? = null
    private var cipher: Cipher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkMode) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.activity_app_authentication)
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("next")) {
                next = extras.getString("next", "")
            }
            if (extras.containsKey("specifyNext")) {
                specifyNext = extras.getString("specifyNext", "")
            }
        }
    }

    fun requestAuth() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val fingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager

        //Find id of Error text
        val errorText = findViewById<TextView>(R.id.authText)
        val authImage = findViewById<ImageView>(R.id.authImage)
        val authTextLocked = findViewById<TextView>(R.id.authTextLocked)
        val authImage2 = findViewById<ImageView>(R.id.authImage2)

        // Check whether the device has a Fingerprint sensor.
        if (!fingerprintManager.isHardwareDetected) {
            /**
             * An error message will be displayed if the device does not contain the fingerprint hardware.
             * However if you plan to implement a default authentication method,
             * you can redirect the user to a default authentication activity from here or can skip this method.
             * Example:
             * Intent intent = new Intent(this, YourActivity.class);
             * startActivity(intent);
             * finish();
             */
            authSuccess()
        } else {
            // Checks whether fingerprint permission is set on manifest
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FINGERPRINT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //If permission is not set show error message
                authSuccess()
            } else {
                // Check whether at least one fingerprint is registered on your device
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    //If no fingerprint is registered show error message
                    authSuccess()
                } else {
                    // Checks whether lock screen security is enabled or not
                    if (!keyguardManager.isKeyguardSecure) {
                        //Show error message when screen security is disabled
                        authSuccess()
                    } else {

                        //else generate keystore key
                        generateKey()

                        //Now initiate Cipher, if cipher is initiated successfully then proceed
                        if (cipherInit()) {
                            val cryptoObject = FingerprintManager.CryptoObject(
                                cipher!!
                            )
                            val helper = FingerPrintHandler(
                                this,
                                errorText,
                                authImage,
                                authTextLocked,
                                authImage2,
                                this,
                                next,
                                specifyNext
                            ) //Set Fingerprint Handler class
                            helper.startAuth(
                                fingerprintManager,
                                cryptoObject
                            ) //now start authentication process
                        }
                    }
                }
            }
        }
    }

    protected fun generateKey() {
        try {
            // Get the reference to the key store
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val keyGenerator: KeyGenerator
        keyGenerator = try {
            // Key generator to generate the key
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }
        try {
            keyStore!!.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        cipher = try {
            Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }
        return try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey(
                KEY_NAME,
                null
            ) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            true
        } catch (e: KeyPermanentlyInvalidatedException) {
            false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }

    fun checkFingerprintAv() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                makeAuthReady()
                auth()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED, BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED, BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED, BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> authSuccess()
        }
    }

    fun makeAuthReady() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this@AppAuthentication,
            executor!!, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == 11) {
                        authSuccess()
                    } else {
                        authError(errorCode, errString.toString())
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    authSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    authFailed()
                }
            })
        promptInfo = PromptInfo.Builder()
            .setTitle(getString(R.string.appAuth_promptTitle))
            .setSubtitle(null)
            .setDescription(getString(R.string.appAuth_promptDescr))
            .setNegativeButtonText(getString(android.R.string.cancel))
            .setConfirmationRequired(true)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }

    fun auth() {
        biometricPrompt!!.authenticate(promptInfo!!)
        val imageViewIcon = findViewById<ImageView>(R.id.authImage)
        val authImage2 = findViewById<ImageView>(R.id.authImage2)
        val authText = findViewById<TextView>(R.id.authText)
        authImage2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.locked))
        imageViewIcon.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_fingerprint_searching
            )
        )
        imageViewIcon.setColorFilter(Color.GRAY)
        authText.setTextColor(Color.GRAY)
        authText.text = getString(R.string.appAuth_touchSensor)
    }

    fun authSuccess() {
        val imageViewIcon = findViewById<ImageView>(R.id.authImage)
        val authText = findViewById<TextView>(R.id.authText)
        val authTextLocked = findViewById<TextView>(R.id.authTextLocked)
        val authImage2 = findViewById<ImageView>(R.id.authImage2)
        val anim_out = AnimationUtils.loadAnimation(this, R.anim.auth_fade_in)
        val anim_in = AnimationUtils.loadAnimation(this, R.anim.auth_fade_out)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imageViewIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@AppAuthentication,
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
                    val intent = Intent(this@AppAuthentication, Home::class.java)
                    if (next != "") {
                        intent.putExtra("next", next)
                    }
                    if (specifyNext != "") {
                        intent.putExtra("specifyNext", specifyNext)
                    }
                    startActivity(intent)
                    finish()
                    //overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
                }, 120)
            }
        })
        imageViewIcon.startAnimation(anim_out)
        authText.startAnimation(anim_out)
        authImage2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.unlocked))
        authTextLocked.setText(R.string.appAuth_appUnlocked)
    }

    fun authFailed() {
        val imageViewIcon = findViewById<ImageView>(R.id.authImage)
        val authText = findViewById<TextView>(R.id.authText)
        val anim_out = AnimationUtils.loadAnimation(this, R.anim.auth_fade_in)
        val anim_in = AnimationUtils.loadAnimation(this, R.anim.auth_fade_out)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imageViewIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@AppAuthentication,
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

    fun authError(errorCode: Int, error: String?) {
        val authImage = findViewById<ImageView>(R.id.authImage)
        val authText = findViewById<TextView>(R.id.authText)
        val anim_out = AnimationUtils.loadAnimation(this, R.anim.auth_fade_in)
        val anim_in = AnimationUtils.loadAnimation(this, R.anim.auth_fade_out)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                authImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@AppAuthentication,
                        R.drawable.ic_fingerprint_locked
                    )
                )
                authImage.setColorFilter(Color.RED)
                if (errorCode == 13) {
                    authText.setText(R.string.appAuth_canceledByUser)
                } else {
                    authText.text = error
                }
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

    fun setLightMode() {
        setTheme(R.style.authenticationLight)
    }

    fun setDarkMode() {
        setTheme(R.style.authenticationDark)
    }

    val isDarkMode: Boolean
        get() {
            var darkMode = false
            val theme =
                PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "system")
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

    override fun onResume() {
        super.onResume()
        requestAuth()
        //checkFingerprintAv();
    }

    companion object {
        // Variable used for storing the key in the Android Keystore container
        private const val KEY_NAME = "TalkToMe"
    }
}