package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun InputScreen(){
        var ID by remember {mutableStateOf("")}
        var StationLabel by remember {mutableStateOf("")}
        var text by remember {mutableStateOf("")}
            MaterialTheme {
                Column(
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    val labelExtra = if(ID.isEmpty()){"Station ID"}else{"Station Name"}
                    OutlinedTextField(
                        value = text,
                        onValueChange = {text = it},
                        label = {Text("Type $labelExtra")},
                        modifier = Modifier.wrapContentSize()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if(ID.isEmpty()){
                            //IsIDValid(text)
                        }
                        else{

                        }
                    })
                    {
                        Text("Click me")
                    }
                }
            }
    }
    private fun IsIDValid(id:String, onValid:(String)->Unit, onError:()->Unit){
        if(id.length!=4) runOnUiThread { onError() }
        id.forEach { it ->
            if(!it.isDigit()) runOnUiThread { onError() }
        }
        ReceiveData(id, object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { onError() }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body.string()
                runOnUiThread {
                    if (response.code == 200 && body.isNotEmpty()) {
                        onValid(id)
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
}