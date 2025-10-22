package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.text.Text
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.BusEntry
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.MetroArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.MetroEntry
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.TypeAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.PopUpButton
import com.google.gson.Gson
import java.util.stream.Collectors

class BaseGlance : GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() = SizeMode.Exact
    val defaultColor = ColorProvider(Color.Black, Color.White)

    val reverseDefaultColor = ColorProvider(Color.White, Color.Black)
    val textSizeDefault = 24

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Standard is 192 x 225 dp for 3 buses at 24 sp with max 16 chars (15 for safety)
        provideContent {
            val busList = getMemoryList(context, id)
            val metroList = getMetroList(context, busList)
            val currentPair = getCurrentStationPair(context)
            val isMetroStation = !metroList.isEmpty()

            val prefs = currentState<Preferences>()
            val now = prefs[longPreferencesKey("now")]

            val standard = DpSize(192.dp, 225.dp)
            val size = LocalSize.current
            val ratio = DpSize((size.width / standard.width).dp, (size.height / standard.height).dp)
            Log.d("nigger", "Current: width=${size.width.value}dp, height=${size.height.value}dp")
            var scale = if(isMetroStation){1.15f}else{setScale(busList, ratio, size)}
            if(busList.isEmpty()){scale = 1f}

            val busDisplay: @Composable () -> Unit = {
                Column {
                    var itemCount = 0
                    val maxRows = 10
                    var rowsLeft = busList.size
                    repeat((busList.size / maxRows) + 1) {
                        var rows:Int
                        if (rowsLeft / maxRows > 0) {
                            rows = maxRows
                            rowsLeft -= maxRows
                        } else {
                            rows = rowsLeft % maxRows
                        }
                        val currentTarget = itemCount + rows
                        Column {
                            var spacerSpace = 4
                            if(busList.size>10 && (size.height.value/100).toInt() == 2) spacerSpace = 1
                            for (j in itemCount until currentTarget) {
                                val bus = busList[j].bus
                                val arrivals = busList[j].arrivals
                                val content: @Composable () -> Unit = {
                                    Row {
                                        BusBox(bus, scale, context)
                                        Text(
                                            " - ",
                                            style = TextStyle(
                                                fontSize = textSizeDefault.spScaled(scale),
                                                color = defaultColor
                                            )
                                        )
                                        arrivals.map(ArriveTime::minutes)
                                            .forEachIndexed { index, minutes ->
                                                val colorState = if(arrivals[index].isLastStation){defaultColor}else{ColorProvider(Color.Red, Color.Red)}
                                                Row {
                                                    Text(
                                                        minutes.toString(),
                                                        style = TextStyle(fontSize = textSizeDefault.spScaled(scale), color = colorState),
                                                        modifier = GlanceModifier.clickable(actionRunCallback<PopUpButton>(
                                                            parameters = actionParametersOf(ActionParameters.Key<String>("stationStop") to arrivals[index].lastStation,
                                                                ActionParameters.Key<String>("busStop") to arrivals[index].realLastStation,
                                                                ActionParameters.Key<String>("isLast") to arrivals[index].isLastStation.toString(),
                                                                ActionParameters.Key<String>("isMetro") to "false")
                                                        )
                                                        )
                                                    )
                                                    if (index != arrivals.size - 1) {
                                                        Text(
                                                            ", ",
                                                            style = TextStyle(fontSize = textSizeDefault.spScaled(scale))
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                }
                                val spacer: @Composable () -> Unit = {
                                    Spacer(modifier = GlanceModifier.height(spacerSpace.dpScaled(scale)))
                                }
                                Column{
                                    content()
                                    spacer()
                                }
                                itemCount++
                            }
                        }
                    }
                }
            }
            val metroDisplay: @Composable () -> Unit = {
                Column(modifier = GlanceModifier.fillMaxSize(), Alignment.Top){
                    if(metroList.size%2 == 1){
                        Column{
                            Row{
                                Column(GlanceModifier.padding(top = 15.dp)){
                                    BusBox(metroList[0].metro, scale)
                                }
                                DisplayMetroStations(metroList[0], 26)
                            }
                        }

                    }
                    else{
                        Column{
                            metroList.forEachIndexed { index, entry ->
                                val space = 10+(index)*5
                                Row(GlanceModifier.padding(top = space.dp)){
                                    Column(GlanceModifier.padding(top = 15.dp)){
                                        BusBox(entry.metro, scale)
                                    }
                                    DisplayMetroStations(entry, 26)
                                }
                            }
                        }
                    }
                }
            }
            val contentDisplay = if(isMetroStation){metroDisplay}else{busDisplay}

            GlanceTheme {
                Scaffold(
                    titleBar = {
                        val text = currentPair?.original?.Name ?: "Not selected"
                        CustomTitleBar(text)
                    },
                    backgroundColor = Color(0xFFd9e5fc).toColorProvider(),
                    content = contentDisplay
                )
            }
        }
    }

    private fun getMemoryList(context: Context, glanceId: GlanceId): ArrayList<BusEntry> {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val value: String? = prefs.getString("bus_list$glanceId", "")
        if (value.isNullOrEmpty()) return arrayListOf()
        return try {
            val gson = GsonBuilder().create()
            val listType = object : TypeToken<ArrayList<BusEntry>>() {}.type
            gson.fromJson(value, listType)
        } catch (e: Exception) {
            Log.d("widget error", e.toString())
            arrayListOf()
        }
    }

    private fun getMetroList(context: Context,list:ArrayList<BusEntry>):ArrayList<MetroEntry>{
        val metroList:ArrayList<MetroEntry> = ArrayList()
        if(list.isEmpty()) return metroList
        if(list[0].bus.type != 3) return metroList
        val metroStations = getMetroStations(context)
        val assign: (list: List<ArriveTime>, checkStation:String,lastStation:String, realLastStation:String) -> Unit =
            {
                list, checkStation ,lastStation, realLastStation ->
                list.forEach { arriveTime -> if(arriveTime.lastStation == checkStation){arriveTime.realLastStation = realLastStation; arriveTime.lastStation = lastStation} }
            }
        list.forEach { it ->
            val listArr = it.arrivals
            when((it.bus.name[1]-'0')){
                1 -> assign(listArr, "Витоша", "Сливница", "Сливница")
                2 -> assign(listArr, "Бизнес парк", "Обеля", "Обеля")
                4 -> assign(listArr, "Витоша", "Сливница", "Обеля")
            }
            val pair = getMetroStationEndStops(metroStations, it.bus.name)
            val listA = it.arrivals.stream().filter{arr -> arr.realLastStation == pair.first}.map{arr-> MetroArriveTime(arr.minutes,arr.lastStation, arr.realLastStation) }.limit(3).collect(Collectors.toCollection {ArrayList()})
            val listB = it.arrivals.stream().filter{arr -> arr.realLastStation == pair.second}.map{arr-> MetroArriveTime(arr.minutes,arr.lastStation, arr.realLastStation) }.limit(3).collect(Collectors.toCollection {ArrayList()})
            val metroEntry = MetroEntry(it.bus.name, listA, listB)
            metroList.add(metroEntry)
        }
        metroList.sortBy { it-> it.metro[1]-'0' }
        return metroList
    }

    private fun getCurrentStationPair(context: Context): StationPairAdvanced?{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")
        if(pairTextOriginal == "null") return null
        return gson.fromJson(pairTextOriginal, object : TypeToken<StationPairAdvanced>() {}.type)
    }

    private fun setScale(busList: ArrayList<BusEntry>, Scaling: DpSize, Standard:DpSize): Float {
        val maxChars = 8.5f
        val maxRows = if(busList.size>7){3f}else{4f}

        val value = maxChars * Scaling.width.value
        var maxStringLength = 1
        busList.forEach { it ->
            val arrivals = it.arrivals.map{it.minutes}
            val curr = it.bus.name.length + arrivals.toString().length - 2 - (2*(arrivals.size-1)) //for "[]", " " and ","
            if (curr > maxStringLength) maxStringLength = curr
        }

        val maxItems = (maxRows * ((Standard.height.value/100)-1).toInt())
        val curr = 1f / maxItems
        val now = 1f / busList.size

        val xScale = value / maxStringLength
        val yScale = 1 / (curr / now)

        Log.d("comparing", "$xScale $yScale")
        var finalScale = minOf(xScale, yScale)
        val xMax = Scaling.width.value*1.125f
        finalScale = finalScale.coerceAtMost(xMax)
        return finalScale
    }
    private fun getTypeName(context:Context,bus:Bus):String{
        val assetManager = context.assets
        val typesJson = assetManager.open("types.json").bufferedReader().use{it.readText()}
        val gson = GsonBuilder().create()
        val data: TypeAdvanced = gson.fromJson(typesJson, object : TypeToken<TypeAdvanced>() {}.type)
        val typeList = data.types
        //Log.d("nigga", "${typeList.size}")
        return typeList[bus.type-1].name
    }
    // Helper functions to scale dp and sp
    fun Int.dpScaled(scale: Float) = (this * scale).dp
    fun Int.spScaled(scale: Float) = (this * scale).sp

    private fun getMetroStations(context:Context): StationAdvanced{
        val assetManager = context.assets
        val listText = assetManager.open("метро.json").bufferedReader().use{it.readText()}
        val gson = Gson()
        val listType = object : TypeToken<StationAdvanced>() {}.type
        return gson.fromJson(listText, listType)
    }

    private fun getMetroStationEndStops(list: StationAdvanced, lineName:String):Pair<String, String>{
        val station = list.lines.stream().filter {line-> line.name == lineName}.findFirst().orElse(null)
        return Pair(station.stops[0], station.stops[1])
    }
    //New functions
    @Composable
    private fun BusBox(bus:Bus, scale: Float, context: Context){
        Box(modifier = GlanceModifier.background(busColor(bus)).cornerRadius(12.dp).padding(horizontal = 6.dp, vertical = 3.dp)
            .clickable(actionRunCallback<PopUpButton>(
                parameters = actionParametersOf(ActionParameters.Key<String>("vehicleType") to getTypeName(context, bus))
            ))){
            Text(text = bus.name,
                style = TextStyle(fontSize = textSizeDefault.spScaled(scale*0.9f), color = reverseDefaultColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )
        }
    }
    @Composable
    private fun BusBox(metroName:String, scale: Float){
        Row{
            Box(modifier = GlanceModifier.background(busColor(metroName[1]-'0')).cornerRadius(12.dp).padding(horizontal = 6.dp, vertical = 3.dp)
                .clickable(actionRunCallback<PopUpButton>(
                    parameters = actionParametersOf(ActionParameters.Key<String>("vehicleType") to "метро")
                ))){
                Text(text = metroName,
                    style = TextStyle(fontSize = textSizeDefault.spScaled(scale*0.75f), color = reverseDefaultColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                )
            }
            Text("⟋\n⟍")
        }
    }
    private fun busColor(bus:Bus):Color{
        return when(bus.type){
            1 -> Color(0xFFC70039)
            2 -> Color(0xFFF59223)
            4 -> Color(0xFF26A6D2)
            5 -> Color.Black
            else -> {
                val metroNumber = bus.name[1] - '0'
                busColor(metroNumber)
            }
        }
    }
    private fun busColor(num:Int):Color{
        return when(num){
            1 -> Color(0xFFE4002B)
            2 -> Color(0xFF003D79)
            3 -> Color(0xFF4D9747)
            4 -> Color(0xFFF59223)
            else -> {
                Log.d("nigger", "number Color Problem")
                Color.Transparent //nigga what
            }
        }
    }

    @Composable
    private fun CustomTitleBar(text:String){
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFafd8f0)) // optional background
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically // 👈 centers icon + text vertically
        ) {
            Text(
                text = "\uD83D\uDD04",
                style = TextStyle(fontSize = 20.sp, color = defaultColor, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.clickable(actionRunCallback<BaseButton>())
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            val scale = if(text.length>14){14f/text.length}else{1f}
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 20.spScaled(scale),
                    color = defaultColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
    }
    @Composable
    private fun DisplayMetroStations(metro: MetroEntry, size:Int){
        val func: @Composable (list:ArrayList<MetroArriveTime>) -> Unit = {
            it ->
            Row(GlanceModifier.padding(start = 4.dp)){
                it.forEachIndexed { index,it ->
                    Text(it.minutes.toString(), style = TextStyle(fontSize = size.sp),
                        modifier = GlanceModifier.clickable(actionRunCallback<PopUpButton>(
                            parameters = actionParametersOf(ActionParameters.Key<String>("stationStop") to it.direction,
                                ActionParameters.Key<String>("busStop") to it.normalDirection,
                                ActionParameters.Key<String>("isMetro") to "true")
                        )))
                    if(index!=metro.direction.lastIndex){
                        Text(", ", style = TextStyle(fontSize = size.sp))
                    }
                }
            }
        }
        Column{
            func(metro.direction)
            func(metro.oppDirection)
        }
    }
    private fun Color.toColorProvider() = ColorProvider(this, this)
}