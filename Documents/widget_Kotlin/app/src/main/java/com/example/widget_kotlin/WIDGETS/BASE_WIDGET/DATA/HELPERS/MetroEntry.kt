package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS

import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime

data class MetroEntry(val metro: String, val direction: ArrayList<MetroArriveTime>, val oppDirection: ArrayList<MetroArriveTime>) {
}