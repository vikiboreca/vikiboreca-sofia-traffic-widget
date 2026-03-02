package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RemoveStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)

        onBackPressedDispatcher.addCallback(this) {
            //override to do nothing
        }

        setContent{
            Input()
        }
    }
    @Composable
    private fun Input(){
        val list = getStationList()
        val index = intent.getIntExtra("indexEditStation", -1)
        var activeList:ArrayList<StationPairAdvanced> by remember { mutableStateOf(list[index].list) }
        var activePair: StationPairAdvanced? by remember {mutableStateOf(null) }
        var changeName by remember {mutableStateOf("")}
        val launcher = startResultActivity({
            activeList.remove(activePair)
            if(!activeList.isEmpty()){
                changeName = activeList[0].original.Name
                activePair = activeList[0]
            }
            else{
                changeName = "no available stations"
                activePair = null
            }
            activeList = ArrayList(activeList)
        }, {}, {})
        UI(activeList, {pair->activePair = pair; changeName = ""}, activePair, launcher, changeName)

    }
    @Composable
    private fun UI(
        list:ArrayList<StationPairAdvanced>,
        onClick:(StationPairAdvanced)->Unit,
        activePair: StationPairAdvanced?,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        changeName: String
        )
    {
        Column{
            DropDown(list, onClick, changeName)
            Row(modifier = Modifier
                .fillMaxWidth(0.975f)
                .height(55.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,)
            {
                BorderedTextButton("\uD83D\uDDD1\uFE0F")
                {
                    if(activePair!=null){
                        val intent = Intent(this@RemoveStationActivity, DeleteActivity::class.java).apply {
                            putExtra(DeleteActivity.TITLE, "Delete station")
                            putExtra(DeleteActivity.MESSAGE, "Are you sure you want to delete this station")
                        }
                        launcher.launch(intent)
                    }
                }
                BorderedTextButton("✔\uFE0F")
                {
                    exit(list)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DropDown(list:ArrayList<StationPairAdvanced>, onClick:(StationPairAdvanced)->Unit, changeName:String){
        var expanded by remember {mutableStateOf(false)}
        var selectedOption by remember {mutableStateOf(
            if(!list.isEmpty()) "not selected" else "no available stations"
        )}

        LaunchedEffect(changeName) {
            if(changeName.isNotEmpty()){
                selectedOption = changeName
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
                label = { Text("Selected station") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ){
                list.forEach { pairAdvanced ->
                    val name = pairAdvanced.original.Name
                    DropdownMenuItem(text = { Text(name) },
                        onClick = {onClick(pairAdvanced); expanded = false; selectedOption = name})
                }
            }
        }
    }
    @Composable
    private fun BorderedTextButton(text:String, action:()->Unit) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp,
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
            Log.d("LAUNCHER_DEBUG", "launcher CREATED")
            Log.d("RESULT_DEBUG", "code=${result.resultCode}")
            Log.d("RESULT_DEBUG", "data=${result.data}")
            Log.d("RESULT_DEBUG", "success=${result.data?.getBooleanExtra("success", false)}")
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
    private fun getStationList():ArrayList<ListPair>{
        val gson = Gson()

        val list: ArrayList<ListPair> =
            gson.fromJson(
                intent.getStringExtra("listEditStation"),
                object : TypeToken<ArrayList<ListPair>>() {}.type
            )
        return list;
    }

    private fun exit(list: ArrayList<StationPairAdvanced>){
        val gson = Gson()

        val intent = Intent().apply { putExtra("success", true); putExtra("AdvancedStationList", gson.toJson(list)) }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}