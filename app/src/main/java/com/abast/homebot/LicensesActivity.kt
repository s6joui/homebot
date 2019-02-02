package com.abast.homebot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_licenses.*

class LicensesActivity : AppCompatActivity() {

    companion object {
        const val LICENSES_HTML_PATH = "file:///android_asset/licenses.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true
        webview.loadUrl(LICENSES_HTML_PATH)
    }

}
