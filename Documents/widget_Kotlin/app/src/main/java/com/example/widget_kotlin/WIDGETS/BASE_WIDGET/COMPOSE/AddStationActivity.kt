package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import androidx.core.content.edit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SHOWOFF.BaseGlance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException


class AddStationActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            InputScreen()
        }
    }
    @Composable
    private fun InputScreen(){

        val updater = WidgetUpdater(SelectorGlance::class.java)
        val updaterBase = WidgetUpdater(BaseGlance::class.java)
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
                                onValid = {
                                    ID = text; text = ""; error = false
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
                                lifecycleScope.launch(Dispatchers.Default) {
                                    updater.updateWidget(this@AddStationActivity)
                                    finish()
                                }
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
    private fun IsIDValid(id:String, onValid:()->Unit, onError:()->Unit){
        if(id.isEmpty() || id.length>4) {runOnUiThread { onError();}; return}
        id.forEach { it ->
            if(!it.isDigit()) {runOnUiThread { onError();}; return}
        }
        if(listHasID(id)) {runOnUiThread { onError();}; return}

        ReceiveData(id, object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { onError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body.string()
                runOnUiThread {
                    if (response.code == 200 && body.isNotEmpty()) {
                        onValid()
                    } else {
                        onError()
                    }
                }
            }
        })
    }


    private fun ReceiveData(stopID: String, call: Callback) {
        val client = OkHttpClient()
        val url = "http://100.114.8.24:8080/api/scrap"
        val jsonBody = "{\"stop\":\"$stopID\"}".trimIndent()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(call)
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

}