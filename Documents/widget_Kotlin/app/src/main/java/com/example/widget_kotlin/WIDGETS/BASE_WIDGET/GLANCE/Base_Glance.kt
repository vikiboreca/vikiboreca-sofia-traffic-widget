package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
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
import androidx.glance.action.clickable
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.BusEntry
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Callback

class Base_Glance : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId)
    {
        val busList = getMemoryList(context, id)
        //Log.d("count", busList.size.toString())
        provideContent {
            GlanceTheme {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.app_widget_background),
                            title = "Welcome, nigger"
                        )
                    }
                ){
                    Column{
                        var itemCount = 0
                        val maxRows = 10
                        var rowsLeft = busList.size
                        for(i in 0 until (busList.size/maxRows)+1){
                            var rows = 0
                            if(rowsLeft/maxRows>0) {rows = maxRows; rowsLeft-=maxRows} else {rows = rowsLeft%maxRows}
                            var currentTarget = itemCount+rows
                            for(j in itemCount until currentTarget){
                                var bus = busList.get(j).bus
                                var arrivals = busList.get(j).arrivals
                                itemCount++
                                Column{Row {
                                    Text(text = bus.name, style = TextStyle(fontSize = 24.sp) ,modifier = GlanceModifier.clickable(actionRunCallback<ActionCallback>()))
                                    Text(" - ", style = TextStyle(fontSize = 24.sp, color = ColorProvider(Color.Cyan, Color.Red)))
                                    arrivals.map(ArriveTime::minutes).forEachIndexed {
                                            index, minutes ->
                                        Row{
                                            Text(minutes.toString(), style = TextStyle(fontSize = 24.sp) ,modifier = GlanceModifier.clickable(actionRunCallback<ActionCallback>()))
                                            if(index != arrivals.size - 1){
                                                Text(", ", style = TextStyle(fontSize = 24.sp))
                                            }
                                        }
                                    }
                                }}
                            }
                        }
                        Column(modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally){
                            Row{
                                Button(text = "Good", onClick = actionRunCallback<BaseButton>())
                                Button(text = "Clear", onClick = actionRunCallback<ActionCallback>(), modifier = GlanceModifier.padding(top = 64.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    private fun getMemoryList(context: Context, glanceId: GlanceId):ArrayList<BusEntry>{
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        val value:String? = prefs.getString("bus_list$glanceId", "")
        if(value.isNullOrEmpty()) return arrayListOf()
        return try{
            val gson = GsonBuilder().create()
            val listType = object: TypeToken<ArrayList<BusEntry>>(){}.type
            gson.fromJson(value, listType)
        }catch(e: Exception){
            Log.d("widget error", e.toString())
            arrayListOf()
        }

    }
}
class BaseButton : ActionCallback{
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
       //val BusStateKey = stringPreferencesKey("bus_state")
        try{
            val map = getMap("2327")
            saveListMemory(context, map, glanceId)
            Base_Glance().update(context, glanceId)
        }catch(e: Exception){
            Log.d("widget error", e.toString())
        }
    }
    private fun ReceiveData(stopID: String, call: Callback){
        val client = OkHttpClient()
        val url = "http://100.114.8.24:8080/api/scrap"
        val jsonBody = "{\"stop\":\"$stopID\"}".trimIndent()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(call)
    }
    private suspend fun getMap(stopID: String): LinkedHashMap<Bus, ArrayList<ArriveTime>> =
        suspendCancellableCoroutine { cont ->  ReceiveData(stopID, object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("fuck ass error", e.toString())
                cont.resumeWith(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    val value = response.body.string()
                    val gsonBuilder = GsonBuilder()
                    val gson = gsonBuilder.create()
                    val listType = object: TypeToken<List<Bus>>(){}.type
                    val buses:List<Bus> = gson.fromJson(value, listType)
                    //Create the Map
                    val busMap: LinkedHashMap<Bus, ArrayList<ArriveTime>> = LinkedHashMap()
                    for(bus in buses){
                        if(!busMap.containsKey(bus)){
                            val arriveTimes: ArrayList<ArriveTime> = ArrayList()
                            for(time in bus.arriveTimes){
                                val arr = ArriveTime(time, true, bus.lastStop)
                                arriveTimes.add(arr)
                            }
                            busMap.put(bus, arriveTimes)
                        }
                        else{
                            val original: ArrayList<ArriveTime>? = busMap.get(bus)
                            for(time in bus.arriveTimes){
                                val arr = ArriveTime(time, false, bus.lastStop)
                                original?.add(arr)
                            }
                            original?.sortBy(ArriveTime::minutes)
                        }
                    }
                    cont.resumeWith(Result.success(busMap))
                //fix the isLastStation for each Bus
                }
                else{
                    cont.resumeWith(Result.failure(Exception("Empty body")))
                }
            }
        })}



    private fun saveListMemory(context: Context, map: Map<Bus, ArrayList<ArriveTime>>, glanceId: GlanceId){
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        val gson = GsonBuilder().create()
        val list = map.map{(bus, arrivals) -> BusEntry(bus, arrivals)}
        prefs.edit { putString("bus_list$glanceId", gson.toJson(list)).apply() }
    }
}