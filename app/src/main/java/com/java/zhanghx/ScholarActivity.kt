package com.java.zhanghx

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_scholar.*
import org.json.JSONObject
import kotlin.properties.Delegates

@Parcelize
data class ScholarIndices(
    val hindex: Int,
    val activity: Double,
    val newStar: Double,
    val citations: Int,
    val pubs: Int
) : Parcelable

@Parcelize
data class Scholar(
    val name: String,
    val img_url: String,
    val affiliation: String,
    val bio: String,
    val passedAway: Boolean,
    val indices: ScholarIndices
) : Parcelable

private val scholars = ArrayList<Scholar>()
private val deadScholars = ArrayList<Scholar>()

private val BASE_URL =
    "https://innovaapi.aminer.cn/predictor/api/v1/valhalla/highlight/get_ncov_expers_list?v=2"
private val pattern = Regex("(<br>)+")

fun initScholarsData() {
    BASE_URL.httpGet().responseString() { request, response, result ->
        val rawStr = result.get()
        val json = JSONObject(rawStr)
        val dataArray = json.getJSONArray("data")
        for (i in 0 until dataArray.length()) {
            val data = dataArray.getJSONObject(i)
            val indicesData = data.getJSONObject("indices")
            val indices = ScholarIndices(
                indicesData.getInt("hindex"),
                indicesData.getDouble("activity"),
                indicesData.getDouble("newStar"),
                indicesData.getInt("citations"),
                indicesData.getInt("pubs")
            )
            val profile = data.getJSONObject("profile")
            val tmp = data.getString("name_zh")
            val name =
                if (tmp != "") tmp
                else data.getString("name")
            val bio = profile.getString("bio")
            val scholar = Scholar(
                name,
                data.getString("avatar"),
                profile.getString("affiliation"),
                pattern.replace(bio, "\n"),
                data.getBoolean("is_passedaway"),
                indices
            )
            if (scholar.passedAway)
                deadScholars.add(scholar)
            else scholars.add(scholar)
        }
    }
}


// TODO: 学者界面
class ScholarActivity : AppCompatActivity() {
    var dead by Delegates.notNull<Boolean>()

    inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(
            addpterView: AdapterView<*>?,
            View: View?,
            position: Int,
            id: Long
        ) {
            val tmpScholars = if (dead) deadScholars else scholars
            val scholar = tmpScholars[position]
            val intent = Intent(this@ScholarActivity, ScholarDetailsActivity::class.java)
            intent.putExtra("data", scholar)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scholar)

        dead = intent.getBooleanExtra("dead", false)
        supportActionBar?.title = if (dead) "追忆学者" else "知疫学者"

        val listData: ArrayList<HashMap<String, Any>> = ArrayList()

        val tmpScholars = if (dead) deadScholars else scholars
        tmpScholars.forEach {
            val tmp = HashMap<String, Any>()
            tmp["name"] = it.name
            tmp["info"] = it.affiliation
            listData.add(tmp)
        }
        val form = arrayOf("name", "info")
        val to = intArrayOf(R.id.nameTextView, R.id.infoTextView)
        val adapter = SimpleAdapter(this, listData, R.layout.scholar_item, form, to)

        listView.adapter = adapter
        listView.onItemClickListener = ListItemClickListener()
    }
}