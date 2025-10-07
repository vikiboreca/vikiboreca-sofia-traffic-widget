package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers

class WidgetUpdater(val widgetClass: Class<out GlanceAppWidget>)  {

    private suspend fun forceUpdateWidget(context: Context) {
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(widgetClass)
        glanceIds.forEach { id ->
            updateAppWidgetState(context, id){
                it[longPreferencesKey("now")] = System.currentTimeMillis()
            }
            widgetClass.getDeclaredConstructor().newInstance().update(context, id)
        }
    }

    private suspend fun normalUpdateWidget(context: Context){
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(widgetClass)
        glanceIds.forEach { id ->
            widgetClass.getDeclaredConstructor().newInstance().update(context, id)
        }
    }

    suspend fun updateWidget(context: Context){
        val force = needToForce(context)
        val timeManager = TimerManager
        if (!force) {
            normalUpdateWidget(context)
            //Log.d("nigger", "${widgetClass.simpleName}  normal update")
        } else {
            forceUpdateWidget(context)
            //Log.d("nigger", "${widgetClass.simpleName}  force update")
        }
        setForce(context, true)
        timeManager.startForClass(widgetClass, 30000, {setForce(context, false)})
    }

    private fun needToForce(context:Context):Boolean{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        return prefs.getBoolean("Force ${widgetClass.simpleName} update", false)
    }

    private fun setForce(context: Context, force:Boolean){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{
            putBoolean("Force ${widgetClass.simpleName} update", force)
        }
    }
}
object TimerManager{
    private val timers = mutableMapOf<Class<*>, Job>()

    fun startForClass(widgetClass: Class<*>, delayMs:Long, onFinish: () -> Unit){
        cancelForClass(widgetClass)

        val job = GlobalScope.launch(Dispatchers.Default) {
            delay(delayMs)
            onFinish()
            timers.remove(widgetClass)
        }
        timers[widgetClass] = job
    }

    fun cancelForClass(widgetClass: Class<*>){
        timers[widgetClass]?.cancel()
        timers.remove(widgetClass)
    }
}