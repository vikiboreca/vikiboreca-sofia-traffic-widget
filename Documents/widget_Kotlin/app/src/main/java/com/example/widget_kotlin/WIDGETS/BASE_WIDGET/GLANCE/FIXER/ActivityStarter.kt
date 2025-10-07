package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.PopUpActivity

class ActivityStarter(val activityClass: Class<out ComponentActivity>){
     fun startIntent(context:Context){
        val intent = android.content.Intent(context, activityClass).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
         Log.d("nigger", "startedActivity of type ${activityClass.simpleName}")
    }
}
