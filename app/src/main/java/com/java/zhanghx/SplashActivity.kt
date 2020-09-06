package com.java.zhanghx

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.properties.Delegates

@SuppressLint("StaticFieldLeak")
lateinit var GLOBAL_CONTEXT: Context
var SCREEN_Width by Delegates.notNull<Int>()

// 载入界面，用于初始化数据和权限
class SplashActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE =
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        GLOBAL_CONTEXT = applicationContext

        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }
        val outMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(outMetrics)
        SCREEN_Width = outMetrics.widthPixels
        initGlobals()
        val dstIntent = Intent(this, ListActivity::class.java)
        startActivity(dstIntent)
        finish()
    }

}

fun initGlobals() {
    initInfectedData()
    initScholarsData()
}