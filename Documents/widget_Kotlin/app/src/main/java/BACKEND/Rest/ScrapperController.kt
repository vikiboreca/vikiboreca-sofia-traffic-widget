package BACKEND.Rest

import BACKEND.DATA.Extra.ExtraStation
import BACKEND.DATA.FullBus
import BACKEND.Service.CoordinateService
import BACKEND.Service.ExtraScrapperService
import BACKEND.Service.NormalScrapperService
import android.content.Context
import android.util.Log
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScrapperController {
    suspend fun getData(id:String):ArrayList<Bus> {
        val scrapper = NormalScrapperService()
        val rawJson = scrapper.getRawData(id)
        val busMap = getBusMap(rawJson)
        return getBusList(busMap)
    }

    suspend fun getData(id:String, limit:Int): ExtraStation? {
        val scrapper = ExtraScrapperService(id, limit)
        try{
            val rawJson = scrapper.getJson()
            val type = object: TypeToken<ExtraStation>(){}.type
            Log.d("fuck2", "extraSuccess")
            return Gson().fromJson(rawJson, type)
        }catch (e:Exception){
            Log.d("fuck2", e.toString())
            return null;
        }
    }
    suspend fun getBusCoordinates(busID:String):Pair<Float, Float>{
        val scrapper = CoordinateService()
        return scrapper.getCoordinates(busID)
    }

    private fun getBusMap(json:String):Map<String, FullBus>{
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

    suspend fun isIDValid(id:String):Boolean{
        val normalScrapperService = NormalScrapperService()
        return normalScrapperService.isIDValid(id)
    }

    suspend fun getPrevStopsCoordinates(context: Context,tripID:String, stopID:String):ArrayList<LatLng>{
        val scrapper = CoordinateService()
        val base = BaseButton()
        val stopList = scrapper.getPrevStops(stopID)
        Log.d("fuck2", "${stopList.size}")
        val list:ArrayList<LatLng> = ArrayList()
        stopList.forEach { it->
            val pair = base.getCoordinates(context, it)
            list.add(LatLng(pair?.first ?: 0.0, pair?.second ?: 0.0))
        }
        return list
    }
}
