package com.java.zhanghx

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {
    lateinit var news: News

    private fun setView() {
        news_title.text = news.title
        news_content.text = news.content.toArticle()
        news_category.text = "类别：${news.type}"
        news_publish_time.text = "时间：${news.time}"
        val source = if (news.source != "") news.source else "无"
        news_publisher.text = "来源：${source}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        news = intent.getParcelableExtra<News>("news") as News
        supportActionBar?.title = "详情"
        setView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.news_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shareQQ -> {
                val mIntent = Intent(Intent.ACTION_SEND)
                mIntent.setPackage("com.tencent.mobileqq")
                mIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "标题：\n" + news.title + "\n" + "正文：\n" + news.content + "..."
                )
                mIntent.type = "text/plain"
                startActivity(Intent.createChooser(mIntent, "分享文本"))
            }
            R.id.shareWeibo -> {
                val mIntent = Intent(Intent.ACTION_SEND)
                mIntent.setPackage("com.sina.weibo")
                mIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "标题：\n" + news.title + "\n" + "正文：\n" + news.content + "..."
                )
                mIntent.type = "text/plain"
                startActivity(Intent.createChooser(mIntent, "分享至微博"))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}