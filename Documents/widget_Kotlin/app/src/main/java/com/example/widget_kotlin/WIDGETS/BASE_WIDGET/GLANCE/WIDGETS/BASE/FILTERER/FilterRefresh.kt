package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.FILTERER

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.Filter
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.FilterPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

class FilterRefresh: ActionCallback {
    val updater = WidgetUpdater(FiltererGlance::class.java)
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters
    ) {
        val id = parameters.getOrDefault(ActionParameters.Key<String>("StationID"), "null")
        val list = getList(context)
        val index = list.indexOfFirst { it->it.id == id }

        if(index!=-1) setList(context, id, index, list)
    }

    private fun getList(context: Context):ArrayList<Filter>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val listString = prefs.getString("filterList", "")?:""
        if(listString.isEmpty()) return ArrayList()

        return Gson().fromJson(listString, object:TypeToken<ArrayList<Filter>>(){}.type)
    }
    private fun saveList(context:Context, list:ArrayList<Filter>){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{
            putString("filterList", Gson().toJson(list))
        }
    }

    private fun setList(context:Context, id:String, idx: Int, list:ArrayList<Filter>){
        CoroutineScope(Dispatchers.Default).launch {
            val types = BaseButton().getTypes(context, id)
            Log.d("fuck2", "$types $id")
            list[idx].reset()
            list[idx].initialize(types)
            saveList(context, list)
            updater.updateWidget(context)
            Log.d("fuck2", "done")
            list.forEach { it->
                Log.d("fuck2", it.id + " " + it.list.toString())
            }
        }
    }
}