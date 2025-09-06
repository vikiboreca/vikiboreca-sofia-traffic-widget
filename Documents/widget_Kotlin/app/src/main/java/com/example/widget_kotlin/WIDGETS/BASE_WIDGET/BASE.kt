package com.example.widget_kotlin.WIDGETS.BASE_WIDGET

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.edit
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
import org.w3c.dom.Text
import java.io.IOException
import java.util.function.Consumer

/**
 * Implementation of App Widget functionality.
 */
class BASE : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context)
        SaveTypes(context, "Автобус", "Трамвай", "Метро", "Тролей", "Нощен автобус")
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context)
        ClearTypes(context)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if("Request" == intent?.action){
            val widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if(widgetID != AppWidgetManager.INVALID_APPWIDGET_ID){
                val views = RemoteViews(context?.packageName, R.layout.b_a_s_e)
                RecieveData("0821", object: okhttp3.Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("Widget Error", e.toString())
                        views.setTextViewText(R.id.response, "Error")
                        AppWidgetManager.getInstance(context).updateAppWidget(widgetID, views)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if(response.isSuccessful){
                            //get the list of objects
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
                                        val arr: ArriveTime = ArriveTime(time, true, bus.lastStop)
                                        arriveTimes.add(arr)
                                    }
                                    busMap.put(bus, arriveTimes)
                                }
                                else{
                                    val original: ArrayList<ArriveTime>? = busMap.get(bus)
                                    for(time in bus.arriveTimes){
                                        val arr: ArriveTime = ArriveTime(time, false, bus.lastStop)
                                        original?.add(arr)
                                    }
                                    original?.sortBy(ArriveTime::minutes)
                                }
                            }
                            //show text
                            views.setTextViewText(R.id.response, displayText(busMap))
                            AppWidgetManager.getInstance(context).updateAppWidget(widgetID, views)
                        }
                    }

                })

            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.Test)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.b_a_s_e)
    val intent = Intent(context, BASE::class.java)
    intent.setAction("Request")
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    views.setOnClickPendingIntent(R.id.button_click_me, pendingIntent)
    views.setTextViewText(R.id.response, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}



private fun SaveTypes(context: Context, vararg stations: String){
    val SharedPreference = context.getSharedPreferences("TransportTypes", Context.MODE_PRIVATE)
    val firstRun = SharedPreference.getBoolean("firstRun", true)
    if(firstRun){
        SharedPreference.edit {
            for (i in 0 until stations.size) {
                putString((i + 1).toString(), stations[i])
            }
            putBoolean("firstRun", false)
        }
    }
}

private fun ClearTypes(context: Context){
    val SharedPreference = context.getSharedPreferences("TransportTypes", Context.MODE_PRIVATE)
    SharedPreference.edit {
        clear()
    }
}

private fun RecieveData(stopID: String, call: okhttp3.Callback){
    val client = OkHttpClient()
    val url = "http://100.114.8.24:8080/api/scrap"
    val jsonBody = "{\"stop\":\"$stopID\"}".trimIndent()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = jsonBody.toRequestBody(mediaType)
    val request = Request.Builder().url(url).post(requestBody).build()
    client.newCall(request).enqueue(call)
}
private fun displayText(buses: Map<Bus, List<ArriveTime?>>): String {
    return buses.entries.joinToString("\n") { (bus, times) ->
        val minutes = times.mapNotNull { it?.minutes }  // ignores nulls
        "${bus.name} - ${minutes.joinToString(", ")}"
    }
}
