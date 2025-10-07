package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.core.content.edit
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.EditStationActivity
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER.ActivityStarter

class EditStationButton: ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val pairText = parameters[ActionParameters.Key<String>("StationPair")]
        var ID: String = ""
        var Name: String = ""
        if(!pairText.isNullOrEmpty()){
            val parts = pairText.split("\n")
            ID = parts[0]
            Name = parts[1]
            saveToPreferences(context, ID, Name)
            val starter = ActivityStarter(EditStationActivity::class.java)
            starter.startIntent(context)
        }
    }

    private fun saveToPreferences(context: Context,ID:String, Name:String){
        val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
        prefs.edit{
            putString("Chosen Station ID", ID)
            putString("Chosen Station Name", Name)
        }
    }
}