package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import androidx.core.content.edit
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.Switch
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.AddStationActivity
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class SelectorGlance: GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    val updater = WidgetUpdater(SelectorGlance::class.java)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val now = prefs[longPreferencesKey("now")]
            val chosenName = prefs[stringPreferencesKey("chosenStation")] ?: ""
            val scope = rememberCoroutineScope()

            val list = getList(context)
            GlanceTheme{
                Scaffold {
                    //Text("${list.size}")

                    Column {
                        var itemCount = 0
                        val maxRows = 10
                        var rowsLeft = list.size
                        repeat((list.size / maxRows) + 1) {
                            var rows: Int
                            if (rowsLeft / maxRows > 0) {
                                rows = maxRows
                                rowsLeft -= maxRows
                            } else {
                                rows = rowsLeft % maxRows
                            }
                            val currentTarget = itemCount + rows
                            Column {
                                for (j in itemCount until currentTarget) {
                                    val pair = list[j]
                                    itemCount++
                                    Row(modifier = GlanceModifier.fillMaxWidth()){
                                        Text(pair.Name)/*TODO(Make the text interactable to add counter station)*/
                                        Row(modifier = GlanceModifier.defaultWeight(),horizontalAlignment = Alignment.End){
                                            Switch(
                                                pair.Name == chosenName,
                                                onCheckedChange = {
                                                    scope.launch {
                                                        updateAppWidgetState(context, id){prefsState ->
                                                            val toRemember = prefsState[stringPreferencesKey("chosenStation")]
                                                            prefsState[stringPreferencesKey("chosenStation")] = if(toRemember!=pair.Name){pair.Name}else{""}
                                                        }
                                                        updater.updateWidget(context)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                            }

                    Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.Bottom, horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        Row(modifier = GlanceModifier.padding(bottom = 6.dp))
                        {
                            Button(
                                text = "Add station",
                                onClick = actionStartActivity<AddStationActivity>(),
                                style = TextStyle(fontSize = 18.sp)
                            )
                            Button("Clear List", onClick = {ClearList(context)})
                        }
                    }
                }
            }
        }
    }
    private fun ClearList(context: Context){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{
            remove("PairList")
        }
    }
    private fun getList(context: Context):ArrayList<StationPair>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val listString = prefs.getString("PairList", null)
        if(!listString.isNullOrEmpty()){
            return gson.fromJson(listString, object : TypeToken<ArrayList<StationPair>>() {}.type)
        }
        return ArrayList()
    }
}