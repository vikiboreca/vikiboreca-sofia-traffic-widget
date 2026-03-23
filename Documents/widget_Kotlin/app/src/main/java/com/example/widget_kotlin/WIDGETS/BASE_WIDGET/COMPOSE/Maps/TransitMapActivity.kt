package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope

class TransitMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vehicleID = intent?.getStringExtra("vehicleID").toString()
        val context = this@TransitMapActivity
        setContent {
            TransitMap(vehicleID, context)
        }
    }
}