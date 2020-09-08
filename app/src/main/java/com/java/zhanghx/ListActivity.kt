package com.java.zhanghx

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_list.*
import kotlin.properties.Delegates


@SuppressLint("StaticFieldLeak")
lateinit var GLOBAL_CONTEXT: Context
var SCREEN_WIDTH by Delegates.notNull<Int>()


fun initGlobalNetRes() {
    initInfectedData()
    initScholarsData()
}


class ListActivity : AppCompatActivity() {
    private var removedType = ArrayList<Pair<Int, CharSequence>>()
    private var existType = ArrayList<Pair<Int, CharSequence>>()

    private var checkedKind = R.id.navKind0
    private var checkedType = R.id.navType0

    lateinit var kindMenu: Menu
    lateinit var typeMenu: Menu

    private lateinit var newsAdapter: NewsAdapter

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
        setContentView(R.layout.activity_list)

        GLOBAL_CONTEXT = applicationContext
        val outMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(outMetrics)
        SCREEN_WIDTH = outMetrics.widthPixels
        initGlobalNetRes()
        initEventsData()
        NewsData.loadFromFile()

        // 获得权限
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }

        setSupportActionBar(newsToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
        setNavigationView()
        searchButton.setOnClickListener {
            startActivityForResult(Intent(this, SearchActivity::class.java), 42)
        }
        newsList.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(this, newsList)


        val drawerToggle =
            ActionBarDrawerToggle(
                this,
                drawer_layout,
                newsToolbar,
                R.string.drawer_open,
                R.string.drawer_close
            )
        drawerToggle.syncState()
        updateTitle()
    }

    override fun onResume() {
        newsAdapter.notifyDataSetChangedSafely()
        updateTitle()
        super.onResume()
    }

    private fun addType() {
        if (removedType.isEmpty())
            Toast.makeText(this, "没有可添加类别！", Toast.LENGTH_SHORT).show()
        else {
            val builder = AlertDialog.Builder(this)
            val spinner = Spinner(this)
            spinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                removedType.map { (_, title) -> title })
            builder.setView(spinner) // 确定: 添加类别
                .setPositiveButton("确定") { _, _ ->
                    val title = spinner.selectedItem.toString()
                    val idx = removedType.indexOfFirst { it.second == title }
                    navView.menu.findItem(removedType[idx].first).isVisible = true
                    existType.add(removedType[idx])
                    removedType.removeAt(idx)
                } // 取消: 什么也不做
                .setNegativeButton("取消") { _, _ -> }
            val dialog = builder.create()
            dialog.window?.setWindowAnimations(R.style.dialog_style)
            dialog.show()
        }
    }

    private fun removeType() {
        if (existType.isEmpty())
            Toast.makeText(this, "没有可删除类别！", Toast.LENGTH_SHORT).show()
        else {
            val builder = AlertDialog.Builder(this)
            val spinner = Spinner(this)
            spinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                existType.map { (_, title) -> title })
            builder.setView(spinner) // 确定: 添加类别
                .setPositiveButton("确定") { _, _ ->
                    val title = spinner.selectedItem.toString()
                    val idx = existType.indexOfFirst { it.second == title }
                    navView.menu.findItem(existType[idx].first).isVisible = false
                    removedType.add(existType[idx])
                    existType.removeAt(idx)
                } // 取消: 什么也不做
                .setNegativeButton("取消") { _, _ -> }
            val dialog = builder.create()
            dialog.window?.setWindowAnimations(R.style.dialog_style)
            dialog.show()
        }
    }

    private fun updateTitle() {
        newsToolbar.title =
            "${kindMenu.findItem(checkedKind).title}：${typeMenu.findItem(checkedType).title}"
    }

    // 设置侧边栏
    private fun setNavigationView() {
        val menu = navView.menu
        kindMenu = menu.getItem(0).subMenu
        typeMenu = menu.getItem(1).subMenu
        kindMenu.getItem(0).isChecked = true
        typeMenu.getItem(0).isChecked = true
        existType.add(Pair(R.id.navType1, typeMenu.getItem(1).title))
        existType.add(Pair(R.id.navType2, typeMenu.getItem(2).title))

        // 设置侧边栏菜单选项点击事件
        navView.setNavigationItemSelectedListener {
            when (it.groupId) {
                R.id.navKind -> {
                    newsAdapter.setCurKind(it.title)
                    kindMenu.findItem(checkedKind).isChecked = false
                    checkedKind = it.itemId
                    it.isChecked = true
                }
                R.id.navType -> {
                    if (it.itemId == R.id.addType) {  // 增加类别
                        addType()
                    } else if (it.itemId == R.id.removeType) {  // 删除类别
                        removeType()
                    } else { // 单击选择新闻类别
                        newsAdapter.setCurType(it.title)
                        newsToolbar.title = it.title
                        typeMenu.findItem(checkedType).isChecked = false
                        checkedType = it.itemId
                        it.isChecked = true
                    }

                }
                R.id.navOthers -> {
                    when (it.itemId) {
                        R.id.navData -> startActivity(Intent(this, InfectedActivity::class.java))
                        R.id.navEvents ->
                            startActivityForResult(Intent(this, EventActivity::class.java), 21)
                        else -> {
                            val intent = Intent(this, ScholarActivity::class.java)
                            val dead = (it.itemId != R.id.navScholar)
                            intent.putExtra("dead", dead)
                            startActivity(intent)
                        }
                    }
                }
            }
            updateTitle()
            true
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42 && data != null) {
            val keywords = data.getStringExtra("keywords")
            val toKG = data.getBooleanExtra("toKG", false)
            if (toKG) {  // 调用知识图谱接口
                val kgIntent = Intent(this, KGActivity::class.java)
                kgIntent.putExtra("keywords", keywords)
                startActivity(kgIntent)
            } else {
                kindMenu.findItem(checkedKind).isChecked = false
                checkedKind = R.id.navKind2
                kindMenu.findItem(checkedKind).isChecked = true
                newsAdapter.doSearch(keywords as String)
                updateTitle()
            }
            return
        }
        else if (requestCode == 21 && data != null) {
            val eventsList = data.getParcelableArrayListExtra<News>("events")
            eventsList?.let { newsAdapter.setEvents(it) }
            kindMenu.findItem(checkedKind).isChecked = false
            checkedKind = R.id.navKind0
            kindMenu.findItem(checkedKind).isChecked = true
            typeMenu.findItem(checkedType).isChecked = false
            checkedType = R.id.navType3
            typeMenu.findItem(checkedType).isChecked = true
            updateTitle()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}