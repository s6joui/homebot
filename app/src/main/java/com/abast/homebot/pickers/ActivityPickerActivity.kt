package com.abast.homebot.pickers

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ActivityPickerActivity : BasePickerActivity() {

    companion object {
        const val REQUEST_CODE = 213
        const val EXTRA_APP_INFO = "app_info"
    }

    lateinit var subscription : Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appInfo = intent.extras?.getParcelable<ActivityInfo>(EXTRA_APP_INFO)
        if(appInfo != null) {
            setHeader(appInfo)
            setLoading(true)
            subscription = getActivityList(appInfo)
                .subscribeOn(Schedulers.io())
                .delay(100, TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
                .subscribe(
                    { array ->
                        setListItems(array)
                        setLoading(false)
                    },
                    { error ->
                        Toast.makeText(this,error.localizedMessage,Toast.LENGTH_SHORT).show()
                        setLoading(false)
                    })
        }
    }

    override fun onItemClick(item: ActivityInfo) {
        val intent = Intent()
        intent.putExtra(AppPickerActivity.EXTRA_PICKED_CONTENT,item)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    private fun getActivityList(activityInfo: ActivityInfo): Single<Array<ActivityInfo>> {
        return Single.create<Array<ActivityInfo>> {
            val i = Intent(Intent.ACTION_MAIN)
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            val info: PackageInfo
            try {
                info = packageManager.getPackageInfo(activityInfo.packageName, PackageManager.GET_ACTIVITIES)
                it.onSuccess(info.activities)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                it.onError(e)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        subscription.dispose()
    }
}
