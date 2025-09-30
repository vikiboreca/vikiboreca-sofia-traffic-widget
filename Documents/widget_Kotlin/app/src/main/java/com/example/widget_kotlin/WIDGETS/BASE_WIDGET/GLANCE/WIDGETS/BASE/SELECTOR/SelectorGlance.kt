package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class SelectorGlance: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme{
                Scaffold {
                    Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.Bottom, horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        Row(modifier = GlanceModifier.padding(bottom = 6.dp))
                        {
                            Button(
                                text = "Add station",
                                onClick = actionRunCallback<ActionCallback>(),
                                style = TextStyle(fontSize = 18.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}