package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddListStation: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            Input()
        }
    }

    @Composable
    private fun Input(){
        MaterialTheme{
            var text by remember {mutableStateOf("")}
            var error by remember {mutableStateOf(false)}
            var label by remember {mutableStateOf("Type list name")}
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
                    if(text.isNotEmpty() && canSaveLists(text)){
                        error = false
                        val intent = Intent().apply { putExtra("success", true) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                    else{
                        if(text.isEmpty()){
                            label = "text must have at least one symbol"
                        }
                        else{
                            label = "you already have that list"
                        }
                        error = true
                    }

                })
                {
                    Text("Click me")
                }
            }


        }
    }

    private fun canSaveLists(name:String):Boolean{
        var list:ArrayList<ListPair>? = ArrayList()
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("StationLists", null)

        if(!listString.isNullOrEmpty()){
            list = gson.fromJson(listString, object : TypeToken<ArrayList<ListPair>>() {}.type)
        }
        if(list.isNullOrEmpty()) list = arrayListOf()

        list.forEach { it->
            if(it.name == name) return false
        }

        list.add(ListPair(name, ArrayList<StationPairAdvanced>()))

        prefs.edit{
            putString("StationLists", gson.toJson(list))
            putString("activeStationList", name)
        }

        return true
    }

}