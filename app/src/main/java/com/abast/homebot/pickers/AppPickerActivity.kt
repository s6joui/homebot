package com.abast.homebot.pickers

import android.content.Intent
import android.os.Bundle

class AppPickerActivity : BasePickerActivity() {

    companion object {
        const val REQUEST_CODE_APP = 214
        const val REQUEST_CODE_SHORTCUT = 215
        const val EXTRA_QUERY_INTENT = "query_intent"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val queryIntent = intent.extras?.getParcelable<Intent>(EXTRA_QUERY_INTENT)
        val itemList = packageManager.queryIntentActivities(queryIntent, 0)

        setListItems(itemList.toTypedArray())
    }

}
