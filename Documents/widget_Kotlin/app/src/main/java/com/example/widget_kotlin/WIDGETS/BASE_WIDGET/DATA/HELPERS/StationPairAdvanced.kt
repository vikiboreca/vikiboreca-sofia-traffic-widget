package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS

data class StationPairAdvanced(val original: StationPair) {
    var counter: StationPair? = null
    fun setCounterStation(opp:StationPair){
        counter = opp
    }
}