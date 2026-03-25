package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.google.gson.Gson

class TransitMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arrival = Gson().fromJson(intent?.getStringExtra("arrival").toString(), ArriveTime::class.java)
        val context = this@TransitMapActivity
        setContent {
            TransitMap(context, arrival)
        }
    }
}