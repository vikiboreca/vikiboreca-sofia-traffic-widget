package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS

data class Filter(val id:String) {
    var list:ArrayList<Pair<Int, Boolean>> = ArrayList()

    fun initialize(l:ArrayList<Int>){
        l.forEach { it->
            list.add(Pair(it, true))
        }
    }
}