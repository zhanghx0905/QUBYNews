package com.java.zhanghx

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.core.FuelError
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter

class NewsAdapter(private val activity: ListActivity, val newsListView: UltimateRecyclerView) :
    UltimateViewAdapter<NewsAdapter.ViewHolder>() {

    fun notifyDataSetChangedSafely() {
        newsListView.post { notifyDataSetChanged(); };
    }

    init {
        newsListView.setAdapter(this)

        newsListView.setDefaultOnRefreshListener {
            NewsData.refresh({ makeErrorToast(it) }) {
                newsListView.setRefreshing(false)
                notifyDataSetChanged()
                makeSuccessToast()
            }
        }

        newsListView.reenableLoadmore()
        newsListView.setOnLoadMoreListener { _, _ ->
            val mSwipeRefreshLayout = newsListView.mSwipeRefreshLayout
            if (NewsData.curKindId == LATEST_IDX) {
                mSwipeRefreshLayout.post { mSwipeRefreshLayout.isRefreshing = true }
                NewsData.loadMore({ makeErrorToast(it) }) {
                    //newsListView.setRefreshing(false)
                    notifyDataSetChanged()
                    makeSuccessToast()
                }
            }
        }
        NewsData.refresh({ makeErrorToast(it) }) { notifyDataSetChanged() }
    }

    private fun makeSuccessToast() {
        Toast.makeText(activity, "加载成功！", Toast.LENGTH_SHORT).show()
    }

    private fun makeErrorToast(error: FuelError) {
        Toast.makeText(
            activity,
            "${activity.resources.getString(R.string.network_err)}: $error",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun doSearch(keywords: String) {
        NewsData.doSearch(keywords)
        notifyDataSetChangedSafely()
    }

    fun setSearch(newsList: List<News>) {
        NewsData.setSearch(newsList)
        notifyDataSetChangedSafely()
    }

    fun setCurKind(name: CharSequence) {
        NewsData.curKindId = ALL_KIND.indexOf(name)
        notifyDataSetChangedSafely()
    }

    fun setCurType(name: CharSequence) {
        val tmp = when (name) {
            "全部" -> "all"
            "新闻" -> "news"
            "论文" -> "paper"
            else -> name
        }
        NewsData.curTypeId = ALL_TYPE.indexOf(tmp)
        notifyDataSetChangedSafely()
    }

    inner class ViewHolder(itemView: View) :
        UltimateRecyclerviewViewHolder<Any>(itemView),
        View.OnClickListener {

        val layout: View = itemView
        val title: TextView = itemView.findViewById(R.id.text_view_title)
        val category: TextView = itemView.findViewById(R.id.text_view_category)
        val publishTime: TextView = itemView.findViewById(R.id.text_view_publish_time)
        val publisher: TextView = itemView.findViewById(R.id.text_view_publisher)

        init {
            val maxWidth = SCREEN_Width / 3 * 2
            title.width = maxWidth
            category.width = maxWidth
            publishTime.width = maxWidth
            publisher.width = maxWidth
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val selectedNews = NewsData.curNews[adapterPosition]
            NewsData.addReadNews(selectedNews)
            notifyDataSetChanged()
            val intent = Intent(activity, DetailsActivity::class.java)
            intent.putExtra("news", selectedNews)
            activity.startActivity(intent)
        }
    }

    override fun onBindHeaderViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val textView = viewHolder.itemView.findViewById<TextView>(R.id.stick_text)
        textView.text = generateHeaderId(position).toString()
    }

    override fun generateHeaderId(position: Int) = NewsData.curNews[position].title[0].toLong()

    override fun getAdapterItemCount(): Int = NewsData.curNews.size

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_header, parent, false)
        return object : RecyclerView.ViewHolder(v) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(v)
    }

    override fun newFooterHolder(view: View) = ViewHolder(view)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news = NewsData.curNews[position]
        with(holder) {
            if (news.read) {
                layout.setBackgroundColor(if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) Color.DKGRAY else Color.LTGRAY)
            } else {
                layout.background = null
            }
            title.text = news.title
            category.text = "类别：${news.type}"
            publishTime.text = "时间：${news.time}"
            val source = if (news.source != "") news.source else "无"
            publisher.text = "来源：${source}"
        }
    }

    override fun newHeaderHolder(view: View) = ViewHolder(view)
}