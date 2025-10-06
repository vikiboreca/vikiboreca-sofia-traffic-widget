package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

class EditStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            ContentScreen()
        }
    }
    @Composable
    private fun ContentScreen(){
        MaterialTheme {

        }
    }
}