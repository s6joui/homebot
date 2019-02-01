package com.abast.homebot.pickers

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

class ItemInfo {

    private lateinit var resolveInfo: ResolveInfo
    private lateinit var activityInfo: ActivityInfo
    private var type = 0

    constructor(info : ResolveInfo){
        type = 0
        resolveInfo = info
    }

    constructor(info : ActivityInfo){
        type = 1
        activityInfo = info
    }

    public fun loadLabel(pm : PackageManager) : CharSequence{
        if(type == 0){
            return resolveInfo.loadLabel(pm)
        }else{
            return activityInfo.loadLabel(pm)
        }
    }

    public fun loadIcon(pm : PackageManager) : Drawable{
        if(type == 0){
            return resolveInfo.loadIcon(pm)
        }else{
            return activityInfo.loadIcon(pm)
        }
    }

}