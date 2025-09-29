package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

class SelectorGlance: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme{
                Scaffold {
                    Text("nigger")
                }
            }
        }
    }
}