package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import BACKEND.Rest.ScrapperController
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch


class AddStationActivity: ComponentActivity() {

    companion object{
        val LIST = "listEditStation"
        val INDEX = "indexEditStation"
        val EXTRA = "extraID"

        fun createActivity(context: Context, list:String, index:Int, extraID:String):Intent{
            val intent = Intent(context, AddStationActivity::class.java)
            intent.putExtra(LIST, list)
            intent.putExtra(INDEX, index)
            intent.putExtra(EXTRA, extraID)
            return intent
        }
        fun createActivity(context: Context, extraID:String):Intent{
            val intent = Intent(context, AddStationActivity::class.java)
            intent.putExtra(LIST, getPureStationLists(context))
            intent.putExtra(INDEX, 0)
            intent.putExtra(EXTRA, extraID)
            return intent
        }

        private fun getPureStationLists(context: Context):String {
            val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
            val gson = Gson()
            return prefs.getString("pureStationLists", "") ?: ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)
        val list = getStationList()
        val index = intent.getIntExtra(INDEX, -1)
        val extraID = intent.getStringExtra(EXTRA) ?: ""
        //Log.d("fuck", list[index].name)
        setContent{
            InputScreen(list, list[index], index, extraID)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    @Composable
    private fun InputScreen(list:ArrayList<ListPair>, pair:ListPair, index:Int, extraID:String){
        var ID by remember {mutableStateOf("")}
        var StationLabel by remember {mutableStateOf("")}
        var text by remember {mutableStateOf("")}
        var label by remember{mutableStateOf("Type Station ID")}
        var error by remember {mutableStateOf(false)}

        var displayContent by remember { mutableIntStateOf(1) }
        var save1 by remember { mutableStateOf({}) }
        val launcher1 = ActivityStarter.startResultActivity(
            pass = {},
            fail = {save1(); finish()},
            onExtra = {intent ->
                val s = intent?.getStringExtra("counter")
                val p:StationPair = Gson().fromJson(s, object : TypeToken<StationPair>() {}.type)
                save1()
                save(p.ID, p.Name, list, pair, index, ID)
                finish()
            }
        )


        val launcher = ActivityStarter.startResultActivity(
            {
                val s = Gson().toJson(list)
                val intent = createActivity(this@AddStationActivity, s, index, ID)
                launcher1.launch(intent)
            },
            {
                save1();
                finish()
            },
            {

            })
        var content2 by remember { mutableStateOf({
            val intent = AcceptActivity.createActivity(this@AddStationActivity, "Full pack?", "Add another counter station", "Decline", "Accept")
            launcher.launch(intent)
        }) }
        var content1 by remember {
            mutableStateOf(@Composable
            {
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
                                text,pair.list,
                                onValid = { saveID ->
                                    ID = saveID; text = ""; error = false
                                    label = "Type Station Name"
                                },
                                onError = {
                                    error = true
                                    label = "Insert a correct ID"
                                },
                                extraID
                            )
                        }
                        else{
                            if(!text.isEmpty() && !listHasName(text, pair.list)){
                                if(extraID.isEmpty()){
                                    StationLabel = text
                                    save1 = {save(ID, StationLabel, list, pair, index, extraID)}
                                    displayContent = 2
                                }
                                else{
                                    val pair = StationPair(ID, text)
                                    val intent = Intent().apply { putExtra("success", true); putExtra("counter", Gson().toJson(pair)) }
                                    setResult(RESULT_OK, intent)
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
            })
        }
            MaterialTheme {
                when(displayContent){
                    1->content1()
                    2->if(extraID.isEmpty()) content2()
                }
            }
    }
    private fun IsIDValid(id:String, list:ArrayList<StationPairAdvanced>, onValid:(saveID:String)->Unit, onError:()->Unit, extraID:String){
        if(id.isEmpty() || id.length>4) {runOnUiThread { onError();}; return}

        id.forEach { it ->
            if(!it.isDigit()) {runOnUiThread { onError();}; return}
        }
        if(listHasID(id, list)) {runOnUiThread { onError();}; return}
        val scrapperController = ScrapperController()
        lifecycleScope.launch {
            if(scrapperController.isIDValid(id)){
                if(extraID.isNotEmpty())
                    if(id == extraID){
                        runOnUiThread { onError();}
                    }
                    else{
                        runOnUiThread { onValid(id);}
                    }
                else{
                    runOnUiThread { onValid(id);}
                }
            }
            else{
                runOnUiThread { onError() }
            }
        }

    }


    private fun save(ID:String, Name:String, list:ArrayList<ListPair>, listPair:ListPair, index:Int, checkerID:String){
        val gson = Gson()
        val pair: StationPairAdvanced

        if(checkerID.isEmpty()){
            pair = StationPairAdvanced(StationPair(ID, Name))
            listPair.list.add(pair)
            list[index] = listPair
        }
        else{
            pair = listPair.list.first { it->it.original.ID == checkerID }
            pair.counter = StationPair(ID, Name)
        }
        val intent = Intent().apply { putExtra("success", true); putExtra("list", gson.toJson(list))}
        setResult(RESULT_OK, intent)
    }

    private fun listHasID(ID:String, list:ArrayList<StationPairAdvanced>):Boolean{
        val listOriginals = list.map{it.original}
        val listIds = listOriginals.map{it.ID}
        return listIds.contains(ID)
    }
    private fun listHasName(Name:String, list:ArrayList<StationPairAdvanced>):Boolean {
        val listOriginals = list.map{it.original}
        val listIds = listOriginals.map{it.Name}
        return listIds.contains(Name)
    }

    private fun getStationList():ArrayList<ListPair>{
        val gson = Gson()
        val listString = intent.getStringExtra(LIST)
        if(listString.isNullOrEmpty()) return ArrayList()
        val list: ArrayList<ListPair> =
            gson.fromJson(
                listString,
                object : TypeToken<ArrayList<ListPair>>() {}.type
            )
        return list;
    }
}