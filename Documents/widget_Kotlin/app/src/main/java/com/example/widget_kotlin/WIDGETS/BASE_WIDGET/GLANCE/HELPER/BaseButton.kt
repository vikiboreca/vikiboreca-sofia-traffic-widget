package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.BusEntry
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.TypeAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.Base_Glance
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

class BaseButton : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            val map = getMap(context,"0603")
            saveListMemory(context, map, glanceId)
            Base_Glance().update(context, glanceId)
        } catch (e: Exception) {
            Log.d("widget error", e.toString())
        }
    }

    private fun ReceiveData(stopID: String, call: Callback) {
        val client = OkHttpClient()
        val url = "http://100.114.8.24:8080/api/scrap"
        val jsonBody = "{\"stop\":\"$stopID\"}".trimIndent()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(call)
    }

    private suspend fun getMap(context: Context,stopID: String): LinkedHashMap<Bus, ArrayList<ArriveTime>> =
        suspendCancellableCoroutine { cont ->
            ReceiveData(stopID, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("fuck ass error", e.toString())
                    cont.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val value = response.body.string()
                        val gsonBuilder = GsonBuilder()
                        val gson = gsonBuilder.create()
                        val listType = object : TypeToken<List<Bus>>() {}.type
                        val buses: List<Bus> = gson.fromJson(value, listType)
                        val busMap: LinkedHashMap<Bus, ArrayList<ArriveTime>> = LinkedHashMap()

                        val assetManager = context.assets
                        val typesJson = assetManager.open("types.json").bufferedReader().use{it.readText()}

                        for (bus in buses) {
                            val map = getIsLastStationMap(assetManager, typesJson, bus)
                            if (!busMap.containsKey(bus)) {
                                val arriveTimes: ArrayList<ArriveTime> = ArrayList()
                                val isLast = isLastStation(bus, map)
                                for (time in bus.arriveTimes) {
                                    val arr = ArriveTime(time, isLast, bus.lastStop)
                                    arriveTimes.add(arr)
                                }
                                if(isLast) arriveTimes.forEach { it-> it.realLastStation = bus.lastStop }
                                busMap[bus] = arriveTimes
                            } else {
                                val original: ArrayList<ArriveTime>? = busMap[bus]
                                val result = original?.firstOrNull{ it.isLastStation }
                                val isLast = isLastStation(bus, map)
                                for (time in bus.arriveTimes) {
                                    val arr = ArriveTime(time, isLast, bus.lastStop)
                                    if(result!=null) arr.realLastStation = result.realLastStation
                                    original?.add(arr)
                                }
                                if(result==null && isLast) original?.forEach{ it->it.realLastStation = bus.lastStop}
                                original?.sortBy(ArriveTime::minutes)
                            }
                        }
                        cont.resumeWith(Result.success(busMap))
                    } else {
                        cont.resumeWith(Result.failure(Exception("Empty body")))
                    }
                }
            })
        }

    private fun saveListMemory(context: Context, map: Map<Bus, ArrayList<ArriveTime>>, glanceId: GlanceId) {
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val list = map.map { (bus, arrivals) -> BusEntry(bus, arrivals) }
        prefs.edit { putString("bus_list$glanceId", gson.toJson(list)).apply() }
    }

    private fun isLastStation(bus:Bus, map:Map<String, List<String>>):Boolean{
        val stationA = normalizeString(bus.lastStop)
        var check = ""
        map[bus.name]?.forEach {
            val stationB = normalizeString(it)
            check += "$stationB "
            if(stationA == stationB) return true
            if(stationA.contains(stationB)) return true
            if(stationB.contains(stationA)) return true
        }
        Log.d("check", "$stationA $check")
        return false
    }


    private fun getIsLastStationMap(assetManager: AssetManager,vehicleTypes:String,bus:Bus):Map<String, List<String>>{
        val gson = GsonBuilder().create()
        val data: TypeAdvanced = gson.fromJson(vehicleTypes, object : TypeToken<TypeAdvanced>() {}.type)
        val typeList = data.types
        val typeName = typeList[bus.type-1].name

        val listText = assetManager.open("$typeName.json").bufferedReader().use{it.readText()}
        val listType = object : TypeToken<StationAdvanced>() {}.type
        val advanced: StationAdvanced = gson.fromJson(listText, listType)
        val stationsList = advanced.lines
        return stationsList.associate { it.name to it.stops }
    }

    private fun normalizeString(word:String):String{
        return word.lowercase().replace(Regex("[,.?\"„“:()-]"), "")
            .replace("\\s+".toRegex(), "").trim()
    }
}