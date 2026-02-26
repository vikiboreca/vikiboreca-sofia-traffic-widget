package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import BACKEND.Rest.ScrapperController
import androidx.core.content.edit
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF.BaseGlance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddStationActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = getStationList()
        val index = intent.getIntExtra("indexEditStation", -1)
        Log.d("fuck", list[index].name)
        setContent{
            InputScreen()
        }
    }
    @Composable
    private fun InputScreen(){
        var ID by remember {mutableStateOf("")}
        var StationLabel by remember {mutableStateOf("")}
        var text by remember {mutableStateOf("")}
        var label by remember{mutableStateOf("Type Station ID")}
        var error by remember {mutableStateOf(false)}

        

            MaterialTheme {
                Column(
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {text = it},
                        label = {Text(label)},
                        modifier = Modifier.wrapContentSize(),
                        isError = error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if(ID.isEmpty()){
                            IsIDValid(
                                text,
                                onValid = { saveID ->
                                    ID = saveID; text = ""; error = false
                                    label = "Type Station Name"
                                },
                                onError = {
                                    error = true
                                    label = "Insert a correct ID"
                                }
                            )
                        }
                        else{
                            if(!text.isEmpty() && !listHasName(text)){
                                StationLabel = text
                                saveToPreferences(ID, StationLabel)
                                finish()
                            }
                            else{
                                error = true
                                label = "Insert a correct name"
                            }
                        }
                    })
                    {
                        Text("Click me")
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


    private fun saveToPreferences(ID:String, Name:String){
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val listString = prefs.getString("PairList", null)
        val pair = StationPairAdvanced(StationPair(ID, Name))
        var list:ArrayList<StationPairAdvanced> = ArrayList()
        if(!listString.isNullOrEmpty()){
            list = gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        }
        list.add(pair)
        val listSave = gson.toJson(list)
        prefs.edit{
            putString("PairList", listSave)
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
    private fun listHasName(Name:String):Boolean {
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("PairList", null)
        if (listString.isNullOrEmpty()) return false
        val list: ArrayList<StationPairAdvanced> =
            gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        val listOriginals = list.map{it.original}
        val listIds = listOriginals.map{it.Name}
        return listIds.contains(Name)
    }

    private fun getStationList():ArrayList<ListPair>{
        val gson = Gson()

        val list: ArrayList<ListPair> =
            gson.fromJson(
                intent.getStringExtra("listEditStation"),
                object : TypeToken<ArrayList<ListPair>>() {}.type
            )
        return list;
    }
}