package com.java.zhanghx

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter

class NewsAdapter(private val activity: ListActivity, newsList: UltimateRecyclerView) :
    UltimateViewAdapter<NewsAdapter.ViewHolder>() {

    init {
        newsList.setAdapter(this)

    }
    inner class ViewHolder(itemView: View) :
        UltimateRecyclerviewViewHolder<Any>(itemView),
        View.OnClickListener,
        View.OnLongClickListener {
        override fun onClick(p0: View?) {
            TODO("Not yet implemented")
        }

        override fun onLongClick(p0: View?): Boolean {
            TODO("Not yet implemented")
        }
    }

    override fun onBindHeaderViewHolder(p0: RecyclerView.ViewHolder?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun generateHeaderId(position: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getAdapterItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun newFooterHolder(view: View?): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun newHeaderHolder(view: View?): ViewHolder {
        TODO("Not yet implemented")
    }
}