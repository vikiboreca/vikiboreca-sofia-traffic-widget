package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.PopUpActivity
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter

class PopUpButton : ActionCallback{
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val list:ArrayList<String> = ArrayList()
        val busType = parameters[ActionParameters.Key<String>("vehicleType")]
        var busStop = parameters[ActionParameters.Key<String>("busStop")]
        var stationStop = parameters[ActionParameters.Key<String>("stationStop")]
        val isLast = parameters[ActionParameters.Key<String>("isLast")]
        val isMetro = parameters[ActionParameters.Key<String>("isMetro")]

        if(!busType.isNullOrEmpty()) list.add(busType)
        if(!isMetro.isNullOrEmpty())
        {if(isMetro == "true")
        {
            list.add("Посока: $busStop")
        }
        else{
            if(!busStop.isNullOrEmpty() && busStop!="undefined") {
                list.add("Последна спирка: $busStop")
            }
            if(!isLast.isNullOrEmpty()){
                if(!stationStop.isNullOrEmpty() && isLast == "false") {
                    list.add("Води до: $stationStop")
                    list.add(isLast)
                }
            }
        }
        }
        saveToPreferences(context, list)
        val startActivity = ActivityStarter(PopUpActivity::class.java)
        startActivity.startIntent(context)
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
}