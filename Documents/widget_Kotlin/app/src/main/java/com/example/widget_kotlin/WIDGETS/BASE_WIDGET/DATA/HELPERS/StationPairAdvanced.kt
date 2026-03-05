package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS

data class StationPairAdvanced(var original: StationPair) {
    var counter: StationPair = StationPair("null", "null")
    var current: StationPair = original

    fun switchStations(){
        when(current){
            original->current = counter
            counter->current = original
        }
    }
}