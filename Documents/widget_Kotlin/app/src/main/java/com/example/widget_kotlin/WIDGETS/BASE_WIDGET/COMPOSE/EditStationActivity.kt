package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EditStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            ContentScreen()
        }
    }
    @Composable
    private fun ContentScreen(){
        MaterialTheme {
            val editStation = getStationPair()
            var advancedStation by remember {
                mutableStateOf(getStationPairAdvanced())
            }
            var station by remember{
                mutableStateOf(advancedStation.current)
            }
            if(editStation!=null){
                UIContent(station, {advancedStation.switchStations(); station = advancedStation.current})
            }
        }
    }

    private fun getStationPair(): StationPair?{
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val id:String? = prefs.getString("Chosen Station ID", "")
        val name:String? = prefs.getString("Chosen Station Name", "")

        if(id.isNullOrEmpty() || name.isNullOrEmpty()) return null
        return StationPair(id, name)
    }

    private fun getStationPairAdvanced():StationPairAdvanced{
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val pair = getStationPair()
        val listString = prefs.getString("PairList", null)
        val list:ArrayList<StationPairAdvanced> = gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        val advanced =  list.stream().filter { it -> it.original == pair }.findFirst()
        return advanced.get()
    }

    @Composable
    private fun UIContent(stationPair: StationPair, switch:()->Unit) {

        Column {
            Row {
                Text("Current station")
                TextButton({ switch() }) {
                    Text(stationPair.Name)
                }
            }
        }
    }
}