package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class EditStationButton: ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val pairText = parameters[ActionParameters.Key<String>("StationPair")]
        var ID: String = ""
        var Name: String = ""
        if(!pairText.isNullOrEmpty()){
            val parts = pairText.split("\n")
            ID = parts[0]
            Name = parts[1]
        }
        Log.d("nigger", "$ID $Name")
    }
}