package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.widget_kotlin.R
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Login.LoginDecider
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps.TransitMapActivity
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class PopUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val list = getTextItems(prefs)
        val count = prefs.getInt("popTextCount", 0)
        val paintRed = list[list.size-1] == "false"
        if(count>1) list.removeAt(list.size-1)
        val arrival = getArrival(prefs)
        val text = getText(list, paintRed)

        setContent {
            MaterialTheme{
                Column(verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text)
                    if(arrival!=null && arrival.vehicleID!="null" && arrival.tripID!="null"){
                        DisplayIcon(arrival, this@PopUpActivity)
                    }
                }
            }
        }
    }

    private fun getTextItems(prefs: SharedPreferences):ArrayList<String>{
        val list = ArrayList<String>()
        val count = prefs.getInt("popTextCount", 0)
        for(i in 0 until count){
            val word = prefs.getString("popText$i", "error") ?: "error"
            if(word.isNotEmpty() &&word!="error") list.add(word)
        }
        return list
    }
    private fun getArrival(prefs: SharedPreferences): ArriveTime? {
        val arriveText = prefs.getString("arrival", "null") ?: "null"
        if(arriveText.isEmpty()) return null;
        return Gson().fromJson(arriveText, ArriveTime::class.java)
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
    @Composable
    private fun DisplayIcon(arrival: ArriveTime, context: Context){
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.maps),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(40.dp).height(43.dp)
                .clickable(onClick = {
                    val intent = Intent(this@PopUpActivity, LoginDecider::class.java)
                        .apply{
                            putExtra("arrival", Gson().toJson(arrival))
                        }
                    startActivity(intent)
                }).border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}