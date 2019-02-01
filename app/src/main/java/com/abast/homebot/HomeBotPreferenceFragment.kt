package com.abast.homebot

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import com.abast.homebot.pickers.AppPickerActivity
import android.net.Uri
import android.os.Build
import android.provider.Settings

class HomeBotPreferenceFragment : PreferenceFragmentCompat() {

    companion object {
        const val KEY_APP_FIRST_LAUNCH = "first_launch"
        const val KEY_APP_LAUNCH_MODE = "launch_mode"
        const val KEY_APP_LAUNCH_VALUE = "launch_value"
        const val KEY_APP_ACTIVITY_NAME = "launch_activity"
        const val KEY_APP_SHORTCUT_INTENT = "s_intent"
        const val KEY_APP_ACTIVE_SUMMARY = "summary"

        const val SWITCH_KEY_APP = "action_app"
        const val SWITCH_KEY_SHORTCUT = "action_shortcut"
        const val SWITCH_KEY_WEB = "action_web"
        const val SWITCH_KEY_FLASHLIGHT = "action_flashlight"
        const val SWITCH_KEY_RECENTS = "action_recents"
        const val SWITCH_KEY_BRIGHTNESS = "action_brightness"
    }

    private lateinit var sharedPreferences : SharedPreferences
    private var switches : HashMap<String, SwitchPreference> = HashMap()

    private lateinit var appPickerIntent : Intent
    private lateinit var shortcutPickerIntent : Intent

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Make intents for App and shortcut picker activities
        appPickerIntent = Intent(context,AppPickerActivity::class.java)
        shortcutPickerIntent = Intent(context,AppPickerActivity::class.java)

        val appQueryIntent = Intent(Intent.ACTION_MAIN, null)
        appQueryIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        appPickerIntent.putExtra(AppPickerActivity.EXTRA_QUERY_INTENT,appQueryIntent)

        val shortcutQueryIntent = Intent(Intent.ACTION_CREATE_SHORTCUT, null)
        shortcutPickerIntent.putExtra(AppPickerActivity.EXTRA_QUERY_INTENT,shortcutQueryIntent)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        val prefCategory = preferenceScreen.getPreference(0) as PreferenceGroup

        // Store all switches in hashmap
        for (i in 0 until prefCategory.preferenceCount) {
            val pref = prefCategory.getPreference(i);
            switches[pref.key] = pref as SwitchPreference
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val switch = preference as SwitchPreference
        uncheckAllSwitchesBut(switch.key)
        when(switch.key){
            SWITCH_KEY_APP -> startActivityForResult(appPickerIntent,AppPickerActivity.REQUEST_CODE_APP)
            SWITCH_KEY_SHORTCUT -> startActivityForResult(shortcutPickerIntent,AppPickerActivity.REQUEST_CODE_SHORTCUT)
            SWITCH_KEY_WEB -> showWebDialog()
            SWITCH_KEY_BRIGHTNESS -> askForBrightnessPermission()
        }
        return super.onPreferenceTreeClick(preference)
    }

    // ============== Utils ============== //

    /**
     * Opens settings app to enable settings write
     */
    private fun askForBrightnessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(activity)) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + activity!!.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    /**
     * Shows dialog with EditText to enter a url
     */
    private fun showWebDialog() {

    }

    /**
     *  Toggles all switches off except the one with the provided key
     */
    private fun uncheckAllSwitchesBut(key: String) {
        for (entry in switches.entries) {
            if (entry.key != key) {
                val pref = entry.value
                pref.isChecked = false
                pref.summary = ""
            }
        }
    }

}