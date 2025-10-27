package BACKEND.Rest

import BACKEND.DATA.FullBus
import BACKEND.Service.ScrapperService
import android.util.Log
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScrapperController {
    suspend fun getData(id:String):ArrayList<Bus> {
        val scrapper = ScrapperService()
        val rawJson = scrapper.getRawData(id)
        val busMap = getBusMap(rawJson)
        return getBusList(busMap)
    }

    fun getBusMap(json:String):Map<String, FullBus>{
        val listType = object : TypeToken<Map<String, FullBus>>() {}.type
        return Gson().fromJson(json, listType)
    }
    private fun getBusList(map: Map<String, FullBus>): ArrayList<Bus> {
        val list = ArrayList<Bus>()
        map.values.forEach { value ->
            val simpleBus = value.toSimpleBus()
            Log.d("fuck", simpleBus.toString())
            val index = list.indexOf(simpleBus)
            if (index == -1) {
                list.add(simpleBus)
            } else {
                val oldBus = list[index]
                oldBus.arriveTimes.apply {
                    addAll(simpleBus.arriveTimes)
                    val cleaned = distinct().sorted()
                    clear()
                    addAll(cleaned)
                }
            }
        }

        val result: ArrayList<Bus> = map.values
            .map { it.toSimpleBus() }
            .groupBy { it.type }
            .toSortedMap()
            .flatMap { (_, group) ->
                group.sortedWith(compareBy { it.name.toIntOrNull() ?: Int.MAX_VALUE })
            }
            .toCollection(ArrayList())
        return result
    }


}
