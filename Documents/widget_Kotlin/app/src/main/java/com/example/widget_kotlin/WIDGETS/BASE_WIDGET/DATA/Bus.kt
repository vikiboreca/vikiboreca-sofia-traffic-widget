package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA

import BACKEND.DATA.FullBus
import java.util.Objects

data class Bus(
    val name: String,
    val type: Int,
    var arriveTimes: ArrayList<Int>,
    val lastStop: String
    )
{

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bus) return false

        return name == other.name && type == other.type
    }

    override fun hashCode(): Int {
        return Objects.hash(name, type)
    }
}