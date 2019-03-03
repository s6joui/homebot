package com.abast.homebot.pickers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.abast.homebot.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

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

    private var pickType : String = PICK_TYPE_APP

    private lateinit var subscription: Disposable

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
        setLoading(true)
        subscription = getActivities(queryIntent)
            .subscribeOn(Schedulers.io())
            .delay(100, TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
            .subscribe { list ->
                setListItems(list)
                setLoading(false)
            }
    }

    private fun getActivities(queryIntent: Intent?) : Single<Array<ActivityInfo>> {
        return Single.create<Array<ActivityInfo>>{
            if(queryIntent == null){
                it.onError(Throwable("Empty intent"))
            }
            val itemList = packageManager.queryIntentActivities(queryIntent, 0)
            val array = itemList.map { resolveInfo -> resolveInfo.activityInfo }.toTypedArray()
            array.sortBy { activityInfo -> activityInfo.loadLabel(packageManager).toString().toLowerCase() }
            it.onSuccess(array)
        }
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

    override fun onStop() {
        super.onStop()
        subscription.dispose()
    }
}
