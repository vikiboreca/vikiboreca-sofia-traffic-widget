package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog



class AddStationActivity: ComponentActivity() {
    var ID:String = ""
    var Name:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            InputScreen()
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun InputScreen(){
        var text by remember { mutableStateOf("") }
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
                        label = {Text("Type")},
                        modifier = Modifier.wrapContentSize()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if(!ID.isEmpty()){
                            if(ValidID(text)){
                                ID = text
                                text = ""
                            }
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
    private fun ValidID(ID:String):Boolean{
        if(ID.length!=4) return false
        ID.forEach { it ->
            if(!it.isDigit()) return false
        }
        return true
    }
}