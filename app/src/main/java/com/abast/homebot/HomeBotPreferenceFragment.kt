package com.abast.homebot

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.preference.*
import com.abast.homebot.pickers.AppPickerActivity
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.abast.homebot.pickers.AppPickerActivity.Companion.EXTRA_PICKED_CONTENT
import com.abast.homebot.pickers.AppPickerActivity.Companion.REQUEST_CODE_APP
import com.abast.homebot.pickers.AppPickerActivity.Companion.REQUEST_CODE_SHORTCUT
import com.abast.homebot.pickers.ItemInfo

class HomeBotPreferenceFragment : PreferenceFragmentCompat() {

    companion object {
        const val KEY_APP_FIRST_LAUNCH = "first_launch"
        const val KEY_APP_LAUNCH_TYPE = "launch_type"
        const val KEY_APP_LAUNCH_VALUE = "launch_value"
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
        appPickerIntent.putExtra(AppPickerActivity.EXTRA_LABEL,getString(R.string.choose_app))
        appPickerIntent.putExtra(AppPickerActivity.EXTRA_PICK_TYPE,AppPickerActivity.PICK_TYPE_APP)

        shortcutPickerIntent = Intent(context,AppPickerActivity::class.java)
        shortcutPickerIntent.putExtra(AppPickerActivity.EXTRA_LABEL,getString(R.string.choose_shortcut))
        shortcutPickerIntent.putExtra(AppPickerActivity.EXTRA_PICK_TYPE,AppPickerActivity.PICK_TYPE_SHORTCUT)

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        val prefCategory = preferenceScreen.getPreference(0) as PreferenceGroup
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        // Store all switches in HashMap
        for (i in 0 until prefCategory.preferenceCount) {
            val pref = prefCategory.getPreference(i);
            switches[pref.key] = pref as SwitchPreference
        }
        // Read saved data
        val currentSwitch = sharedPreferences.getString(KEY_APP_LAUNCH_TYPE, "")
        val summary = sharedPreferences.getString(KEY_APP_ACTIVE_SUMMARY,"")
        switches[currentSwitch!!]?.summary = summary
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = sharedPreferences.edit()
        val switch = preference as SwitchPreference
        uncheckAllSwitchesBut(switch.key)
        if(switch.isChecked){
            editor.putString(KEY_APP_LAUNCH_TYPE,switch.key)
            editor.remove(KEY_APP_ACTIVE_SUMMARY)
            editor.apply()
            when(switch.key){
                SWITCH_KEY_APP -> startActivityForResult(appPickerIntent,AppPickerActivity.REQUEST_CODE_APP)
                SWITCH_KEY_SHORTCUT -> startActivityForResult(shortcutPickerIntent,AppPickerActivity.REQUEST_CODE_SHORTCUT)
                SWITCH_KEY_BRIGHTNESS -> askForBrightnessPermission()
                SWITCH_KEY_WEB -> showWebDialog(){ setLaunchUrl(it) }
            }
        }else{
            editor.remove(KEY_APP_LAUNCH_TYPE)
            editor.remove(KEY_APP_ACTIVE_SUMMARY)
            editor.apply()
            switches[switch.key]?.summary = null
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            val sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            when(requestCode){
                REQUEST_CODE_APP -> {
                    val appData = data?.extras?.getParcelable<ActivityInfo>(EXTRA_PICKED_CONTENT)
                    if(appData != null){
                        val label = appData.loadLabel(activity?.packageManager!!).toString()
                        switches[SWITCH_KEY_APP]?.summary = label
                        val activityIntent = Intent()
                        activityIntent.setClassName(appData.packageName, appData.name)
                        val editor = sharedPreferences.edit()
                        editor.putString(KEY_APP_LAUNCH_VALUE, activityIntent.toUri(Intent.URI_INTENT_SCHEME))
                        editor.putString(KEY_APP_ACTIVE_SUMMARY, label)
                        editor.apply()
                    }
                }
                REQUEST_CODE_SHORTCUT -> {
                    val shortcutIntent = data?.extras?.getParcelable<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
                    val shortcutName = data?.extras?.getString(Intent.EXTRA_SHORTCUT_NAME)
                    switches[SWITCH_KEY_SHORTCUT]?.summary = shortcutName
                    val editor = sharedPreferences.edit()
                    editor.putString(KEY_APP_LAUNCH_VALUE, shortcutIntent?.toUri(Intent.URI_INTENT_SCHEME))
                    editor.putString(KEY_APP_ACTIVE_SUMMARY, shortcutName)
                    editor.apply()
                }
            }
        }else{
            // Sets all switches to off
            switches.map{ it.value.isChecked = false }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if we have brightness permission
        val brightnessSwitch = switches[SWITCH_KEY_BRIGHTNESS]
        if (!Settings.System.canWrite(activity) && brightnessSwitch?.isChecked == true) {
            brightnessSwitch.isChecked = false
        }
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
    private fun showWebDialog(onInput : (String) -> Unit) {
        context?.let{
            val et = EditText(it)
            et.hint = getString(R.string.web_hint)
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.enter_url)
            builder.setView(et)
            builder.setPositiveButton(R.string.ok){dialog, _ ->
                onInput.invoke(et.text.toString())
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.cancel){dialog,_ ->
                dialog.cancel()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
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

    /**
     * Sets the web url to launch
     */
    private fun setLaunchUrl(it: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_APP_LAUNCH_VALUE, it)
        editor.putString(KEY_APP_ACTIVE_SUMMARY, it)
        editor.apply()
        switches[SWITCH_KEY_WEB]?.summary = it
    }


}