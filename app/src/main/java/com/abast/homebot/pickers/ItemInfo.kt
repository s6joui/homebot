package com.abast.homebot.pickers

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

class ItemInfo {

    lateinit var resolveInfo: ResolveInfo
    lateinit var activityInfo: ActivityInfo
    private var type = 0

    constructor(info : ResolveInfo){
        type = 0
        resolveInfo = info
    }

    constructor(info : ActivityInfo){
        type = 1
        activityInfo = info
    }

    fun packageName() : String{
        return if(type == 0){
            resolveInfo.activityInfo.packageName
        }else{
            activityInfo.packageName + "." + activityInfo.name
        }
    }

    fun loadLabel(pm : PackageManager) : CharSequence{
        return if(type == 0){
            resolveInfo.loadLabel(pm)
        }else{
            activityInfo.loadLabel(pm)
        }
    }

    fun loadIcon(pm : PackageManager) : Drawable{
        return if(type == 0){
            resolveInfo?.loadIcon(pm)
        }else{
            activityInfo?.loadIcon(pm)
        }
    }

}