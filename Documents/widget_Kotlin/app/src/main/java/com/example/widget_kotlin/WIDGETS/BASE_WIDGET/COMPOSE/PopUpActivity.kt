package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf


class PopUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var test:String? = intent.getStringExtra("test")
        if(test.isNullOrEmpty()) test = "error"
        val a = getTextItems(getSharedPreferences("bus_widget", Context.MODE_PRIVATE))
        setContent {
            MaterialTheme{
                Text(a.toString())
            }
        }
    }

    private fun getTextItems(prefs: SharedPreferences):List<String>{
        val list = ArrayList<String>()
        val count = prefs.getInt("any -1", 0)
        for(i in 0 until count){
            var word = prefs.getString("any $i", "error")
            if(word.isNullOrEmpty()) word = "error"
            list.add(word)
        }
        return list
    }
}