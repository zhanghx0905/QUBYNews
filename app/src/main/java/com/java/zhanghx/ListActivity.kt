package com.java.zhanghx

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_list.*


class ListActivity : AppCompatActivity() {
    // removed info type (id, title)
    private var removedType = ArrayList<Pair<Int, CharSequence>>()

    private var checkedKind = R.id.navKind0
    private var checkedType = R.id.navType0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
        searchButton.setOnClickListener {
            Toast.makeText(this, "搜索还没实现！", Toast.LENGTH_SHORT).show()
        }
        newsList.layoutManager = LinearLayoutManager(this)
        setNavigationView()

        val drawerToggle =
            ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
            )
        drawerToggle.syncState()
    }

    // 添加下拉菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 控制下拉菜单
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addType -> {
                if (removedType.isEmpty())
                    Toast.makeText(this, "没有可添加类别！", Toast.LENGTH_SHORT).show()
                else {
                    val builder = AlertDialog.Builder(this)
                    val spinner = Spinner(this)
                    spinner.adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        removedType.map { (id, title) -> title })
                    builder.setView(spinner) // 确定: 添加类别
                        .setPositiveButton("确定") { _, _ ->
                            val title = spinner.selectedItem.toString()
                            val idx = removedType.indexOfFirst { it.second == title }
                            navView.menu.findItem(removedType[idx].first).isVisible = true
                            removedType.removeAt(idx)
                        } // 取消: 什么也不做
                        .setNegativeButton("取消") { _, _ -> }
                    builder.create().show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 设置侧边栏
    private fun setNavigationView() {
        val menu = navView.menu
        val kindMenu = menu.getItem(0).subMenu
        val typeMenu = menu.getItem(1).subMenu

        kindMenu.getItem(0).isChecked = true
        typeMenu.getItem(0).let {
            it.isChecked = true
            toolbar.title = it.title
        }
        var lastClick = 0L
        // 设置侧边栏菜单选项点击事件
        navView.setNavigationItemSelectedListener {
            val cur = System.currentTimeMillis()
            val isDoubleClick = (cur - lastClick) < 500L
            lastClick = cur
            when (it.groupId) {
                R.id.navKind -> {
                    // newsAdapter.setCurKind(it.title)
                    kindMenu.findItem(checkedKind).isChecked = false
                    checkedKind = it.itemId
                }
                R.id.navType -> {
                    if (isDoubleClick) {  // 双击删除类别
                        val allString = "全部"
                        if (it.title != allString) {
                            it.isVisible = false
                            removedType.add(Pair(it.itemId, it.title))
                            if (it.isChecked) {
                                // newsAdapter.setCurCategory(allString)
                                toolbar.title = allString
                                it.isChecked = false
                                checkedType = R.id.navType0
                            }
                        }
                    } else {
                        // newsAdapter.setCurCategory(it.title)
                        toolbar.title = it.title
                        typeMenu.findItem(checkedType).isChecked = false
                        checkedType = it.itemId
                    }
                }
            }
            it.isChecked = true
            true
        }
    }

    override fun onResume() {
        // newsAdapter.notifyDataSetChanged()
        super.onResume()
    }

    private fun doSearch(query: String) {
        // newsAdapter.doSearch(query)
        val kindMenu = navView.menu.getItem(0).subMenu
        kindMenu.findItem(checkedKind).isChecked = false
        checkedKind = R.id.navKind2
        kindMenu.findItem(checkedKind).isChecked = true // 搜索结果一栏
    }
}