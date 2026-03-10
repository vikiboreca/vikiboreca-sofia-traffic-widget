package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import BACKEND.Rest.ScrapperController
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class RealEditStation: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent?.getStringExtra("ID") ?: ""
        val name = intent?.getStringExtra("Name") ?: ""
        val counter = intent?.getBooleanExtra("counter", false) ?: false
        Log.d("fuck", "$id $name")
        setContent{
            Input(id, name, counter)
        }
    }
    @Composable
    private fun Input(ID:String, Name:String, counter:Boolean){
        var id by remember{ mutableStateOf(ID) }
        var name by remember { mutableStateOf(Name) }

        val launcher = ActivityStarter.startResultActivity({
            val intent = Intent().apply { putExtra("success", true); putExtra("counter", Gson().toJson(
                StationPair("null", "null"))) }
            setResult(RESULT_OK, intent)
            finish()
        }, {}, {})

        MaterialTheme{
            UI(id, name, {it->id = it}, {it->name = it}, launcher, counter)
        }
    }
    @Composable
    private fun UI(id:String, name:String, idChange:(it:String)->Unit, nameChange:(it:String)->Unit, launcher:ManagedActivityResultLauncher<Intent, ActivityResult>, counter:Boolean){
        var label1 by remember { mutableStateOf("ID") }
        var label2 by remember { mutableStateOf("Name") }
        var error1 by remember { mutableStateOf(false) }
        var error2 by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally){
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth())
            {
                OutlinedTextField(
                    value = id,
                    onValueChange = { idChange(it) },
                    label = { Text(label1) },
                    isError = error1,
                    readOnly = false,
                    modifier = Modifier.fillMaxWidth(0.3f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceDim,
                        errorTextColor = MaterialTheme.colorScheme.error
                    )
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { nameChange(it) },
                    label = { Text(label2) },
                    isError = error2,
                    readOnly = false,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceDim,
                        errorTextColor = MaterialTheme.colorScheme.error
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()){
                Button(onClick = {
                    if(name.isNotEmpty())
                    {
                        label2 = "Name"; error2 = false;

                        isIDValid(id,
                            onValid = {
                                val pair = StationPair(id, name)
                                val intent = Intent().apply { putExtra("success", true); putExtra("counter", Gson().toJson(pair)) }
                                setResult(RESULT_OK, intent)
                                finish()
                            },
                            onError = {
                                label1 = "Fix"
                                error1 = true;
                                idChange("")
                            })
                    }
                    else{
                        label2 = "Fix"
                        error2 = true
                    }

                }){
                    Text("Save")
                }
                //
                Button(onClick = {
                    if(counter){
                        val intent = AcceptActivity.createActivity(this@RealEditStation, "Delete counter station", "Are you sure you want to delete the counter station", "Decline", "Accept")
                        launcher.launch(intent)
                    }
                    else{
                        Toast.makeText(this@RealEditStation, "You can't delete the original station", Toast.LENGTH_SHORT).show()
                    }

                }){
                    Text("Delete")
                }
            }

        }

    }

    private fun isIDValid(id:String, onValid:()->Unit, onError:()->Unit){
        if(id.isEmpty() || id.length>4) {runOnUiThread { onError();}; return}

        id.forEach { it ->
            if(!it.isDigit()) {runOnUiThread { onError()}; return}
        }
        val scrapperController = ScrapperController()
        lifecycleScope.launch {
            if(scrapperController.isIDValid(id)){
                runOnUiThread { onValid() }
            }
            else{
                runOnUiThread { onError() }
            }
        }
    }
    private fun isPairValid(id:String,name:String, list: ArrayList<StationPairAdvanced>, setProblem:(Int)->Unit){
        if(name.isEmpty()) setProblem(1)
        list.forEach {it->
            if(it.original.ID == id && it.original.Name == name){
                setProblem(2); return;
            }
            if(it.counter.ID == id && it.counter.Name == name){
                setProblem(2); return;
            }
        }
        return setProblem(-1)
    }
}