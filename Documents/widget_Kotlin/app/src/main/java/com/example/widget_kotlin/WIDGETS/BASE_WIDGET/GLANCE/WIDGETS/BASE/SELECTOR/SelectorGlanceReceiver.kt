package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectorGlanceReceiver : GlanceAppWidgetReceiver() {
    val updater = WidgetUpdater(SelectorGlance::class.java)
    override val glanceAppWidget: GlanceAppWidget
        get() = SelectorGlance()

//    override fun onAppWidgetOptionsChanged(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int,
//        newOptions: Bundle
//    ) {
//        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
//        CoroutineScope(Dispatchers.Default).launch {
//            val id = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
//            updateAppWidgetState(
//                context,
//                id
//            ){
//                preferences ->
//                preferences[Preferences.Key<String>("")] = ""
//            }
//        }
//
//    }
}