package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import androidx.core.content.edit
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.Switch
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.EditStationList
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.EditStationButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.BaseWidget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SelectorGlance : BaseWidget() {

    // Keys for preferences
    private val ActiveIndexKey = intPreferencesKey("activeIndex")
    private val LastHeightKey = intPreferencesKey("lastHeight")
    private val ChosenStationKey = stringPreferencesKey("chosenStation")

    @Composable
    override fun UIContent(context: Context, id: GlanceId, prefs: Preferences) {

        // Load data
        //remove(context)
        val list = getList(context)
        val listName = getListName(context)
        val realChosenName = getCurrentStationPair(context)

        val size = LocalSize.current

        setActiveIndex(context, id, size, prefs)

        val lists = getLists(list, size)
        var activeIndex = prefs[ActiveIndexKey] ?: 0
        if(activeIndex>=lists.size) activeIndex = 0

        val stationList = if (lists.isNotEmpty()) lists[activeIndex] else ArrayList()

        //Log.d("SelectorGlance", "lists.size=${lists.size}, activeIndex=$activeIndex")

        Scaffold(
            titleBar = { CustomTitleBar(context, listName, id, lists) },
            backgroundColor = Color(0xFFd9e5fc).toColorProvider(),
            content = { ContentDisplay(context, id, stationList, realChosenName) }
        )
    }
    private fun setActiveIndex(context: Context, id: GlanceId, size: DpSize, prefs: Preferences) {
        val currentHeight = size.height.value.toInt()
        val lastHeight = prefs[LastHeightKey] ?: -1

        CoroutineScope(Dispatchers.Default).launch {
            updateAppWidgetState(context, id) { state ->
                when {
                    lastHeight != -1 && lastHeight != currentHeight -> {
                        state[ActiveIndexKey] = 0
                        state[LastHeightKey] = currentHeight
                    }
                    lastHeight == -1 -> {
                        state[LastHeightKey] = currentHeight
                    }
                }
            }
        }
    }

    private fun Color.toColorProvider() = ColorProvider(this, this)

    private fun remove(context: Context){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{
            remove("PairList")
        }
    }

    private fun getList(context: Context): ArrayList<StationPairAdvanced> {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("PairList", "") ?: ""
        return if (listString.isNotEmpty()) {
            gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        } else ArrayList()
    }

    private fun getListName(context: Context): String {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        return prefs.getString("PairListName", "no list") ?: "no list"
    }

    private fun getLists(list: ArrayList<StationPairAdvanced>, size: DpSize): ArrayList<ArrayList<StationPairAdvanced>> {
        if(list.isEmpty()) return ArrayList()
        val start: Int = size.height.value.toInt() / 100 - 2
        var cycles = if (start == 0) 4 else 2
        if (start == 3) cycles = 1
        val l: List<Int> = listOf(6, 11, 15, 20)
        var idx = 0
        val lists: ArrayList<ArrayList<StationPairAdvanced>> = ArrayList()
        for (i in 0 until cycles) {
            val stationList: ArrayList<StationPairAdvanced> = ArrayList()
            for (j in 0 until l[start]) {
                stationList.add(list[idx])
                idx++
                if (idx >= list.size) {
                    lists.add(stationList); break
                }
            }
            if (idx >= list.size) break
            lists.add(stationList)
        }
        return lists
    }

    private fun saveCurrentStation(context: Context, pairAdvanced: StationPairAdvanced) {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")
        var pairText = gson.toJson(pairAdvanced)
        if (pairText == pairTextOriginal) pairText = "null"
        prefs.edit { putString("activeStation", pairText) }
    }

    private fun getCurrentStationPair(context: Context): String {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")
        if (pairTextOriginal == "null") return ""
        val advanced: StationPairAdvanced? = gson.fromJson(pairTextOriginal, object : TypeToken<StationPairAdvanced>() {}.type)
        return advanced?.current?.Name ?: ""
    }

    @Composable
    private fun CustomTitleBar(
        context: Context,
        text: String,
        glanceId: GlanceId,
        list: ArrayList<ArrayList<StationPairAdvanced>>
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFafd8f0))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "\uD83D\uDD04",
                style = TextStyle(fontSize = 20.sp, color = ColorProvider(Color.Black, Color.White), fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.clickable {
                    if(list.isNotEmpty())
                    {
                        CoroutineScope(Dispatchers.Default).launch {
                            updateAppWidgetState(context, glanceId) { prefsState ->
                                var index = prefsState[ActiveIndexKey] ?: 0
                                index = (index + 1) % list.size
                                prefsState[ActiveIndexKey] = index
                            }
                            selectorUpdater.updateWidget(context)
                        }
                    }
                }
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            val scale = if (text.length > 14) 14f / text.length else 1f
            Text(
                text = text,
                style = TextStyle(
                    fontSize = (20f * scale).sp,
                    color = ColorProvider(Color.Black, Color.White),
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.clickable(actionStartActivity<EditStationList>())
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
    }

    @Composable
    private fun ContentDisplay(
        context: Context,
        id: GlanceId,
        list: ArrayList<StationPairAdvanced>,
        realChosenName: String
    ) {
        Column {
            var itemCount = 0
            val maxRows = 10
            var rowsLeft = list.size
            repeat((list.size / maxRows) + 1) {
                val rows = if (rowsLeft > maxRows) maxRows else rowsLeft
                rowsLeft -= rows
                val currentTarget = itemCount + rows
                Column {
                    for (j in itemCount until currentTarget) {
                        val pairAdvanced = list[j]
                        val pair = pairAdvanced.current
                        itemCount++
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            Text(
                                pair.Name,
                                style = TextStyle(fontWeight = FontWeight.Bold),
                                modifier = GlanceModifier.clickable(
                                    actionRunCallback<EditStationButton>(
                                        parameters = actionParametersOf(
                                            ActionParameters.Key<String>("StationPair") to "${pair.ID}\n${pair.Name}"
                                        )
                                    )
                                ).padding(top = 3.dp)
                            )
                            Row(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.End) {
                                Switch(
                                    pair.Name == realChosenName,
                                    onCheckedChange = {
                                        var state = "";
                                        val glanceID = getGlanceId(context)
                                        CoroutineScope(Dispatchers.Default).launch {
                                            saveCurrentStation(context, pairAdvanced)
                                            updateAppWidgetState(context, id) { prefsState ->
                                                val toRemember = prefsState[ChosenStationKey]
                                                state = if (toRemember != pair.Name) pair.Name else ""
                                                prefsState[ChosenStationKey] = state
                                            }
                                            Parallel(
                                                {
                                                    if(state!=""){
                                                        BaseButton().getResults(context, glanceID)
                                                    }
                                                    basicUpdater.updateWidget(context)
                                                },
                                                {
                                                    if(state!=""){
                                                        val list = BaseButton().getTypes(context, pair.ID)
                                                        saveTypes(context, list)
                                                    }
                                                    else saveTypes(context, null)
                                                    filtererUpdater.updateWidget(context)
                                                },
                                                {
                                                    selectorUpdater.updateWidget(context)
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    private fun getGlanceId(context: Context):String{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        return prefs.getString("glanceId","")?:""
    }
    private fun saveTypes(context: Context, list:ArrayList<Int>?){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)

        prefs.edit{
            if(list!=null) putString("currentTypes", Gson().toJson(list))
            else putString("currentTypes", "")
        }
    }
    suspend fun Parallel(vararg tasks: suspend () -> Unit) =
        coroutineScope {
            tasks.map { async { it() } }.awaitAll()
        }
}