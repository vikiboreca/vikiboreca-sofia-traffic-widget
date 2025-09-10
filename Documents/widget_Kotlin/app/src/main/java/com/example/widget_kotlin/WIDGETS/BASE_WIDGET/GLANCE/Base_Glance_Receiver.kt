package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class Base_Glance_Receiver() : GlanceAppWidgetReceiver()
{
    override val glanceAppWidget: GlanceAppWidget
        get() = Base_Glance()

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        SaveTypes(context, "Автобус", "Трамвай", "Метро", "Тролей", "Нощен автобус")
        Log.d("enable", "enable")
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        ClearTypes(context)
        Log.d("disable", "disable")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d("delete", "delete")
        ClearList(context, appWidgetIds)
    }
    private fun SaveTypes(context: Context?, vararg stations: String){
        val SharedPreference = context?.getSharedPreferences("TransportTypes", Context.MODE_PRIVATE)
        val firstRun = SharedPreference?.getBoolean("firstRun", true)
        if(firstRun == true){
            SharedPreference.edit {
                for (i in 0 until stations.size) {
                    putString((i + 1).toString(), stations[i]).apply()
                }
                putBoolean("firstRun", false).apply()
            }
        }
    }

    private fun ClearTypes(context: Context?){
        val SharedPreference = context?.getSharedPreferences("TransportTypes", Context.MODE_PRIVATE)
        SharedPreference?.edit {
            clear().apply()
        }

    }
    private fun ClearList(context: Context?, ids: IntArray) {
        val busPreference = context?.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        busPreference?.edit {
            for(id in ids){
                remove("bus_list$id").apply()
            }
        }
    }
}