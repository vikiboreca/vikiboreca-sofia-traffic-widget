package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps.TransitMapActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
class LoginDecider: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arrival = intent?.getStringExtra("arrival").toString()

        setContent{
            var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
            if (isLoggedIn) {
                startActivity(arrival)
                finish()
            } else {
                loginBody().LoginScreen {
                    startActivity(arrival)
                    finish()
                }
            }
        }
    }


    private fun startActivity(arrival:String){
        val intent = Intent(this@LoginDecider, TransitMapActivity::class.java)
            .apply{
                putExtra("arrival", arrival)
            }
        startActivity(intent)
    }
}