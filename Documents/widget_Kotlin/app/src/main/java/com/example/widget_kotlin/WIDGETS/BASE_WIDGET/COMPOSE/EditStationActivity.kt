package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER.BaseButton
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.FILTERER.FiltererGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF.BaseGlance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditStationActivity: ComponentActivity() {
    val selectorUpdater = WidgetUpdater(SelectorGlance::class.java)
    val baseUpdater = WidgetUpdater(BaseGlance::class.java)

    val filterUpdater = WidgetUpdater(FiltererGlance::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)

        onBackPressedDispatcher.addCallback(this) {
            //override to do nothing
        }
        setContent{
            ContentScreen()
        }
    }
    @Composable
    private fun ContentScreen(){
        MaterialTheme {
            val editStation = getStationPair()
            Log.d("fuck", editStation.toString())
            var advancedStation by remember {
                mutableStateOf(getStationPairAdvanced())
            }
            var station by remember{
                mutableStateOf(advancedStation.current)
            }

            val launcher1 = ActivityStarter.startResultActivity({}, {},{
                intent->
                val s = intent?.getStringExtra("counter")
                val p:StationPair = Gson().fromJson(s, object : TypeToken<StationPair>() {}.type)
                advancedStation.counter = p
                advancedStation.current = p
                station = p
            })

            val launcher2 = ActivityStarter.startResultActivity({},{},{
                    intent->
                val s = intent?.getStringExtra("counter")
                val p:StationPair = Gson().fromJson(s, object : TypeToken<StationPair>() {}.type)
                if(advancedStation.current.ID == advancedStation.original.ID){
                    advancedStation.original = p
                }
                else{
                    advancedStation.counter = p
                }
                advancedStation.current = p
                station = p
            })

            if(editStation!=null){
                UIContent(station, advancedStation,{advancedStation.switchStations(); station = advancedStation.current}, launcher1, launcher2)
            }
        }
    }

    @Composable
    private fun UIContent(stationPair: StationPair, advanced: StationPairAdvanced,switch:()->Unit, vararg launchers: ManagedActivityResultLauncher<Intent, ActivityResult>) {

        Column {
            DropDown(stationPair)
            {
                switch()
            }
            Row(modifier = Modifier
                .fillMaxWidth(0.975f)
                .height(55.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically)
            {

                BorderedTextButton("⚙\uFE0F")
                {
                    val intent:Intent;
                    if(isEmpty(stationPair))
                    {
                        intent = AddStationActivity.createActivity(this@EditStationActivity, advanced.original.ID)
                        launchers[0].launch(intent)
                    }
                    else{
                        intent = Intent(this@EditStationActivity, RealEditStation::class.java)
                        val original = stationPair.ID == advanced.original.ID
                        intent.apply { putExtra("ID", stationPair.ID); putExtra("Name", stationPair.Name); putExtra("counter", !original) }
                        launchers[1].launch(intent)
                    }

                }

                BorderedTextButton("✔\uFE0F")
                {
                    if(!isEmpty(stationPair)){
                        exit(advanced, stationPair)
                    }
                    else{
                        Toast.makeText(this@EditStationActivity, "The station is null", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DropDown(pair: StationPair, switch:()->Unit){

        var selectedOption by remember (pair){
            mutableStateOf(pair.Name)
        }

        LaunchedEffect(pair) {
            selectedOption = pair.Name
        }

        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Active station") },
            trailingIcon = {
                TextButton(onClick = {switch();})
                {
                    Text("⟳", style = TextStyle(fontWeight = FontWeight.Bold, color = Color.Black))
                }
            }
        )
    }

    @Composable
    private fun BorderedTextButton(text:String, action:()->Unit) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp,
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
        list.forEach {
            it -> Log.d("fuck", it.original.ID + " " + it.counter.ID)
        }
        val advanced =  list.stream().filter { it -> it.original.ID == pair?.ID || it.counter.ID == pair?.ID}.findFirst()
        return advanced.get()
    }

    private fun save(context: Context, pairAdvanced: StationPairAdvanced, pair: StationPair){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("PairList", "") ?: ""
        val list:ArrayList<StationPairAdvanced> = gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        val index = list.indexOfFirst { it.original == pairAdvanced.original }
        if(index!=-1) list[index] = pairAdvanced

        val listString2 = prefs.getString("pureStationLists", "") ?: ""
        val list2:ArrayList<ListPair> = gson.fromJson(listString2, object : TypeToken<ArrayList<ListPair>>() {}.type)
        outer@ for(i in 0 until list2.size){
            for(j in 0 until list2[i].list.size){
                if(pairAdvanced.original.ID == list2[i].list[j].original.ID){
                    list2[i].list[j] = pairAdvanced
                    break@outer
                }
            }
        }

        prefs.edit{
            putString("PairList", gson.toJson(list))
            putString("pureStationLists", gson.toJson(list2))
            putString("activeStation", gson.toJson(pairAdvanced))
        }
    }

    private fun exit(advanced: StationPairAdvanced, pair: StationPair){
        save(this@EditStationActivity, advanced, pair)
        lifecycleScope.launch(Dispatchers.Default) {
            val list3 = BaseButton().getTypes(this@EditStationActivity, advanced.current.ID)
            saveTypes(this@EditStationActivity, list3)
            selectorUpdater.updateWidget(this@EditStationActivity)
            baseUpdater.updateWidget(this@EditStationActivity)
            filterUpdater.updateWidget(this@EditStationActivity)
            finish()
        }
    }

    private fun isEmpty(station: StationPair):Boolean{
        return station.ID == "null" || station.Name == "null"
    }

    private fun saveTypes(context: Context, list:ArrayList<Int>?){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)

        prefs.edit{
            if(list!=null) putString("currentTypes", Gson().toJson(list))
            else putString("currentTypes", "")
        }
    }
}