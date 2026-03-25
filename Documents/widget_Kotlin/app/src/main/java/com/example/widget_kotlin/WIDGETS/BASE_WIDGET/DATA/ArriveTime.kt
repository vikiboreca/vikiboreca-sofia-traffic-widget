package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA

data class ArriveTime(val minutes:Int, var isLastStation: Boolean, var lastStation: String) {
    var realLastStation:String = "undefined"

    var prevehicleID:String = "null"
    var aftervehicleID:String = "null"
    var vehicleID:String = "null"
    var tripID:String = "null"
}