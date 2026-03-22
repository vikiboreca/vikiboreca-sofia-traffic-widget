package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class TransitMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TransitMap()
        }
    }
}