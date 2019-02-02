package com.abast.homebot.pickers

import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import kotlinx.android.synthetic.main.activity_recyclerview.*
import kotlinx.android.synthetic.main.list_item.view.*

abstract class BasePickerActivity : AppCompatActivity() {

    lateinit var adapter : ItemInfoAdapter
    var headerItem : ItemInfo? = null

    abstract fun onItemClick(item: ItemInfo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        adapter = ItemInfoAdapter(this) {
            onItemClick(it)
        }

        recycler_view.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        recycler_view.adapter = adapter
    }

    fun setHeader(item : ItemInfo){
        headerItem = item
        headerTitle.visibility = View.VISIBLE
        header.visibility = View.VISIBLE
        header.label.text = item.loadLabel(packageManager)
        header.subtitle.text = getString(R.string.default_activity)
        header.image.setImageDrawable(item.loadIcon(packageManager))
    }

    fun setListItems(items : Array<ResolveInfo>){
        val list : List<ItemInfo> = items.map{ItemInfo(it)}
        setListItems(list)
    }

    fun setListItems(items : Array<ActivityInfo>){
        val list : List<ItemInfo> = items.map{ItemInfo(it)}
        setListItems(list)
    }

    fun setListItems(items : List<ItemInfo>){
        adapter.items = items
        adapter.notifyDataSetChanged()

        if(items.isEmpty()){
            //Show empty message
        }
    }

}
