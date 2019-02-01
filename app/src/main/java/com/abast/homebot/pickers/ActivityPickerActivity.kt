package com.abast.homebot.pickers

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle

class ActivityPickerActivity : BasePickerActivity() {

    companion object {
        const val REQUEST_CODE = 213
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appInfo = intent.extras?.getParcelable<ResolveInfo>("app")
        if(appInfo != null) {
            val array = getActivityList(appInfo) ?: emptyArray()
            setHeader(ItemInfo(appInfo))
            setListItems(array)
        }
    }

    private fun getActivityList(app: ResolveInfo): Array<ActivityInfo>? {
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val info: PackageInfo
        try {
            info = packageManager.getPackageInfo(app.activityInfo.packageName, PackageManager.GET_ACTIVITIES)
            return info.activities
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

}
