package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import BACKEND.Rest.ScrapperController
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.AssetManager
import android.util.Log
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.BusEntry
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.Filter
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.TypeAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF.BaseGlance
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var CurrentStationID:String = ""
    val updater = WidgetUpdater(BaseGlance::class.java)
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        getResults(context, glanceId, parameters)
    }
    private suspend fun getResults(context: Context, glanceId: GlanceId, parameters: ActionParameters){
        val currentPair = getCurrentStationPair(context)
        if(currentPair!=null){
            try {
                Log.d("fuck", "send ${currentPair.original}")
                val id = currentPair.current.ID
                saveCoordinates(context, id)
                val map = getMap(context,id)
                addVehicleID(map, id)
                saveListMemory(context, map, glanceId)
                updater.updateWidget(context)
            } catch (e: Exception) {
                Log.d("widget error", e.toString())
            }
        }
        else{
            Log.d("fuck", "fck you mean by null")
        }
    }

    suspend fun getResults(context: Context, glanceIdString: String){
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(BaseGlance::class.java)
        val currentPair = getCurrentStationPair(context)
        if(currentPair!=null){
            try {
                Log.d("fuck", "send ${currentPair.current}")
                val id = currentPair.current.ID
                saveCoordinates(context, id)
                val map = getMap(context,id)
                addVehicleID(map, id)
                for (id in glanceIds) {
                    if (id.toString() == glanceIdString) {
                        saveListMemory(context, map, id)
                    }
                }
                //updater.updateWidget(context)
            } catch (e: Exception) {
                Log.d("widget error", e.toString())
            }
        }
        else{
            Log.d("fuck", "fck you mean by null")
        }
    }

    suspend fun getTypes(context: Context, id:String):ArrayList<Int>{
        try{
            val map = getMap(context, id)
            val list = map.keys.map{it.type}.toHashSet().toList()
            return ArrayList(list)
        }catch(e:Exception){
            return ArrayList()
        }
    }


    private fun ReceiveData(stopID: String, call: Callback) {
        CurrentStationID = stopID
        val client = OkHttpClient()
        val url = "http://100.114.8.24:8080/api/scrap"
        val jsonBody = "{\"stop\":\"$stopID\"}".trimIndent()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(call)
    }
    private suspend fun getMap(context: Context, stopID: String, pass:Boolean): LinkedHashMap<Bus, ArrayList<ArriveTime>> =
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
                        val typesJson = assetManager.open("types.json").bufferedReader().use { it.readText() }

                        for (bus in buses) {
                            val map = getIsLastStationMap(assetManager, typesJson, bus)
                            val realLastStation = getFromPreferences(
                                context,
                                "busStationSave$CurrentStationID${bus.name}",
                                null,
                                "undefined"
                            )
                            if (!busMap.containsKey(bus)) {
                                val arriveTimes: ArrayList<ArriveTime> = ArrayList()
                                val isLast = isLastStation(bus, map)
                                for (time in bus.arriveTimes) {
                                    val arr = ArriveTime(time, isLast, bus.lastStop)
                                    arriveTimes.add(arr)
                                }
                                // saving to memory
                                if (!realLastStation.isNullOrEmpty() && realLastStation != "undefined") {
                                    arriveTimes.forEach { it -> it.realLastStation = realLastStation }
                                } else if (isLast) {
                                    arriveTimes.forEach { it -> it.realLastStation = bus.lastStop }
                                    saveToPreferences(
                                        context,
                                        "busStationSave$CurrentStationID${bus.name}",
                                        null,
                                        bus.lastStop
                                    )
                                }

                                busMap[bus] = arriveTimes
                            } else {
                                val original: ArrayList<ArriveTime>? = busMap[bus]
                                val isLast = isLastStation(bus, map)
                                for (time in bus.arriveTimes) {
                                    val arr = ArriveTime(time, isLast, bus.lastStop)
                                    original?.add(arr)
                                }

                                if (!realLastStation.isNullOrEmpty() && realLastStation != "undefined") {
                                    original?.forEach { it -> it.realLastStation = realLastStation }
                                } else if (isLast) {
                                    original?.forEach { it -> it.realLastStation = bus.lastStop }
                                    saveToPreferences(
                                        context,
                                        "busStationSave$CurrentStationID${bus.name}",
                                        null,
                                        bus.lastStop
                                    )
                                }

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


    private suspend fun getMap(context: Context, stopID: String): LinkedHashMap<Bus, ArrayList<ArriveTime>> {
        val filters = getList2(context)
        val filter = filters.find{it->it.id == stopID}?.list
        CurrentStationID = stopID
        val scrapperController = ScrapperController()

        val buses: ArrayList<Bus> = scrapperController.getData(stopID)
        val busMap: LinkedHashMap<Bus, ArrayList<ArriveTime>> = LinkedHashMap()
        val assetManager = context.assets
        val typesJson = assetManager.open("types.json").bufferedReader().use { it.readText() }

        for (bus in buses) {
            if(filter?.find{ it->it.id == bus.type}?.state == false) continue

            val map = getIsLastStationMap(assetManager, typesJson, bus)
            val realLastStation =
                getFromPreferences(context, "busStationSave$CurrentStationID${bus.name}", null, "undefined")

            val arriveTimes = busMap.getOrPut(bus) { ArrayList() }
            val isLast = isLastStation(bus, map)

            bus.arriveTimes.forEach { time ->
                arriveTimes.add(ArriveTime(time, isLast, bus.lastStop))
            }

            if (!realLastStation.isNullOrEmpty() && realLastStation != "undefined") {
                arriveTimes.forEach { it.realLastStation = realLastStation }
            } else if (isLast) {
                arriveTimes.forEach { it.realLastStation = bus.lastStop }
                saveToPreferences(context, "busStationSave$CurrentStationID${bus.name}", null, bus.lastStop)
            }

            arriveTimes.sortBy(ArriveTime::minutes)
        }

        return busMap
    }





    private fun saveListMemory(context: Context, map: Map<Bus, ArrayList<ArriveTime>>, glanceId: GlanceId) {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val list = map.map { (bus, arrivals) -> BusEntry(bus, arrivals) }
        prefs.edit { putString("bus_list$glanceId", gson.toJson(list))}
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
        Log.d("stationCheck", "$stationA $check")
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
    private fun saveToPreferences(context:Context, tag:String, widgetID:Int?, value:Any){
        val IDText = if(widgetID!=null){"$widgetID"}else{""}
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{putString("memory $tag $IDText", "$value")}
    }
    private fun getFromPreferences(context:Context, tag:String, widgetID:Int?, default:String):String?{
        val IDText = if(widgetID!=null){"$widgetID"}else{""}
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        return prefs.getString("memory $tag $IDText", default)
    }
    private fun getCurrentStationPair(context: Context): StationPairAdvanced?{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")
        if(pairTextOriginal == "null") return null
        return gson.fromJson(pairTextOriginal, object : TypeToken<StationPairAdvanced>() {}.type)
    }
    private fun getList2(context: Context):ArrayList<Filter>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val listString = prefs.getString("filterList", "")?:""
        if(listString.isEmpty()) return ArrayList()

        return Gson().fromJson(listString, object:TypeToken<ArrayList<Filter>>(){}.type)
    }

    private suspend fun addVehicleID(map:Map<Bus, ArrayList<ArriveTime>>, id:String){
        if(id.length<4) return;
        val controller = ScrapperController()
        val extraStation = controller.getData(id, map.keys.size*4)
        map.forEach { bus, arriveTimes->
            val list2 = extraStation?.departures?.filter{it->it.lineId == bus.exName}?.take(arriveTimes.size)
            list2?.forEachIndexed { index, extraBus->
                Log.d("fuck2", extraBus.destination.bg + " $index")
                arriveTimes[index].vehicleID = extraBus.vehicleId
                val s = when(bus.type){
                    1->"A"
                    2->"TM"
                    3->""
                    4->"TB"
                    5->"A"
                    else -> {
                        ""
                    }
                }
                arriveTimes[index].vehicleID = s+arriveTimes[index].vehicleID.split("/")[1]
                Log.d("fuck2", arriveTimes[index].vehicleID)
            }
        }
    }
    private fun saveCoordinates(context:Context, stopID:String){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        context.assets.open("stops.txt").bufferedReader().useLines{lines->
            lines.drop(1).forEach { line->
                val parts = line.split(",")
                val id = parts[1]
                if(id.isEmpty() || id!=stopID) return@forEach
                val lat = parts[4].toDoubleOrNull()
                val lon = parts[5].toDoubleOrNull()
                if(lat!=null && lon!=null){
                    prefs.edit{
                        putString("stopCoordinates", Gson().toJson(Pair(lat, lon)))
                    }
                    return
                }
            }
        }
    }
}