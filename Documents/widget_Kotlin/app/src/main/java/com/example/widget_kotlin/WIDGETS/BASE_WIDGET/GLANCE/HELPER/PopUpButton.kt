package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.PopUpActivity

class PopUpButton : ActionCallback{
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val list:ArrayList<String> = ArrayList()
        val busType = parameters[ActionParameters.Key<String>("vehicleType")]
        val busStop = parameters[ActionParameters.Key<String>("busStop")]
        val stationStop = parameters[ActionParameters.Key<String>("stationStop")]
        val isLast = parameters[ActionParameters.Key<String>("isLast")]
        if(!busType.isNullOrEmpty()) list.add(busType)
        if(!busStop.isNullOrEmpty() && busStop!="undefined") list.add("Последна спирка: $busStop")
        if(!stationStop.isNullOrEmpty()) list.add("Води до: $stationStop")
        if(!isLast.isNullOrEmpty()) list.add(isLast)
        saveToPreferences(context, list)
        startIntent(context)
    }

    private fun saveToPreferences(context: Context, list:List<String>){
        val preferences = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        preferences.edit {
            putInt("popTextCount", list.size)
            list.forEachIndexed {
                index, string ->
                putString("popText$index", string)
            }
        }
    }

    private fun startIntent(context:Context){
        val intent = android.content.Intent(context, PopUpActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}