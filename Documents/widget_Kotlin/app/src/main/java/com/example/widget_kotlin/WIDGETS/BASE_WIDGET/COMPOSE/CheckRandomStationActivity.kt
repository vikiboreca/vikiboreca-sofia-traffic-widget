package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import BACKEND.Rest.ScrapperController
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckRandomStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var glanceIDString = intent?.extras?.getString("glanceId")
        if(glanceIDString.isNullOrEmpty()) glanceIDString = "impossible"
        setContent{
            SetContent(glanceIDString)
        }
    }

    @Composable
    private fun SetContent(glanceIDString:String){
        MaterialTheme {
            UIContent(glanceIDString)
        }
    }
    @Composable
    private fun UIContent(glanceIDString:String){

        var ID by remember {mutableStateOf("")}
        var text by remember {mutableStateOf("")}
        var label by remember{mutableStateOf("Type Station ID")}
        var error by remember {mutableStateOf(false)}

        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            OutlinedTextField(
                value = text,
                onValueChange = {text = it},
                label = {Text(label)},
                modifier = Modifier.wrapContentSize(),
                isError = error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    IsIDValid(
                        text,
                        onValid = { saveID ->
                            ID = saveID
                            endActivity(this@CheckRandomStationActivity,ID, glanceIDString, funny = {text = "thanks k"; error = false})
                        },
                        onError = {
                            error = true
                            label = "Insert a correct ID"
                        }
                    )
                }
            ){
                Text("Click me")
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
}