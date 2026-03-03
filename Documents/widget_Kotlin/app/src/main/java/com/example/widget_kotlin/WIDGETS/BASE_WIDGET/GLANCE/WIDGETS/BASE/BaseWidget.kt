package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF.BaseGlance

open class BaseWidget: GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() = SizeMode.Exact

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    val selectorUpdater = WidgetUpdater(SelectorGlance::class.java)
    val basicUpdater = WidgetUpdater(BaseGlance::class.java)

    val standard = DpSize(192.dp, 225.dp)


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent{
            ProvideContent(context, id)
        }
    }
    @Composable
    protected fun ProvideContent(context: Context, id: GlanceId){
        GlanceTheme{
            val prefs = currentState<Preferences>()
            val now = prefs[longPreferencesKey("now")]
            UIContent(context, id, prefs)
        }
    }
    @Composable
    protected open fun UIContent(context: Context, id: GlanceId, prefs: Preferences){
        Log.d("nigger", "You haven't overridden the UI content function")
    }
}