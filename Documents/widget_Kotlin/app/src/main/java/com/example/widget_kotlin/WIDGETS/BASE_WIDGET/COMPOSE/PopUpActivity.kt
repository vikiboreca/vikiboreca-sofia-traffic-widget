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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


class PopUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = getTextItems(getSharedPreferences("bus_widget", MODE_PRIVATE))
        val paintRed = list[list.size-1] == "false"
        if(list.size>1) list.removeAt(list.size-1)
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
            val word = prefs.getString("popText$i", "error")
            if(!word.isNullOrEmpty()&&word!="error") list.add(word)
        }
        return list
    }

    private fun getText(list: List<String>, paintRed: Boolean): AnnotatedString {
        return buildAnnotatedString {
            list.forEachIndexed { index, string ->
                if (paintRed && index == list.lastIndex) {
                    val parts = string.split(":")
                    if (parts.size == 2) {
                        append("${parts[0]}: ")
                        withStyle(style = SpanStyle(color = Color.Red)) {
                            append(parts[1])
                        }
                    } else {
                        append(string)
                    }
                } else {
                    append(string)
                }

                if (index < list.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    private fun getText(list:ArrayList<String>):String{
        return list.joinToString("\n")
    }
}