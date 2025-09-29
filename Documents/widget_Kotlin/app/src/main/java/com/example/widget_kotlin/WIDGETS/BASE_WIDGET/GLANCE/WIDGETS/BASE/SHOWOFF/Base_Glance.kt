package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.text.Text
import com.example.widget_kotlin.R
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.BusEntry
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.SizeMode
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.TypeAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.PopUpButton
import kotlin.math.abs
import kotlin.math.floor

class Base_Glance : GlanceAppWidget() {
    override val sizeMode: SizeMode
        get() = SizeMode.Exact

    var smallerButtons:Boolean = false
    val defaultColor = ColorProvider(Color.Black, Color.White)
    val textSizeDefault = 24

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Standard is 192 x 225 dp for 3 buses at 24 sp with max 16 chars (15 for safety)
        val busList = getMemoryList(context, id)
        provideContent {
            val standard = DpSize(192.dp, 225.dp)
            val size = LocalSize.current
            val ratio = DpSize((size.width / standard.width).dp, (size.height / standard.height).dp)
            var scale = setScale(busList, ratio, size)
            if(busList.isEmpty()){scale = 1f}
            val changePadding = ratio.width>ratio.height


            Log.d("WidgetSize", "Current: width=${size.width.value}dp, height=${size.height.value}dp")

            GlanceTheme {
                Scaffold(
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.drawable.app_widget_background),
                            title = "Welcome, nigger"
                        )
                    }
                ) {
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
                                for (j in itemCount until currentTarget) {
                                    val bus = busList[j].bus
                                    val arrivals = busList[j].arrivals
                                    itemCount++
                                    Row {
                                        Text(
                                            text = bus.name,
                                            style = TextStyle(fontSize = textSizeDefault.spScaled(scale)),
                                            modifier = GlanceModifier.clickable(actionRunCallback<PopUpButton>(
                                                parameters = actionParametersOf(ActionParameters.Key<String>("vehicleType") to getTypeName(context, bus))
                                            ))
                                        )
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
                                                                ActionParameters.Key<String>("isLast") to arrivals[index].isLastStation.toString())
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
                            }
                        }
                    }

                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val buttonsPadding = 4
                        val buttonScale = if(!smallerButtons){scale * 0.9f}else{scale*0.6f}
                        var buttonSize = 18.spScaled(buttonScale)
                        if(floor(size.width.value) == floor(standard.width.value) || floor(size.height.value) == floor(standard.height.value)) buttonSize = 18.sp
                        //buttonSize = buttonSize.value.coerceAtMost(18f).sp
                        Row(modifier = GlanceModifier.padding(bottom = buttonsPadding.dpScaled(scale))) {
                            Row(modifier = GlanceModifier.padding(end = 6.dp)) {
                                Button(
                                    text = "Good",
                                    onClick = actionRunCallback<BaseButton>(),
                                    style = TextStyle(fontSize = buttonSize)
                                )
                            }
                            Row(modifier = GlanceModifier.padding(start = 6.dp)) {
                                Button(
                                    text = "Clear",
                                    onClick = actionRunCallback<ActionCallback>(),
                                    style = TextStyle(fontSize = buttonSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMemoryList(context: Context, glanceId: GlanceId): ArrayList<BusEntry> {
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
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

    private fun setScale(busList: List<BusEntry>, Scaling: DpSize, Standard:DpSize): Float {
        val maxChars = 9f
        val maxRows = 3f

        val value = maxChars * Scaling.width.value
        //val maxLength = floor(value)
        var maxStringLength = 1
        busList.forEach { it ->
            val arrivals = it.arrivals.map{it.minutes}
            val curr = it.bus.name.length + arrivals.toString().length - 2 - (2*(arrivals.size-1)) //for "[]", " " and ","
            //Log.d("test", "$arrivals $curr")
            if (curr > maxStringLength) maxStringLength = curr
        }

        val maxItems = (maxRows * ((Standard.height.value/100)-1))
        val curr = 1f / maxItems
        val now = 1f / busList.size

        val xScale = value / maxStringLength
        val yScale = 1 / (curr / now)

        Log.d("comparing", "$xScale $yScale")
        var finalScale = minOf(xScale, yScale)
        val xMax = Scaling.width.value*1.2f
        finalScale = finalScale.coerceAtMost(xMax)
        smallerButtons = abs(xScale-yScale)<0.1f
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
}
