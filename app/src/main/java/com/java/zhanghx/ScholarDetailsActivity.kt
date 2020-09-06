package com.java.zhanghx

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.map
import kotlinx.android.synthetic.main.activity_scholar_details.*
import org.jetbrains.anko.doAsync

class ScholarDetailsActivity : AppCompatActivity() {

    private fun setView(data: Scholar) {
        doAsync {
            data.img_url.httpGet().response { _, _, result ->
                result.map {
                    val tmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                    imageView.setImageBitmap(scale(tmp))
                    imageView.visibility = View.VISIBLE
                }
            }
        }

        supportActionBar?.title = data.name
        afTextView.text = data.affiliation.toArticle()
        val indices = data.indices
        val s =
            "学术成就 H-index: ${indices.hindex}\n学术活跃度：${indices.activity}\n学术合作：${indices.newStar}\n引用数：${indices.citations}\n论文数：${indices.pubs}"
        inTextView.text = s.toArticle()
        bioTextView.text = data.bio.toArticle()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scholar_details)

        val data = intent.getParcelableExtra<Scholar>("data")
        data?.let { setView(it) }
    }
}