package com.java.zhanghx

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_search.*
import java.util.*


data class Query(
    val size: Int? = null,
    val words: String? = null
)

class SearchActivity : Activity() {

    var listItems = ArrayList<String>()
    val searchHistorySharedPreference = "searchHistory"
    var MAX_HISTORY = 7

    lateinit var adapter: ArrayAdapter<String>

    inner class QueryTextListener : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(queryStr: String): Boolean {
            var notInHistory = true
            for (str in listItems) {
                if (str == queryStr) {
                    notInHistory = false
                    break
                }
            }
            if (notInHistory) {
                listItems.add(0, queryStr)
                if (listItems.size > MAX_HISTORY)
                    listItems.removeAt(MAX_HISTORY)
            }
            adapter.notifyDataSetChanged()
            val query = Query(10, queryStr)
            return false
            //TODO: 与NewsAdapter协同完成查询
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            // perform the default action of showing any suggestions if available
            return false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, listItems)
        searchHistory.adapter = adapter
        // 恢复历史记录
        val his = getSharedPreferences(searchHistorySharedPreference, Context.MODE_PRIVATE)
        var index: Int = 0
        while (index < MAX_HISTORY) {
            if (his.getString(index.toString(), "") != "") {
                his.getString(index.toString(), "")?.let { listItems.add(it) }
            }
            index += 1
        }

        searchHistory.setOnItemClickListener { adapterView, view, position, id ->
            val historyQuery = adapterView.getItemAtPosition(position) as String
            searchNews.setQuery(historyQuery, false)
        }
        searchNews.setOnQueryTextListener(QueryTextListener())
    }

    // 退出时存储历史数据
    override fun onDestroy() {
        val editor =
            getSharedPreferences(searchHistorySharedPreference, Context.MODE_PRIVATE).edit()
        var index: Int = 0
        while (index < listItems.size) {
            editor.putString(index.toString(), listItems[index])
            index += 1
        }
        editor.commit()
        super.onDestroy()
    }
}