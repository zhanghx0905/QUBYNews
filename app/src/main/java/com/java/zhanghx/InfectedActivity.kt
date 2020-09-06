package com.java.zhanghx

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_infected.*
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToLong

// Store Covid-19 data
data class InfectedData(
    val begDate: String = "",
    val confirmed: ArrayList<Int> = ArrayList(),
    val cured: ArrayList<Int> = ArrayList(),
    val dead: ArrayList<Int> = ArrayList()
)

data class Province(
    var infectedData: InfectedData = InfectedData(),
    val cities: TreeMap<String, InfectedData> = TreeMap()
)

data class Country(
    var infectedData: InfectedData = InfectedData(),
    val provinces: TreeMap<String, Province> = TreeMap()
)

private val countries = TreeMap<String, Country>()
private const val url = "https://covid-dashboard.aminer.cn/api/dist/epidemic.json"


private fun processData(address: String, data: InfectedData) {
    val addressList = address.split('|')
    val country = countries[addressList[0]] ?: Country()
    when (addressList.size) {
        1 -> {
            country.infectedData = data
        }
        else -> {
            val province = country.provinces[addressList[1]] ?: Province()
            when (addressList.size) {
                2 -> {
                    province.infectedData = data
                }
                else -> province.cities[addressList[2]] = data
            }
            country.provinces[addressList[1]] = province
        }
    }
    countries[addressList[0]] = country
}

fun initInfectedData() {

    url.httpGet().responseString() { request, response, result ->
        val rawStr = result.get()
        val jsonObject = JSONObject(rawStr)
        for (address in jsonObject.keys()) {
            val dataObject = jsonObject.getJSONObject(address)
            val infectedDataArray = dataObject.getJSONArray("data")
            val infectedData = InfectedData(begDate = dataObject.getString("begin"))

            for (i in 0 until infectedDataArray.length()) {
                val tmp = infectedDataArray.getJSONArray(i)
                infectedData.confirmed.add(tmp.getInt(0))
                infectedData.cured.add(tmp.getInt(2))
                infectedData.dead.add(tmp.getInt(3))
            }
            processData(address, infectedData)
        }
    }
}


// 界面类
class InfectedActivity : AppCompatActivity() {

    lateinit var countryAdapter: ArrayAdapter<String>
    lateinit var provinceAdapter: ArrayAdapter<String>
    lateinit var cityAdapter: ArrayAdapter<String>

    inner class CountrySelectListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            addpterView: AdapterView<*>?,
            View: View?,
            position: Int,
            id: Long
        ) {
            val country = countries[addpterView!!.getItemAtPosition(position).toString()]
            val data = country!!.provinces.keys.toMutableList()
            data.add(0, "All")
            provinceAdapter = ArrayAdapter(
                this@InfectedActivity,
                android.R.layout.simple_spinner_dropdown_item,
                data
            )
            spinnerState.adapter = provinceAdapter
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    inner class ProvinceSelectListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            addpterView: AdapterView<*>?,
            View: View?,
            position: Int,
            id: Long
        ) {
            val country = countries[spinnerCountry.selectedItem.toString()]
            val province = country!!.provinces[addpterView!!.getItemAtPosition(position).toString()]

            val data = if (province != null) {
                val tmp = province.cities.keys.toMutableList()
                tmp.add(0, "All")
                tmp
            } else mutableListOf("All")
            cityAdapter = ArrayAdapter(
                this@InfectedActivity,
                android.R.layout.simple_spinner_dropdown_item,
                data
            )
            spinnerCity.adapter = cityAdapter
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    private fun getData(country: String, province: String?, city: String?): InfectedData {
        val c = countries[country] as Country
        if (province == null || province == "All")
            return c.infectedData
        val p = c.provinces[province] as Province
        if (city == null || city == "All")
            return p.infectedData
        return p.cities[city] as InfectedData
    }

    inner class XFormatter(private val begData: String) : ValueFormatter() {
        private val inFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val outFormatter = DateTimeFormatter.ofPattern("MM-dd")
        override fun getFormattedValue(value: Float): String {
            val date = LocalDate.parse(begData, inFormatter)

            return outFormatter.format(date.plusDays(value.roundToLong())) as String
        }
    }

    private fun setChart(dataList: ArrayList<Int>, begDate: String) {
        if (dataList.size == 0) {
            Toast.makeText(this, "无数据", Toast.LENGTH_SHORT).show()
            lineChart.visibility = View.GONE
        }
        val entries: ArrayList<Entry> = ArrayList()
        for (i in 0 until dataList.size) {
            entries.add(Entry(i.toFloat(), dataList[i].toFloat()))
        }
        val dataSet = LineDataSet(entries, "Label") // add entries to dataset
        dataSet.color = Color.parseColor("#AABCC6") //线条颜色
        dataSet.setDrawValues(false) // 设置是否显示数据点的值
        dataSet.setDrawCircleHole(false) // 设置数据点是空心还是实心，默认空心
        dataSet.setCircleColor(Color.parseColor("#AABCC6")) // 设置数据点的颜色
        dataSet.circleSize = 1F // 设置数据点的大小
        dataSet.highLightColor = Color.parseColor("#AABCC6") // 设置点击时高亮的点的颜色
        dataSet.lineWidth = 3f //线条宽度


        val rightAxis: YAxis = lineChart.axisRight
        rightAxis.isEnabled = false
        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.isEnabled = true
        leftAxis.textColor = Color.parseColor("#333333")

        //设置x轴
        val xAxis: XAxis = lineChart.getXAxis()
        xAxis.textColor = Color.parseColor("#333333")
        xAxis.textSize = 11f
        xAxis.axisMinimum = 0f
        xAxis.setDrawAxisLine(false) //是否绘制轴线
        xAxis.setDrawGridLines(false) //设置x轴上每个点对应的线
        xAxis.setDrawLabels(true) //绘制标签  指x轴上的对应数值
        xAxis.position = XAxis.XAxisPosition.BOTTOM //设置x轴的显示位置
        xAxis.granularity = 1f //禁止放大后x轴标签重绘
        xAxis.valueFormatter = XFormatter(begDate)  // 设置格式化
        //透明化图例
        val legend: Legend = lineChart.legend
        legend.form = Legend.LegendForm.NONE
        legend.textColor = Color.WHITE

        //隐藏x轴描述
        val description = Description()
        description.isEnabled = false
        lineChart.description = description

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // refresh
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infected)
        supportActionBar?.let {
            it.title = "实时疫情数据"
        }
        countryAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            countries.keys.toMutableList()
        )
        spinnerCountry.adapter = countryAdapter
        spinnerCountry.onItemSelectedListener = CountrySelectListener()
        spinnerState.onItemSelectedListener = ProvinceSelectListener()
        infoButton.setOnClickListener {
            val country = spinnerCountry.selectedItem.toString()
            val province = spinnerState.selectedItem?.toString()
            val city = spinnerCity.selectedItem?.toString()
            val data = getData(country, province, city)
            val dataList = when (spinnerType.selectedItem.toString()) {
                "感染" -> data.confirmed
                "死亡" -> data.dead
                else -> data.cured
            }
            setChart(dataList, data.begDate)
        }

    }
}