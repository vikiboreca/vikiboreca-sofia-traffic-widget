package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import BACKEND.Rest.ScrapperController
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF.BaseGlance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckRandomStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var glanceIDString = intent?.extras?.getString("glanceId")
        if(glanceIDString.isNullOrEmpty()) glanceIDString = "impossible"
        val advanced = getCurrentStationPair(this@CheckRandomStationActivity)
        //Log.d("fuck", advanced?.original?.Name + " " + advanced?.counter?.Name)
        setContent{
            SetContent(glanceIDString, advanced)
        }
    }

    @Composable
    private fun SetContent(glanceIDString:String, advanced: StationPairAdvanced){
        MaterialTheme {
            UIContent(glanceIDString, advanced)
        }
    }
    @Composable
    private fun UIContent(glanceIDString: String, advanced: StationPairAdvanced) {

        var ID by remember { mutableStateOf("") }
        var label by remember { mutableStateOf("Type Station ID") }
        var error by remember { mutableStateOf(false) }

        Column {


            TextField(
                value = ID,
                onValueChange = { ID = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                isError = error
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.975f)
                    .height(55.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                BorderedTextButton("✔\uFE0F") {

                    IsIDValid(
                        ID,
                        onValid = { saveID ->
                            ID = saveID
                            endActivity(
                                this@CheckRandomStationActivity,
                                ID,
                                glanceIDString,
                                funny = {
                                    ID = "thanks k"
                                    error = false
                                    label = "so cool"
                                }
                            )
                        },
                        onError = {
                            label = "Insert a correct ID"
                            error = true
                            ID = ""
                        }
                    )

                }
                if (advanced.counter.ID != "null") {
                    BorderedTextButton("⇄\uFE0F") {
                        endActivity(this@CheckRandomStationActivity, advanced, glanceIDString)
                    }
                }
            }
        }
    }
    private fun IsIDValid(id:String, onValid:(saveID:String)->Unit, onError:()->Unit){
        if(id.isEmpty() || id.length>4) {runOnUiThread { onError();}; return}
        id.forEach { it ->
            if(!it.isDigit()) {runOnUiThread { onError();}; return}
        }
        if(listHasID(id)) {runOnUiThread { onError();}; return}
        val realID = id
        val scrapperController = ScrapperController()
        lifecycleScope.launch {
            if(scrapperController.isIDValid(id)){
                runOnUiThread { onValid(realID) }
            }
            else{
                runOnUiThread { onError() }
            }
        }

    }
    private fun listHasID(ID:String):Boolean{
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("PairList", null)
        if(listString.isNullOrEmpty()) return false
        val list:ArrayList<StationPairAdvanced> = gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        val listOriginals = list.map{it.original}
        val listIds = listOriginals.map{it.ID}
        return listIds.contains(ID)
    }

    private fun endActivity(context:Context,id:String, glanceId: String, funny: () -> Unit){
        val selectUpdate = WidgetUpdater(SelectorGlance::class.java)
        val baseUpdate = WidgetUpdater(BaseGlance::class.java)
        val baseButton = BaseButton()
        saveCurrentStation(context, id)
        lifecycleScope.launch(Dispatchers.Default) {

            baseButton.getResults(context, glanceId)
            baseUpdate.updateWidget(context)
            selectUpdate.updateWidget(context)
            funny()
            withContext(Dispatchers.Main) {
                finish() // closes the current Activity
            }
        }

    }
    private fun endActivity(context: Context, advanced: StationPairAdvanced, glanceID:String){
        advanced.switchStations()
        saveCurrentStation(this@CheckRandomStationActivity, advanced)
        updateList(context, advanced)
        val selectUpdate = WidgetUpdater(SelectorGlance::class.java)
        val baseUpdate = WidgetUpdater(BaseGlance::class.java)
        val baseButton = BaseButton()
        lifecycleScope.launch(Dispatchers.Default) {

            baseButton.getResults(context, glanceID)
            baseUpdate.updateWidget(context)
            selectUpdate.updateWidget(context)
            withContext(Dispatchers.Main) {
                finish() // closes the current Activity
            }
        }
    }
    private fun updateList(context: Context, advanced: StationPairAdvanced){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("PairList", "") ?: ""
        val list:ArrayList<StationPairAdvanced> = gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        val index = list.indexOfFirst { it-> it.original.ID == advanced.original.ID }
        if(index!=-1) list[index] = advanced

        prefs.edit{
            putString("PairList", gson.toJson(list))
        }
    }
    private fun saveCurrentStation(context: Context,id:String){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val pairAdvanced = StationPairAdvanced(StationPair(id, "random"))

        val pairText = gson.toJson(pairAdvanced)

        prefs.edit{
            putString("activeStation", pairText)
        }
        //Log.d("nigger", pairText)
    }

    private fun saveCurrentStation(context: Context, stationPairAdvanced: StationPairAdvanced) {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        prefs.edit{
            putString("activeStation", gson.toJson(stationPairAdvanced))
        }
    }

    private fun getCurrentStationPair(context: Context): StationPairAdvanced {
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val pairTextOriginal = prefs.getString("activeStation", "null")
        if (pairTextOriginal == "null") return StationPairAdvanced(StationPair("null", "null"))
        val advanced: StationPairAdvanced? = gson.fromJson(pairTextOriginal, object : TypeToken<StationPairAdvanced>() {}.type)
        if(advanced == null) return StationPairAdvanced(StationPair("null", "null"))
        return advanced
    }
    @Composable
    private fun BorderedTextButton(text:String, action:()->Unit) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 20.sp,
            style = TextStyle(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(onClick = action)
        )
    }
}