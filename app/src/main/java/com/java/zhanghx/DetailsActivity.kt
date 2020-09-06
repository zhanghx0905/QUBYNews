package com.java.zhanghx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(findViewById(R.id.newsToolbar))


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.news_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}