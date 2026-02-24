package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
class EditStationList : ComponentActivity() {


    private var index: Int = -1
    private var list: ArrayList<ListPair> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InputContent()
        }
    }

    @Composable
    private fun InputContent() {
        var updater by remember { mutableLongStateOf(1L) }

        val list = remember(updater) {
            getStationLists().apply {
                add(ListPair("Create a new list", ArrayList()))
            }
        }

        this.list = list

        val launcher = startResultActivity({
            if (index != -1) {
                this.list.removeAt(index)
                val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
                val gson = Gson()
                prefs.edit {
                    putString("StationLists", gson.toJson(this@EditStationList.list))
                    if (this@EditStationList.list.isNotEmpty())
                        putString("activeStationList", this@EditStationList.list[0].name)
                }

                index = -1
            }
            updater++
        })

        MaterialTheme {
            Column {
                SimpleDropdown(list)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.975f)
                        .height(55.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BorderedTextButton("➕") {}
                    BorderedTextButton("➖") {}
                    BorderedTextButton("\uD83D\uDDD1\uFE0F") {
                        val intent = Intent(this@EditStationList, DeleteList::class.java)
                        launcher.launch(intent)
                    }
                }
            }
        }
    }

    private fun getStationLists(): ArrayList<ListPair> {
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("StationLists", null)
        if (listString == null) return arrayListOf()

        val list: ArrayList<ListPair>? =
            gson.fromJson(listString, object : TypeToken<ArrayList<ListPair>>() {}.type)

        return list ?: arrayListOf()
    }

    private fun getActiveStationName(): String {
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        var listString: String? = prefs.getString("activeStationList", null)
        if (listString.isNullOrEmpty()) listString = "no available lists"
        return listString
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleDropdown(list: ArrayList<ListPair>) {
        var selectedOption by remember {
            mutableStateOf(
                if (list.size <= 1) "no available lists" else list[0].name
            )
        }
        if(list.size>1) index = 0

        var expanded by remember { mutableStateOf(false) }
        var updater by remember { mutableLongStateOf(1L) }

        val launcher = startResultActivity({selectedOption = getActiveStationName()
            list.add(ListPair(selectedOption, ArrayList()))
            val temp = list[list.size-1]
            list[list.size-1] = list[list.size-2]
            list[list.size-2] = temp
            this.list = list
            this.list.removeAt(list.size-1)
            index = this.list.size-1
            updater++})

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Select a list") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                for (i in 0 until list.size) {
                    DropdownMenuItem(
                        text = { Text(list[i].name) },
                        onClick = {
                            expanded = false

                            if (i == list.lastIndex) {
                                val intent = Intent(
                                    this@EditStationList,
                                    AddListStation::class.java
                                )
                                launcher.launch(intent)
                            } else {
                                selectedOption = list[i].name
                                index = i
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun BorderedTextButton(text:String, action:()->Unit) {
        Text(
            text = text,
            color = Color.White,
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

    @Composable
    private fun startResultActivity(pass:()->Unit, fail:()->Unit = {}): ManagedActivityResultLauncher<Intent, ActivityResult> {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val success =
                    result.data?.getBooleanExtra("success", false) ?: false
                if (success) {
                    pass()
                }
                else{
                    fail()
                }
            }
            else if(result.resultCode == RESULT_CANCELED){
                fail()
            }
        }
        return launcher
    }
}