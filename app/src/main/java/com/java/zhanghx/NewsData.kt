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
const val EVENT_IDX = 3
val ALL_TYPE = arrayOf("all", "news", "paper", "event")

const val GET_COUNT = 40
const val URL = "https://covid-dashboard.aminer.cn/api/events/list?"

const val NEWS_FILENAME = "QUBYNews"

object NewsData {
    val allNews = Array(ALL_KIND.size) { Array(ALL_TYPE.size) { ArrayList<News>() } }
    val newsSet = HashSet<String>()
    private val readSet = HashSet<String>()
    private var keywords: MutableList<String>? = null   // for search load more/refresh

    var curKindId = LATEST_IDX
    var curTypeId = ALL_IDX
    val curNews
        inline get() = allNews[curKindId][curTypeId]
    var curPage = 1
    val canRefresh
        inline get() = (curKindId != READ_IDX && curTypeId != EVENT_IDX)


    inline fun refresh(
        crossinline errorHandler: (error: FuelError) -> Unit,
        crossinline finishHandler: () -> Unit
    ) { // 下拉
        if (canRefresh) {
            val url = URL + "size=$GET_COUNT&page=1&type=${ALL_TYPE[curTypeId]}"
            url.httpGet().responseString() { // 异步执行
                    _, _, result ->
                when (result) {
                    is Result.Failure -> errorHandler(result.error)
                    is Result.Success -> addNews(result.get())
                }
                finishHandler()
            }
        } else finishHandler()
    }

    inline fun loadMore(
        crossinline errorHandler: (error: FuelError) -> Unit,
        crossinline finishHandler: () -> Unit
    ) {
        curPage += 1
        val url = URL + "size=$GET_COUNT&page=$curPage&type=${ALL_TYPE[curTypeId]}"
        url.httpGet().responseString() { _, _, result ->
            when (result) {
                is Result.Failure -> errorHandler(result.error)
                is Result.Success -> addNews(result.get(), true)
            }
            finishHandler()
        }
    }

    inline fun parseNews(rawData: String): ArrayList<News> {
        val json = JSONObject(rawData)
        val data = json.getJSONArray("data")
        val newsList = ArrayList<News>()
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
            newsList.add(news)
        }
        return newsList
    }

    inline fun addNews(rawData: String, loadmore: Boolean = false) {
        val newsList = parseNews(rawData)
        for (news in newsList) {
            if (curKindId == SEARCH_IDX) {
                if (!searchFilter(news))
                    continue
            }
            val typeId = ALL_TYPE.indexOf(news.type)
            if (typeId != -1 && !newsSet.contains(news.id)) {
                if (!loadmore) {
                    allNews[curKindId][typeId].add(0, news)
                    allNews[curKindId][ALL_IDX].add(0, news)
                } else {
                    allNews[curKindId][typeId].add(news)
                    allNews[curKindId][ALL_IDX].add(news)
                }
                newsSet.add(news.id)

            }
        }
    }

    fun searchFilter(news: News): Boolean {
        val title = news.title.toLowerCase()
        keywords?.forEach {
            if (title.contains(it.toLowerCase()))
                return true
        }
        return false
    }

    fun doSearch(keyword: String) {
        keywords = keyword.split(' ').toMutableList()
        val result = ArrayList<News>()
        for (news in curNews) {
            if (searchFilter(news))
                result.add(news)
        }
        curKindId = SEARCH_IDX
        val searchNews = allNews[SEARCH_IDX]
        searchNews.forEach(ArrayList<News>::clear)
        result.forEach {
            val typeId = ALL_TYPE.indexOf(it.type)
            allNews[SEARCH_IDX][typeId].add(it)
            allNews[SEARCH_IDX][ALL_IDX].add(it)
        }
    }


    fun addReadNews(news: News) {
        news.read = true
        if (!readSet.contains(news.id)) {
            val typeId = ALL_TYPE.indexOf(news.type)
            allNews[READ_IDX][typeId].add(0, news)
            allNews[READ_IDX][ALL_IDX].add(0, news)
            readSet.add(news.id)
            storeToFile()
        }
    }

    fun setEvents(newsList: List<News>) {
        curKindId = LATEST_IDX
        curTypeId = EVENT_IDX
        allNews[curKindId][curTypeId].clear()
        newsList.forEach {
            allNews[curKindId][curTypeId].add(it)
            allNews[curKindId][ALL_IDX].add(it)
        }
    }

    private fun storeToFile() {
        doAsync {
            val parcel = Parcel.obtain()
            parcel.writeTypedList(allNews[READ_IDX][ALL_IDX])
            val fo = GLOBAL_CONTEXT.openFileOutput(NEWS_FILENAME, Context.MODE_PRIVATE)
            fo.write(parcel.marshall())
            parcel.recycle()
        }
    }

    fun loadFromFile() {
        try {
            val fi = GLOBAL_CONTEXT.openFileInput(NEWS_FILENAME)
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
        } catch (e: IOException) { // 初次使用，文件还不存在
            Log.e("initGlobals", e.toString())
        }
    }
}
