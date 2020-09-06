package com.java.zhanghx

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.map
import kotlinx.android.synthetic.main.activity_kg.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject

private val BASE_URL = "https://innovaapi.aminer.cn/covid/api/v1/pneumonia/entityquery?entity="

data class Relation(
    val relation: String,
    val label: String,
    val forward: Boolean
)

data class Entity(
    var label: String = "",
    var info: String = "",
    val Properties: ArrayList<String> = ArrayList(),
    val Relations: ArrayList<Relation> = ArrayList(),
    var img_url: String = ""
)

class KGActivity : AppCompatActivity() {
    private val entity = Entity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kg)
        supportActionBar?.title = "知疫图谱"
        val keywords = intent.getStringExtra("keywords")
        keywords?.let { initData(it) }
    }

    private fun initData(keywords: String) {
        val url = BASE_URL + keywords
        url.httpGet().responseString() { request, response, result ->
            val rawStr = result.get()
            val jsonObject = JSONObject(rawStr)
            val dataArray = jsonObject.getJSONArray("data")
            val entities = ArrayList<String>()
            for (i in 0 until dataArray.length()) {
                entities.add(dataArray.getJSONObject(i).getString("label"))
            }
            if (entities.isEmpty()) {
                Toast.makeText(this, "没有相关结果！", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val builder = AlertDialog.Builder(this)
                val spinner = Spinner(this)
                spinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    entities
                )
                builder.setTitle("请选择您想查询的实体:")
                builder.setView(spinner)  // 主要工作都在这个匿名函数内完成
                    .setPositiveButton("确定") { _, _ ->
                        val idx = spinner.selectedItemPosition
                        val infoJson = dataArray.getJSONObject(idx)
                        initEntity(infoJson)
                        supportActionBar?.title = entity.label
                        setView()
                    }
                builder.create().show()
            }
        }
    }

    private fun initEntity(infoJson: JSONObject) {
        entity.label = infoJson.getString("label")
        entity.img_url = infoJson.getString("img")
        val info = infoJson.getJSONObject("abstractInfo")
        entity.info = info.getString("enwiki") + info.getString("baidu") + info.getString("zhwiki")
        val covidInfo = info.getJSONObject("COVID")
        val properties = covidInfo.getJSONObject("properties")
        for (key in properties.keys()) {
            entity.Properties.add("$key: ${properties[key]}")
        }
        val relations = covidInfo.getJSONArray("relations")
        for (i in 0 until relations.length()) {
            val relation = relations.getJSONObject(i)
            val tmp = Relation(
                relation.getString("relation"),
                relation.getString("label"),
                relation.getBoolean("forward")
            )
            entity.Relations.add(tmp)
        }
    }

    private fun setView() {
        doAsync {
            entity.img_url.httpGet().response { _, _, result ->
                result.map {
                    val tmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                    imageView.setImageBitmap(tmp)
                    imageView.visibility = View.VISIBLE
                }
            }
        }
        infoTextView.text = entity.info.toArticle()
        propertyTextView.text = entity.Properties.joinToString("\n").toArticle()
        val relations = ArrayList<String>()
        for (relation in entity.Relations) {
            if (relation.forward)
                relations.add("${entity.label} --- ${relation.relation} --> ${relation.label}")
            else relations.add("${entity.label} <-- ${relation.relation} --- ${relation.label}")
        }
        relationTextView.text = relations.joinToString("\n")
    }
}