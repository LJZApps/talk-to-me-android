package com.lnzpk.chat_app.settings

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.lnzpk.chat_app.R
import com.lnzpk.chat_app.colors.Colors
import com.lnzpk.chat_app.colors.Colors.setToolbarColor

class DataProtection : AppCompatActivity() {
    var webView: WebView? = null
    var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Colors.isDarkMode(this)) {
            setDarkMode()
        } else {
            setLightMode()
        }
        setContentView(R.layout.data_protection)
        toolbar = findViewById(R.id.privacyToolbar)
        setSupportActionBar(toolbar)
        toolbar!!.inflateMenu(R.menu.data_protection_menu)
        toolbar!!.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.printDataProtection -> createWebPagePrint()
            }
            false
        }
        setToolbarColor(this, this, toolbar!!)
        webView = findViewById(R.id.dataProtectionWebView)
        webView!!.getSettings().javaScriptEnabled = true
        webView!!.getSettings().javaScriptCanOpenWindowsAutomatically = true
        webView!!.loadUrl("https://www.iubenda.com/privacy-policy/63210242/full-legal")
    }

    private fun createWebPagePrint() {
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return;*/
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val printAdapter = webView!!.createPrintDocumentAdapter()
        val jobName = "Privacy policy for Talk to me"
        val builder = PrintAttributes.Builder()
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A5)
        val printJob = printManager.print(jobName, printAdapter, builder.build())
        if (printJob.isCompleted) {
            Toast.makeText(applicationContext, "Gedruckt.", Toast.LENGTH_LONG).show()
        } else if (printJob.isFailed) {
            Toast.makeText(applicationContext, "Druck fehlgeschlagen.", Toast.LENGTH_LONG).show()
        }
        // Save the job object for later status checking
    }

    fun setLightMode() {
        setTheme(R.style.settingsLight)
    }

    fun setDarkMode() {
        setTheme(R.style.settingsDark)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.data_protection_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.printDataProtection) {
            createWebPagePrint()
        }
        return super.onOptionsItemSelected(item)
    }
}