package com.java.zhanghx

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_event.*
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


private val EVENTS_FILE = "events.json"
private const val N_CLUSTERS = 25
private val EventsClusters = Array(N_CLUSTERS) { ArrayList<News>() }
private val Keywords = Array(N_CLUSTERS) { "" }


fun initEventsData() {
    var rawData = ""
    try {
        val input = GLOBAL_CONTEXT.assets.open(EVENTS_FILE)
        rawData = convertStreamToString(input) as String
    } catch (e: IOException) {
        e.printStackTrace();
    }
    val json = JSONArray(rawData)
    for (i in 0 until json.length()) {
        val newsObj = json.getJSONObject(i)
        val news = News(
            id = newsObj.getString("id"),
            content = "", source = "",
            title = newsObj.getString("title"),
            time = newsObj.getString("time"),
            type = "event"
        )
        val predict = newsObj.getInt("predict")
        EventsClusters[predict].add(news)
        if (Keywords[predict] == "")
            Keywords[predict] = "关键词：${newsObj.getString("keywords")}"
    }
}


private fun convertStreamToString(input: InputStream): String? {
    var s: String? = null
    try {
        //格式转换
        val scanner: Scanner = Scanner(input, "UTF-8").useDelimiter("\\A")
        if (scanner.hasNext()) {
            s = scanner.next()
        }
        input.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return s
}


class EventActivity : AppCompatActivity() {
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        supportActionBar?.title = "热点事件"

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, Keywords)
        eventListView.adapter = adapter
        eventListView.setOnItemClickListener { addpterView, View, position: Int, id: Long ->
            val intent = Intent()
            intent.putParcelableArrayListExtra("events", EventsClusters[position])
            setResult(21, intent)
            finish()
        }
    }
}