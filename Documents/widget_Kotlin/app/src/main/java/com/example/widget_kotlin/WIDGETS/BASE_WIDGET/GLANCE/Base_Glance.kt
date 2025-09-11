package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.text.Text
import com.example.widget_kotlin.R
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.BusEntry
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import androidx.core.content.edit
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.SizeMode
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.TypeAdvanced
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Callback
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.time.Duration.Companion.minutes

class Base_Glance : GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() = SizeMode.Exact

    var smallerButtons:Boolean = false
    val defaultColor = ColorProvider(Color.Black, Color.White)

    val textSizeDefault = 24

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Standard is 192 x 225 dp for 3 buses at 24 sp with max 16 chars (15 for safety)
        val busList = getMemoryList(context, id)
        provideContent {
            val standard = DpSize(192.dp, 225.dp)
            val size = LocalSize.current
            val ratio = DpSize((size.width / standard.width).dp, (size.height / standard.height).dp)
            var scale = setScale(busList, ratio, size)
            if(busList.isEmpty()){scale = 1f}
            val changePadding = ratio.width>ratio.height


            Log.d("WidgetSize", "Current: width=${size.width.value}dp, height=${size.height.value}dp")

            GlanceTheme {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.app_widget_background),
                            title = "Welcome, nigger"
                        )
                    }
                ) {
                    Column {
                        var itemCount = 0
                        val maxRows = 10
                        var rowsLeft = busList.size
                        repeat((busList.size / maxRows) + 1) {
                            var rows:Int
                            if (rowsLeft / maxRows > 0) {
                                rows = maxRows
                                rowsLeft -= maxRows
                            } else {
                                rows = rowsLeft % maxRows
                            }
                            val currentTarget = itemCount + rows
                            Column {
                                for (j in itemCount until currentTarget) {
                                    val bus = busList[j].bus
                                    val arrivals = busList[j].arrivals
                                    itemCount++
                                    Row {
                                        Text(
                                            text = bus.name,
                                            style = TextStyle(fontSize = textSizeDefault.spScaled(scale)),
                                            modifier = GlanceModifier.clickable(actionRunCallback<ActionCallback>())
                                        )
                                        Text(
                                            " - ",
                                            style = TextStyle(
                                                fontSize = textSizeDefault.spScaled(scale),
                                                color = defaultColor
                                            )
                                        )
                                        arrivals.map(ArriveTime::minutes)
                                            .forEachIndexed { index, minutes ->
                                                val colorState = if(arrivals[index].isLastStation){defaultColor}else{ColorProvider(Color.Red, Color.Red)}
                                                Row {
                                                    Text(
                                                        minutes.toString(),
                                                        style = TextStyle(fontSize = textSizeDefault.spScaled(scale), color = colorState),
                                                        modifier = GlanceModifier.clickable(actionRunCallback<ActionCallback>())
                                                    )
                                                    if (index != arrivals.size - 1) {
                                                        Text(
                                                            ", ",
                                                            style = TextStyle(fontSize = textSizeDefault.spScaled(scale))
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val buttonsPadding = if(changePadding){4}else{12}
                        val buttonScale = if(!smallerButtons){scale * 0.9f}else{scale*0.6f}
                        Row(modifier = GlanceModifier.padding(bottom = buttonsPadding.dpScaled(scale))) {
                            Row(modifier = GlanceModifier.padding(end = 6.dp)) {
                                Button(
                                    text = "Good",
                                    onClick = actionRunCallback<BaseButton>(),
                                    style = TextStyle(fontSize = 18.spScaled(buttonScale))
                                )
                            }
                            Row(modifier = GlanceModifier.padding(start = 6.dp)) {
                                Button(
                                    text = "Clear",
                                    onClick = actionRunCallback<ActionCallback>(),
                                    style = TextStyle(fontSize = 18.spScaled(buttonScale))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMemoryList(context: Context, glanceId: GlanceId): ArrayList<BusEntry> {
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        val value: String? = prefs.getString("bus_list$glanceId", "")
        if (value.isNullOrEmpty()) return arrayListOf()
        return try {
            val gson = GsonBuilder().create()
            val listType = object : TypeToken<ArrayList<BusEntry>>() {}.type
            gson.fromJson(value, listType)
        } catch (e: Exception) {
            Log.d("widget error", e.toString())
            arrayListOf()
        }
    }

    private fun setScale(busList: List<BusEntry>, Scaling: DpSize, Standard:DpSize): Float {
        val maxChars = 9f
        val maxRows = 3f

        val value = maxChars * Scaling.width.value
        val maxLength = floor(value)
        var maxStringLength = 1
        busList.forEach { it ->
            val arrivals = it.arrivals.map{it.minutes}
            val curr = it.bus.name.length + arrivals.toString().length - 2 - (2*(arrivals.size-1)) //for "[]", " " and ","
            if (curr > maxStringLength) maxStringLength = curr
        }

        val maxItems = (maxRows * ((Standard.height.value/100)-1))
        val curr = 1f / maxItems
        val now = 1f / busList.size

        val xScale = maxLength / maxStringLength
        val yScale = 1 / (curr / now)

        smallerButtons = if(abs(xScale-yScale)<0.05f){true}else{false}

        Log.d("comparing", "$xScale $yScale")
        return minOf(xScale, yScale)
    }
    // Helper functions to scale dp and sp
    fun Int.dpScaled(scale: Float) = (this * scale).dp
    fun Int.spScaled(scale: Float) = (this * scale).sp
}


class BaseButton : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            val map = getMap(context,"2327")
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
                            val (map, isLast) = isLastStation(assetManager, typesJson, bus)
                            if (!busMap.containsKey(bus)) {
                                val arriveTimes: ArrayList<ArriveTime> = ArrayList()
                                for (time in bus.arriveTimes) {
                                    val arr = ArriveTime(time, isLast, bus.lastStop)
                                    arriveTimes.add(arr)
                                }
                                busMap[bus] = arriveTimes
                            } else {
                                val original: ArrayList<ArriveTime>? = busMap[bus]
                                for (time in bus.arriveTimes) {
                                    val arr = ArriveTime(time, isLastStation(bus, map), bus.lastStop)
                                    original?.add(arr)
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

    private fun saveListMemory(context: Context, map: Map<Bus, ArrayList<ArriveTime>>, glanceId: GlanceId) {
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val list = map.map { (bus, arrivals) -> BusEntry(bus, arrivals) }
        prefs.edit { putString("bus_list$glanceId", gson.toJson(list)).apply() }
    }


    private fun isLastStation(assetManager: AssetManager,vehicleTypes:String,bus:Bus):Pair<Map<String, List<String>>, Boolean>{
        val map = getIsLastStationMap(assetManager, vehicleTypes, bus)
        val normalizesStation = normalizeString(bus.lastStop)
        map[bus.name]?.forEach {
            val normalizeCompare = normalizeString(it)
            if(normalizesStation == normalizeCompare) return Pair(map, true)
        }
        return Pair(map, false)
    }
    private fun isLastStation(bus:Bus, map:Map<String, List<String>>):Boolean{
        val normalizesStation = normalizeString(bus.lastStop)
        map[bus.name]?.forEach {
            val normalizeCompare = normalizeString(it)
            if(normalizesStation == normalizeCompare) return true
        }
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
        return word.lowercase().replace(Regex("[,.?\":-]"), "")
            .replace("\\s+".toRegex(), "").trim()
    }
}