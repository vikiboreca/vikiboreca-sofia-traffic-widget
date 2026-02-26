package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditStationList: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        val updater = WidgetUpdater(SelectorGlance::class.java)
        setContent{
            Input()
//add a way to change the active list
            BackHandler {
                lifecycleScope.launch(Dispatchers.Default) {
                    updater.updateWidget(this@EditStationList)
                    finish()
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    @Composable
    private fun Input() {
        var list by remember { mutableStateOf(getPureStationLists().apply { add(ListPair("Create a new list", ArrayList())) }) }
        var justDeleted by remember { mutableStateOf(false) }
        var activeIndex by remember { mutableIntStateOf(if (list.isNotEmpty()) 0 else -1) }
        var changeName by remember { mutableStateOf("") }

        val launcher1 = startResultActivity({}, {}, )
        {
            intent->
            val gson = Gson()
            val name = intent?.getStringExtra("list") ?: ""
            list = gson.fromJson(name, object : TypeToken<ArrayList<ListPair>>() {}.type)
        }
        val launcher2 = startResultActivity({}, {}, {})
        val launcher3 = startResultActivity(
            pass = {
                if (activeIndex != -1 && activeIndex < list.size) {
                    list.removeAt(activeIndex)
                    justDeleted = true
                    activeIndex = if (list.isNotEmpty()) 0 else -1
                    changeName = "select a station"
                    savePureList(list)
                    list = ArrayList(list)
                }
            },
            fail = {},
            onExtra = null
        )

        MaterialTheme {
            UI(
                list = list,
                justDeleted = justDeleted,
                activeIndex = activeIndex,
                onSelectIndex = { index ->
                    activeIndex = index
                    justDeleted = false
                    changeName = ""
                },
                {l->list = l ?: ArrayList(list) },
                changeName = changeName,
                launcher1, launcher2, launcher3
            )
        }
    }

    @Composable
    private fun UI(
        list: ArrayList<ListPair>,
        justDeleted: Boolean,
        activeIndex: Int,
        onSelectIndex: (Int) -> Unit,
        updateList:(ArrayList<ListPair>?)->Unit,
        changeName:String,
        vararg launchers: ManagedActivityResultLauncher<Intent, ActivityResult>,
    ) {
        Column {
            SimpleDropdown(list, onClick = onSelectIndex, updateList, changeName)

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.975f)
                    .height(55.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BorderedTextButton("➕") {
                    if(!justDeleted){
                        val intent = Intent(this@EditStationList, AddStationActivity::class.java)
                        val gson = Gson()
                        val json = gson.toJson(list)
                        intent.putExtra("listEditStation", json)
                        intent.putExtra("indexEditStation", activeIndex)
                        launchers[0].launch(intent)
                    }
                }
                BorderedTextButton("➖") {
                    //TODO(code code code)
                    //launchers[1].launch(intent)
                }
                BorderedTextButton("\uD83D\uDDD1\uFE0F") {
                    if (!justDeleted && activeIndex != -1) {
                        val intent = Intent(this@EditStationList, DeleteList::class.java)
                        launchers[2].launch(intent)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleDropdown(list: ArrayList<ListPair>, onClick:(Int)->Unit, updateList:(ArrayList<ListPair>?)->Unit, changeName:String) {
        var selectedOption by remember{
            mutableStateOf(
                if (list.size <= 1) "no available lists" else list[0].name
            )
        }

        LaunchedEffect(changeName) {
            if(changeName.isNotEmpty()){
                selectedOption = changeName
            }
        }

        var expanded by remember { mutableStateOf(false) }

        val launcher = startResultActivity({ updateList(getStationLists()); onClick(list.size-1)}, {},
            { intent->
                val name = intent?.getStringExtra("name") ?: ""
                selectedOption = name
            })

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
                                onClick(i)
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
    private fun startResultActivity(pass:()->Unit, fail:()->Unit = {}, onExtra: ((Intent?) -> Unit)? = null): ManagedActivityResultLauncher<Intent, ActivityResult> {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val success =
                    result.data?.getBooleanExtra("success", false) ?: false
                if (success) {
                    onExtra?.invoke(result.data)
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
    private fun getStationLists(): ArrayList<ListPair> {
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("StationLists", null)
        if (listString == null) return arrayListOf()

        val list: ArrayList<ListPair>? =
            gson.fromJson(listString, object : TypeToken<ArrayList<ListPair>>() {}.type)
        return list ?: arrayListOf()
    }
    private fun getPureStationLists(): ArrayList<ListPair> {
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val listString = prefs.getString("pureStationLists", null)
        if (listString == null) return arrayListOf()

        val list: ArrayList<ListPair>? =
            gson.fromJson(listString, object : TypeToken<ArrayList<ListPair>>() {}.type)


        return list ?: arrayListOf()
    }

    private fun removeList(){
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{
            remove("StationLists")
            remove("pureStationLists")
        }
    }
    private fun savePureList(list:ArrayList<ListPair>){
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        val list2 = ArrayList(list)
        list2.removeAt(list.size-1)
        prefs.edit{
            putString("pureStationLists", gson.toJson(list2))
        }
    }

}
