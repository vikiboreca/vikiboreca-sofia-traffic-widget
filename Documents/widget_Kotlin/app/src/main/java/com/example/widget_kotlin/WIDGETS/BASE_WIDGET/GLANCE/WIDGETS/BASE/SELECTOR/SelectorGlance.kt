package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import androidx.core.content.edit
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.Switch
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.AddStationActivity
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.EditStationButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.BaseWidget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SelectorGlance: BaseWidget() {

    @Composable
    override fun UIContent(context: Context, id: GlanceId, prefs:Preferences) {
        val list = getList(context)
        val chosenName = prefs[stringPreferencesKey("chosenStation")] ?: ""
        val realChosenName = getCurrentStationPair(context)
            Scaffold {
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
                                itemCount++
                                val pairAdvanced = list[j]
                                val pair = pairAdvanced.original
                                Row(modifier = GlanceModifier.fillMaxWidth()){
                                    Text(pair.Name, style = TextStyle(fontWeight = FontWeight.Bold),
                                        modifier = GlanceModifier.clickable(
                                            actionRunCallback<EditStationButton>(
                                                parameters = actionParametersOf(
                                                    ActionParameters.Key<String>("StationPair") to "${pair.ID}\n${pair.Name}"
                                                )
                                            )
                                        ).padding(top = 3.dp)
                                    )
                                    Row(modifier = GlanceModifier.defaultWeight(),horizontalAlignment = Alignment.End){
                                        Switch(
                                            pair.Name == realChosenName,
                                            onCheckedChange = {
                                                CoroutineScope(Dispatchers.Default).launch {
                                                    saveCurrentStation(context, pairAdvanced)
                                                    updateAppWidgetState(context, id){prefsState ->
                                                        val toRemember = prefsState[stringPreferencesKey("chosenStation")]
                                                        prefsState[stringPreferencesKey("chosenStation")] = if(toRemember!=pair.Name){pair.Name}else{""}
                                                    }
                                                    selectorUpdater.updateWidget(context)
                                                    delay(100)
                                                    basicUpdater.updateWidget(context)
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
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Button(
                            text = "...",
                            onClick = {ClearList(context)},
                        )
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
    private fun getList(context: Context):ArrayList<StationPairAdvanced>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val listString = prefs.getString("PairList", null)
        if(!listString.isNullOrEmpty()){
            return gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        }
        return ArrayList()
    }
    private fun saveCurrentStation(context: Context,pairAdvanced: StationPairAdvanced){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val pairTextOriginal = prefs.getString("activeStation", "null")

        var pairText = gson.toJson(pairAdvanced)

        if(pairText == pairTextOriginal) pairText = "null"
        prefs.edit{
            putString("activeStation", pairText)
        }
        //Log.d("nigger", pairText)
    }
    private fun getCurrentStationPair(context: Context):String{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")
        if(pairTextOriginal == "null") return ""
        val advanced: StationPairAdvanced? = gson.fromJson(pairTextOriginal, object : TypeToken<StationPairAdvanced>() {}.type)
        return advanced?.original?.Name ?: ""
    }
}