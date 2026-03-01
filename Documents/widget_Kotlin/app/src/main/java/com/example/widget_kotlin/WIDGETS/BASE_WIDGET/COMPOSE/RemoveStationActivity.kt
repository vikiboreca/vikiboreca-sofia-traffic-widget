package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.ListPair
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RemoveStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            Input()
        }
    }
    @Composable
    private fun Input(){
        val list = getStationList()
        val index = intent.getIntExtra("indexEditStation", -1)
        UI(list[index])
    }
    @Composable
    private fun UI(pair:ListPair){

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
}