package com.java.zhanghx

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.parcel.Parcelize
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.io.IOException

@Parcelize
data class News(
    val id: String,
    val title: String,
    val content: String,
    val time: String,
    val source: String,
    val type: String,
    var read: Boolean = false
) : Parcelable


const val LATEST_IDX = 0
const val READ_IDX = 1
const val SEARCH_IDX = 2
val ALL_KIND = arrayOf("最新", "已读", "搜索结果")

const val ALL_IDX = 0
val ALL_TYPE = arrayOf("all", "news", "paper")

const val GET_COUNT = 20
val URL = "https://covid-dashboard.aminer.cn/api/events/list?size=$GET_COUNT&"

val NEWS_FILENAME = "QUBYNews"

object NewsData {
    val allNews = Array(ALL_KIND.size) { Array(ALL_TYPE.size) { ArrayList<News>() } }
    val newsSet = HashSet<String>()
    val readSet = HashSet<String>()

    var curKindId = LATEST_IDX
    var prevKindId = -1      // -1表示无效，即不存在prevKind
    var curTypeId = ALL_IDX
    val curNews
        inline get() = allNews[curKindId][curTypeId]
    var curPage = 1

    inline fun refresh(crossinline errorHandler: (error: FuelError) -> Unit) { // 下拉
        val url = URL + "page=1&type=${ALL_TYPE[curTypeId]}"
        url.httpGet().responseString() { request, response, result ->
            when (result) {
                is Result.Failure -> errorHandler(result.error)
                is Result.Success -> addNews(result.get())
            }
        }
    }

    inline fun loadMore(crossinline errorHandler: (error: FuelError) -> Unit) {
        curPage += 1
        val url = URL + "page=$curPage&type=${ALL_TYPE[curTypeId]}"
        url.httpGet().responseString() { request, response, result ->
            when (result) {
                is Result.Failure -> errorHandler(result.error)
                is Result.Success -> addNews(result.get(), true)
            }
        }
    }

    inline fun addNews(rawData: String, loadmore: Boolean = false) {
        val json = JSONObject(rawData)
        val data = json.getJSONArray("data")
        for (i in 0 until data.length()) {
            val newsData = data.getJSONObject(i)

            val news = News(
                id = newsData.getString("_id"),
                title = newsData.getString("title"),
                content = newsData.getString("content"),
                time = newsData.getString("time"),
                type = newsData.getString("type"),
                source = newsData.getString("source")
            )
            val typeId = ALL_TYPE.indexOf(news.type)
            if (!newsSet.contains(news.id) && typeId != -1) {
                if (!loadmore) {
                    allNews[LATEST_IDX][typeId].add(0, news)
                    allNews[LATEST_IDX][ALL_IDX].add(0, news)
                } else {
                    allNews[LATEST_IDX][typeId].add(news)
                    allNews[LATEST_IDX][ALL_IDX].add(news)
                }
                newsSet.add(news.id)

            }
        }
    }

    fun doSearch(keywords: String) {
        if (prevKindId == -1) {
            prevKindId = curKindId
        }
        val keywordSet = keywords.split(' ').toHashSet()
        val result = ArrayList<News>()
        for (news in curNews) {
            val title = news.title.toLowerCase()
            for (kw in keywordSet) {
                if (title.contains(kw.toLowerCase())) {
                    result.add(news)
                }
            }
        }
        setSearch(result)
    }

    fun setSearch(newsList: List<News>) {
        if (prevKindId == -1) {
            prevKindId = curKindId
        }
        println(newsList)
        curKindId = SEARCH_IDX
        val searchNews = allNews[SEARCH_IDX]
        searchNews.forEach(ArrayList<News>::clear)
        newsList.forEach {
            val typeId = ALL_TYPE.indexOf(it.type)
            allNews[SEARCH_IDX][typeId].add(it)
            allNews[SEARCH_IDX][ALL_IDX].add(it)
        }
    }

    fun addReadNews(news: News) {
        news.read = true
        if (!readSet.contains(news.id)) {
            val typeId = ALL_TYPE.indexOf(news.type)
            allNews[READ_IDX][typeId].add(news)
            allNews[READ_IDX][ALL_IDX].add(news)
            readSet.add(news.id)
            storeToFile()
        }
    }

    private fun storeToFile() {
        doAsync {
            val parcel = Parcel.obtain()
            parcel.writeTypedList(allNews[READ_IDX][ALL_IDX])
            val fo = GLOBAL_CONTEXT.openFileOutput("$NEWS_FILENAME", Context.MODE_PRIVATE)
            fo.write(parcel.marshall())
            parcel.recycle()
        }
    }

    fun loadFromFile() {
        try {
            val fi = GLOBAL_CONTEXT.openFileInput("$NEWS_FILENAME")
            val bytes = fi.readBytes()
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            parcel.readTypedList(allNews[READ_IDX][ALL_IDX], ParcelHelper.NEWS_CREATOR)
            parcel.recycle()
            allNews[READ_IDX].let {
                for (x in it[ALL_IDX]) {
                    it[ALL_TYPE.indexOf(x.type)].add(x)
                    readSet.add(x.id)
                }
            }
        } catch (e: IOException) { // 正常，应该是文件还不存在
            Log.e("initGlobals", e.toString())
        }
    }
}
