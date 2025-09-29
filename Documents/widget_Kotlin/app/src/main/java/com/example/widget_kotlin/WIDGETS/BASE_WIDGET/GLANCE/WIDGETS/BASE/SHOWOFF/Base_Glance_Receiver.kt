package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF

import android.content.Context
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class Base_Glance_Receiver() : GlanceAppWidgetReceiver()
{
    override val glanceAppWidget: GlanceAppWidget
        get() = Base_Glance()


    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        ClearList(context, appWidgetIds)
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