package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditStationList : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InputContent()
        }
    }

    @Composable
    private fun InputContent() {
        val list = getStationLists().toMutableList()
        list.add(ListPair("Create a new list", ArrayList<StationPairAdvanced>()))

        MaterialTheme {
            Column {
                SimpleDropdown(ArrayList(list))
                Spacer(Modifier.height(20.dp))
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
        var expanded by remember { mutableStateOf(false) }
        var updater by remember { mutableLongStateOf(1L) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val success =
                    result.data?.getBooleanExtra("success", false) ?: false
                if (success) {
                    selectedOption = getActiveStationName()
                    list.add(ListPair(selectedOption, ArrayList<StationPairAdvanced>()))
                    val temp = list[list.size-1]
                    list[list.size-1] = list[list.size-2]
                    list[list.size-2] = temp
                    updater++
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
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
                            }
                        }
                    )
                }
            }
        }
    }
}