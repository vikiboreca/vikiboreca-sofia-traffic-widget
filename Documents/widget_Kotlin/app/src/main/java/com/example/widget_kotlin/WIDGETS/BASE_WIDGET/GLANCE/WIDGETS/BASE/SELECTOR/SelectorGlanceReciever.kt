package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.SELECTOR

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SelectorGlanceReciever : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = SelectorGlance()
}