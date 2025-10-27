package BACKEND.DATA

import android.util.Log
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus

data class FullBus(
    val id:Int,
    val name:String,
    val ext_id:String,
    val type:Int,
    val color:String,
    val icon:String,
    val route_name:String,
    val route_id:Int,
    val route_ext_id:String,
    val st_name:String,
    val st_name_en:String,
    val st_code:String,
    val details:List<BusDetails>,
    val last_stop:String
) {
    fun toSimpleBus(): Bus {
        val list = details.map { it.t }.toCollection(ArrayList())
        return Bus(name, type, list, stationSplitter(route_name))
    }

    private fun stationSplitter(route:String):String{
        val parts = route.split("\\s-\\s".toRegex())
        val last = parts[parts.size-1]
        return last
    }
}