package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA

data class ArriveTime(val minutes:Int, var isLastStation: Boolean, var lastStation: String) {
    var realLastStation:String = "undefined"
}