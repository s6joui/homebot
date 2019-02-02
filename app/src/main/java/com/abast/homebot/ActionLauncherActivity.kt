package com.abast.homebot

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abast.homebot.HomeBotPreferenceFragment.Companion.KEY_APP_LAUNCH_TYPE
import com.abast.homebot.HomeBotPreferenceFragment.Companion.KEY_APP_LAUNCH_VALUE
import com.abast.homebot.HomeBotPreferenceFragment.Companion.SWITCH_KEY_APP
import com.abast.homebot.HomeBotPreferenceFragment.Companion.SWITCH_KEY_BRIGHTNESS
import com.abast.homebot.HomeBotPreferenceFragment.Companion.SWITCH_KEY_FLASHLIGHT
import com.abast.homebot.HomeBotPreferenceFragment.Companion.SWITCH_KEY_RECENTS
import com.abast.homebot.HomeBotPreferenceFragment.Companion.SWITCH_KEY_SHORTCUT
import com.abast.homebot.HomeBotPreferenceFragment.Companion.SWITCH_KEY_WEB
import java.net.URISyntaxException

class ActionLauncherActivity : AppCompatActivity() {

    companion object {
        const val TORCH_ENABLED = "torch_enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val launchType = sharedPrefs.getString(KEY_APP_LAUNCH_TYPE, null)

        val launchValue = sharedPrefs.getString(KEY_APP_LAUNCH_VALUE, null)

        when(launchType){
            SWITCH_KEY_FLASHLIGHT -> toggleFlashlight(sharedPrefs)
            SWITCH_KEY_BRIGHTNESS -> toggleBrightness()
            SWITCH_KEY_RECENTS -> openRecents()
            SWITCH_KEY_WEB -> openWebAddress(launchValue)
            SWITCH_KEY_SHORTCUT, SWITCH_KEY_APP -> launchUri(launchValue)
            else -> launchMainActivity()
        }
    }

    /**
     * Launches MainActivity. Used as fallback for any errors that might occur.
     */
    private fun launchMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        finish()
        startActivity(i)
    }

    /**
     * Launches a Uri. Used for launching shortcuts and activities.
     */
    private fun launchUri(uri: String?) {
        if (uri != null) {
            try {
                val shortcut = Intent.parseUri(uri, 0)
                startActivity(shortcut)
                finish()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                launchMainActivity()
            }
        }
    }

    /**
     * Toggles flashlight via a Service
     */
    private fun toggleFlashlight(sharedPrefs : SharedPreferences){
        val torchEnabled = sharedPrefs.getBoolean(TORCH_ENABLED,false)
        sharedPrefs.edit().putBoolean(TORCH_ENABLED,!torchEnabled).apply()
        val flashlightIntent = Intent(this,FlashlightService::class.java)
        flashlightIntent.action = if(torchEnabled) FlashlightService.DISABLE_TORCH else FlashlightService.ENABLE_TORCH
        ContextCompat.startForegroundService(this,flashlightIntent)
        finish()
    }

    /**
     * Opens the recent apps screen
     * Source: http://stackoverflow.com/a/15964856/404784
     */
    private fun openRecents() {
        try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getService = serviceManagerClass.getMethod("getService", String::class.java)
            val retbinder = getService.invoke(serviceManagerClass, "statusbar") as IBinder
            val statusBarClass = Class.forName(retbinder.interfaceDescriptor!!)
            val statusBarObject = statusBarClass.classes[0].getMethod("asInterface", IBinder::class.java)
                .invoke(null, *arrayOf<Any>(retbinder))
            val clearAll = statusBarClass.getMethod("toggleRecentApps")
            clearAll.isAccessible = true
            clearAll.invoke(statusBarObject)
            finish()
        } catch (ex: Exception) {
            ex.printStackTrace()
            launchMainActivity()
        }

    }

    /**
     * Toggles brightness between maximum and minimum.
     */
    private fun toggleBrightness() {
        if (Settings.System.canWrite(this)) {
            val cResolver = contentResolver
            try {
                // To handle the auto
                Settings.System.putInt(
                    cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                //Get the current system brightness
                val brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS)
                if (brightness > 0) {
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
                } else {
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255)
                }
                finish()
            } catch (e: Settings.SettingNotFoundException) {
                Log.e("Error", getString(R.string.error_brightness))
                Toast.makeText(this,R.string.error_brightness,Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                launchMainActivity()
            }
        } else {
            launchMainActivity()
        }
    }

    /**
     * Opens web browser pointing to given url
     */
    private fun openWebAddress(url: String?) {
        if(url != null){
            var finalUrl = url
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://"))
                finalUrl = "http://$url"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            startActivity(browserIntent)
            finish()
        }else{
            launchMainActivity()
        }
    }

}
