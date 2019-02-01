package com.abast.homebot

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.preference.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_APP_FIRST_LAUNCH = "first_launch"
        const val KEY_APP_LAUNCH_MODE = "launch_mode"
        const val KEY_APP_LAUNCH_VALUE = "launch_value"
        const val KEY_APP_ACTIVITY_NAME = "launch_activity"
        const val KEY_APP_SHORTCUT_INTENT = "s_intent"
        const val KEY_APP_ACTIVE_SUMMARY = "summary"

        const val LAUNCH_TYPE_APP = "action_app"
        const val LAUNCH_TYPE_SHORTCUT = "action_shortcut"
        const val LAUNCH_TYPE_WEB = "action_web"
        const val LAUNCH_TYPE_FLASHLIGHT = "action_flashlight"
        const val LAUNCH_TYPE_RECENTS = "action_recents"
        const val LAUNCH_TYPE_LOCK = "action_lock"
        const val LAUNCH_TYPE_BRIGHTNESS = "action_brightness"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.preferences_frame, HomeBotPreferenceFragment()).commit()

        warningButton.setOnClickListener{
            // Opens assistant settings
            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
            val packageManager = packageManager
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this,getString(R.string.error_no_assistant),Toast.LENGTH_LONG).show()
            }
        }
    }

    class HomeBotPreferenceFragment : PreferenceFragmentCompat() {

        private lateinit var sharedPreferences : SharedPreferences

        private var switches : HashMap<String,SwitchPreference> = HashMap()

        override fun onAttach(context: Context?) {
            super.onAttach(context)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
            val prefCategory = preferenceScreen.getPreference(0) as PreferenceGroup
            for (i in 0 until prefCategory.preferenceCount) {
                val pref = prefCategory.getPreference(i);
                switches[pref.key] = pref as SwitchPreference
            }
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            val switch = preference as SwitchPreference
            uncheckAllBut(switch.key)

            return super.onPreferenceTreeClick(preference)
        }

        // Utils
        private fun uncheckAllBut(key: String) {
            for (entry in switches.entries) {
                if (entry.key != key) {
                    val pref = entry.value
                    pref.isChecked = false
                    pref.summary = ""
                }
            }
        }

    }

}