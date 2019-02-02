package com.abast.homebot.pickers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.abast.homebot.R

class AppPickerActivity : BasePickerActivity() {

    companion object {
        const val REQUEST_CODE_APP = 214
        const val REQUEST_CODE_SHORTCUT = 215
        const val REQUEST_CODE_SHORTCUT_CONFIG = 216

        const val PICK_TYPE_APP = "pick_type_app"
        const val PICK_TYPE_SHORTCUT = "pick_type_shortcut"

        const val EXTRA_PICK_TYPE = "pick_type"
        const val EXTRA_LABEL = "label"
        const val EXTRA_PICKED_CONTENT = "result"
    }

    var pickType : String = PICK_TYPE_APP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickType = intent.extras?.getString(EXTRA_PICK_TYPE) ?: PICK_TYPE_APP
        val label = intent.extras?.getString(EXTRA_LABEL)

        var queryIntent : Intent? = null
        if(pickType == PICK_TYPE_APP){
            queryIntent = Intent(Intent.ACTION_MAIN, null)
            queryIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        }else if(pickType == PICK_TYPE_SHORTCUT){
            queryIntent = Intent(Intent.ACTION_CREATE_SHORTCUT, null)
        }

        title = label ?: getString(R.string.choose_app)
        val itemList = packageManager.queryIntentActivities(queryIntent, 0)
        setListItems(itemList.map { it.activityInfo }.toTypedArray())
    }

    override fun onItemClick(item: ActivityInfo) {
        if(pickType == PICK_TYPE_APP){
            val i = Intent(this, ActivityPickerActivity::class.java)
            i.putExtra(ActivityPickerActivity.EXTRA_APP_INFO, item)
            startActivityForResult(i, ActivityPickerActivity.REQUEST_CODE)
        }else if(pickType == PICK_TYPE_SHORTCUT){
            launchShortcutConfig(item)
        }
    }

    /**
     * Launches the shortcut picker for the selected app. The app should return an intent
     */
    private fun launchShortcutConfig(aInfo: ActivityInfo) {
        try {
            val i = Intent(Intent.ACTION_CREATE_SHORTCUT)
            i.setClassName(aInfo.packageName, aInfo.name)
            if (i.resolveActivity(packageManager) != null) {
                startActivityForResult(i, REQUEST_CODE_SHORTCUT_CONFIG)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            if(requestCode == ActivityPickerActivity.REQUEST_CODE || requestCode == REQUEST_CODE_SHORTCUT_CONFIG){
                setResult(Activity.RESULT_OK,data)
                finish()
            }
        }
    }

}
