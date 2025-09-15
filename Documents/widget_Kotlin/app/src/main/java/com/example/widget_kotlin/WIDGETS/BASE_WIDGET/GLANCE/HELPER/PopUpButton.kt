package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.HELPER

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class PopUpButton : ActionCallback{
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        var value = parameters[ActionParameters.Key<String>("nigga")]
        if(value.isNullOrEmpty()) value = "error"
        Log.d("test", value)
    }
}