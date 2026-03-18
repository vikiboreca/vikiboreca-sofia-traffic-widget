package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
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
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.Filter
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.WidgetUpdater
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.FILTERER.FiltererGlance
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR.SelectorGlance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditStationList: ComponentActivity() {

    val updater = WidgetUpdater(SelectorGlance::class.java)
    val filterUpdater = WidgetUpdater(FiltererGlance::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        onBackPressedDispatcher.addCallback(this) {
            //override to do nothing
        }
        setContent{
            //removeList()
            Input()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    @Composable
    private fun Input() {
        var list by remember { mutableStateOf(getPureStationLists().apply { add(ListPair("Create a new list", ArrayList())) }) }
        var justDeleted by remember { mutableStateOf(false) }
        var activeIndex by remember { mutableIntStateOf(if (list.size>1) 0 else -1) }
        var changeName by remember { mutableStateOf("") }

        val launcher1 = ActivityStarter.startResultActivity(pass = {}, fail = {}, onExtra =
            {
                intent->
                val gson = Gson()
                val name = intent?.getStringExtra("list") ?: ""
                list = gson.fromJson(name, object : TypeToken<ArrayList<ListPair>>() {}.type)
                savePureList(list)
            })
        val launcher2 = ActivityStarter.startResultActivity({}, {},
            {
                intent->
                val gson = Gson()
                val name = intent?.getStringExtra("AdvancedStationList") ?: ""
                val list2:ArrayList<StationPairAdvanced> = gson.fromJson(name, object: TypeToken<ArrayList<StationPairAdvanced>>(){}.type)
                list[activeIndex].list = list2
                list = ArrayList(list)
                savePureList(list)
            }
        )
        val launcher3 = ActivityStarter.startResultActivity(
            pass = {
                if (activeIndex != -1 && activeIndex < list.size) {
                    val deleteList = list[activeIndex]
                    deleteListTypes(deleteList)

                    list.removeAt(activeIndex)
                    justDeleted = true
                    activeIndex = if (list.size>1) 0 else -1
                    changeName = if(list.size>1) "select a station" else "no available stations"
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
        vararg launchers: ManagedActivityResultLauncher<Intent, ActivityResult>
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
                    if(!justDeleted && activeIndex!=-1 && list[activeIndex].list.size<20){
                        val gson = Gson()
                        val json = gson.toJson(list)
                        val intent = AddStationActivity.createActivity(this@EditStationList, json, activeIndex, "")
                        launchers[0].launch(intent)
                    }
                    else{
                        if(!justDeleted && list[activeIndex].list.size>=20){
                            Toast.makeText(this@EditStationList, "Max 20 stations", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                BorderedTextButton("➖") {
                    if(!justDeleted && activeIndex!=-1 && list[activeIndex].list.isNotEmpty()){
                        val intent = Intent(this@EditStationList, RemoveStationActivity::class.java)
                        val gson = Gson()
                        val json = gson.toJson(list)
                        intent.putExtra("listEditStation", json)
                        intent.putExtra("indexEditStation", activeIndex)
                        launchers[1].launch(intent)
                    }
                    else{
                        if(!justDeleted && list[activeIndex].list.isEmpty()){
                            Toast.makeText(this@EditStationList, "You don't have stations to delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                BorderedTextButton("\uD83D\uDDD1\uFE0F") {
                    if (!justDeleted && activeIndex != -1) {
                        val intent = AcceptActivity.createActivity(this@EditStationList, "Delete list", "Are you sure you want to delete this list", "Cancel", "Delete")
                        launchers[2].launch(intent)
                    }
                }

                BorderedTextButton("✔\uFE0F") {
                    if(!justDeleted && activeIndex != -1){
                        saveActiveList(list, activeIndex)
                        exit()
                    }
                    else{
                        if(list.size <= 1){
                            Toast.makeText(this@EditStationList, "You need to have a list to exit", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this@EditStationList, "Select a list", Toast.LENGTH_SHORT).show()
                        }
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

        val launcher = ActivityStarter.startResultActivity({ updateList(getStationLists()); onClick(list.size-1)}, {},
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
            fontSize = 18.sp,
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

    private fun saveActiveList(list:ArrayList<ListPair>, index:Int){
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        prefs.edit{
            putString("PairListName", list[index].name)
            putString("PairList", gson.toJson(list[index].list))
        }
    }
    private fun saveActiveList(){
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()
        prefs.edit{
            putString("PairListName", "Create a new list")
            putString("PairList", null)
        }
    }

    private fun exit (){
        lifecycleScope.launch(Dispatchers.Default) {
            updater.updateWidget(this@EditStationList)
            filterUpdater.updateWidget(this@EditStationList)
            finish()
        }
    }

    private fun getList2(context: Context):ArrayList<Filter>{
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        val listString = prefs.getString("filterList", "")?:""
        if(listString.isEmpty()) return ArrayList()

        return Gson().fromJson(listString, object:TypeToken<ArrayList<Filter>>(){}.type)
    }
    private fun saveList2(context:Context, list:ArrayList<Filter>){
        val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
        prefs.edit{
            putString("filterList", Gson().toJson(list))
        }
    }
    private fun removeStationTypes(context: Context, advanced: StationPairAdvanced?){
        val list = getList2(context)
        if(advanced==null) return
        if(advanced.counter.ID!="null"){
            list.filter { it-> it.id!=advanced.original.ID && it.id!=advanced.counter.ID }
        }
        else{
            list.filter{it-> it.id!=advanced.original.ID}
        }
        saveList2(context, list)
    }
    private fun deleteListTypes(pair:ListPair){
        pair.list.forEach { it->
            removeStationTypes(this@EditStationList, it)
        }
    }
}
