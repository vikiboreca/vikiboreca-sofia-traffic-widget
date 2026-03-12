package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.FILTERER

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.Switch
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.EditStationList
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.Filter
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.BaseWidget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.widget_kotlin.R
class FiltererGlance: BaseWidget() {

    private val iconList = listOf(R.drawable.bus, R.drawable.tram, R.drawable.metro, R.drawable.trolley, R.drawable.night_bus)

    @Composable
    override fun UIContent(context: Context, id: GlanceId, prefs: Preferences) {
        super.UIContent(context, id, prefs)
        val list = getList(context)
        val pair = getCurrentStationPair(context)

        Scaffold(
            titleBar = { CustomTitleBar(pair.Name) },
            backgroundColor = Color(0xFFd9e5fc).toColorProvider(),
            content = {Content(context, list, pair.ID)}
        )

    }

    @Composable
    private fun CustomTitleBar(
        text: String
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
    private fun Content(context: Context, list: ArrayList<Int>, id:String){
        val listFilter = getList2(context)
        var listPair:ArrayList<Pair<Int, Boolean>> = ArrayList()
        listFilter.forEach { it->
            if(it.id == id){
                listPair = it.list;
            }
        }
        if(listPair.isEmpty()){
            val f = Filter(id)
            f.initialize(list)
            listFilter.add(f)
            saveList2(context, listFilter)
            listPair = f.list
        }

        Column{
            listPair.forEach { it->
                Row{
                    DisplayIcon(it.first)
                    Switch(it.second,
                        onCheckedChange = {
                            
                        })
                }
            }
        }
    }

    private fun Color.toColorProvider() = ColorProvider(this, this)

    private fun getList(context: Context):ArrayList<Int>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val listString = prefs.getString("currentTypes", "")?:""

        if(listString.isEmpty()) return ArrayList()

        return Gson().fromJson(listString, object:TypeToken<ArrayList<Int>>(){}.type)
    }
    private fun getList2(context: Context):ArrayList<Filter>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val listString = prefs.getString("filterList", "")?:""
        if(listString.isEmpty()) return ArrayList()

        return Gson().fromJson(listString, object:TypeToken<ArrayList<Filter>>(){}.type)
    }
    private fun saveList2(context: Context, list:ArrayList<Filter>){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        prefs.edit{
            putString("filterList", gson.toJson(list))
        }
    }
    private fun getCurrentStationPair(context: Context): StationPair {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")?:"null"
        if (pairTextOriginal == "null") return StationPair("null", "not selected")
        val advanced: StationPairAdvanced? = gson.fromJson(pairTextOriginal, object : TypeToken<StationPairAdvanced>() {}.type)
        return advanced?.current?:StationPair("null", "not selected")
    }


    @Composable
    @GlanceComposable
    private fun DisplayIcon(index:Int){
        Image(
            provider = ImageProvider(iconList[index-1]),
            contentDescription = null,
            modifier = GlanceModifier.size(32.dp)
        )
    }
}