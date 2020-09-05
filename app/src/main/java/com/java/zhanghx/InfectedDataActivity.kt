package com.java.zhanghx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


// Store Covid-19 data
data class InfectedData(
    val begDate: LocalDate? = null,
    val confirmed: ArrayList<Int> = ArrayList(),
    val cured: ArrayList<Int> = ArrayList(),
    val dead: ArrayList<Int> = ArrayList()
)

data class Province(
    var infectedData: InfectedData = InfectedData(),
    val cities: HashMap<String, InfectedData> = HashMap()
)

data class Country(
    var infectedData: InfectedData = InfectedData(),
    val provinces: HashMap<String, Province> = HashMap()
)

class InfectedDataActivity : AppCompatActivity() {
    private val countries = HashMap<String, Country>()
    private val url = "https://covid-dashboard.aminer.cn/api/dist/epidemic.json"


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

    private fun initData() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        url.httpGet().responseString() { request, response, result ->
            println(result)

            val rawStr = result.get()
            val jsonObject = JSONObject(rawStr)
            for (address in jsonObject.keys()) {
                val dataObject = jsonObject.getJSONObject(address)
                val date = LocalDate.parse(dataObject.getString("begin"), formatter)
                val infectedDataArray = dataObject.getJSONArray("data")
                val infectedData = InfectedData(begDate = date)

                for (i in 0 until infectedDataArray.length()) {
                    val tmp = infectedDataArray.getJSONArray(i)
                    infectedData.confirmed.add(tmp.getInt(0))
                    infectedData.cured.add(tmp.getInt(2))
                    infectedData.dead.add(tmp.getInt(3))
                }
                // 统一转换成小写存储
                processData(address.toLowerCase(Locale.getDefault()), infectedData)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infected_data)

        initData()


    }
}