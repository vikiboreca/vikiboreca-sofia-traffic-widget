package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle


class PopUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = getTextItems(getSharedPreferences("bus_widget", Context.MODE_PRIVATE))
        val paintRed = list.last() == "true"
        val text = getText(list, paintRed)
        setContent {
            MaterialTheme{
                Text(text)
            }
        }
    }

    private fun getTextItems(prefs: SharedPreferences):ArrayList<String>{
        val list = ArrayList<String>()
        val count = prefs.getInt("popTextCount", 0)
        for(i in 0 until count){
            var word = prefs.getString("popText$i", "error")
            if(!word.isNullOrEmpty()) list.add(word)
        }
        return list
    }

    private fun getText(list:ArrayList<String>, paintRed:Boolean):String{
        if(paintRed){
            val last = list[list.size-2]
            list.removeAt(list.size-1)
            val parts = last.split(":")
            //withStyle(SpanStyle(color = Color.Red)){
              //  append(parts[parts.size-1])
            //}
        }
        return list.joinToString(separator = "\n")
    }
}