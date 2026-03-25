package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps
import BACKEND.Rest.ScrapperController
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.Bus
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import com.example.widget_kotlin.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.scale
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap

data class BusMarker(
    val id: String,
    val line: String,
    val latLng: LatLng,
    val iconID: Int,
    val prevStops: List<LatLng>,
    val speed:String
)

suspend fun fetchBusPositions(context: Context, arrival: ArriveTime): List<BusMarker> {
    val bus = getBus(context)
    val tripB = ScrapperController().getBusCoordinates(arrival.vehicleID)
    val list = ScrapperController().getPrevStopsCoordinates(context,arrival, getCurrentStopID(context))
    if(tripB!=null) list[0] = tripB.coords
    Log.d("fuck2", list.size.toString())
    val spd = if(tripB!=null) tripB.speed.toString()+" km/h" else "unknown"
    return listOf(
        BusMarker(bus?.type.toString(),
            bus?.name.toString(),
            tripB?.coords ?: if(list.isNotEmpty()) list[0] else LatLng(0.0, 0.0),
            getIconIdx(bus?.type ?: 3), list, spd)
    )
}

@Composable
fun TransitMap(context: Context, arrival: ArriveTime) {
    Log.d("fuck2", "${arrival.vehicleID} hey ${arrival.tripID}")
    var busStop by remember { mutableStateOf(getStopBus(context)) }
    var busPositions by remember { mutableStateOf<List<BusMarker>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busStop.latLng, 14f)
    }
    LaunchedEffect(Unit) {
        while (true) {
            isLoading = true
            try {
                busPositions = withContext(Dispatchers.IO) {
                    fetchBusPositions(context, arrival) + listOf(busStop)
                }
            } catch (e: Exception) {
                // handle error
                Log.d("fuck2", e.toString())
            } finally {
                isLoading = false
            }
            delay(7_500L)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false
            )
        ) {
            busPositions.forEach { bus ->
                key(bus.latLng, bus.speed){
                    MarkerInfoWindowContent(
                        state = remember{MarkerState(position = bus.latLng)},
                        icon = resizeIcon(context, bus.iconID, 32)
                    ) {
                        Column{
                            Text(text = "Line: ${bus.line}")
                            if(bus.speed.isNotEmpty()) Text(text = "Speed: ${bus.speed}")
                        }
                    }
                    if(bus.prevStops.isNotEmpty()){
                        Polyline(
                            points = bus.prevStops,
                            color = getRouteColor(getBus(context)?.type?:3),
                            width = 12f,
                            jointType = JointType.ROUND
                        )
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}
private fun getBus(context: Context): Bus? {
    val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
    val busString = prefs.getString("bus", "null") ?: "null"
    if(busString == "null") return null;
    return Gson().fromJson(busString, object: TypeToken<Bus>(){}.type)
}

private fun getIconIdx(idx:Int): Int{
    val index = when(idx){
        1 -> R.drawable.bus
        2 -> R.drawable.tram
        4 -> R.drawable.trolley
        5 -> R.drawable.night_bus
        else -> {
            R.drawable.metro
        }
    }
    return index
}
private fun getCurrentCoordinates(context: Context):Pair<Double, Double>{
    val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
    val text = prefs.getString("stopCoordinates", "")?:""
    if(text.isEmpty() || text == "null") return Pair(42.6977, 23.3219)
    return Gson().fromJson(text, object:TypeToken<Pair<Double, Double>>(){}.type)
}
private fun getStopBus(context: Context): BusMarker{
    val stopCoordinates = getCurrentCoordinates(context)
    val stopLatLng = LatLng(stopCoordinates.first, stopCoordinates.second)
    return BusMarker(
        "3", "Vehicle Stop", stopLatLng, getIconIdx(3), listOf(), ""
    )
}
private fun resizeIcon(context: Context, iconRes: Int, sizeDp: Int = 32): BitmapDescriptor {
    val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
    val bitmap = BitmapFactory.decodeResource(context.resources, iconRes)
    val scaled = bitmap.scale(sizePx, sizePx)
    return BitmapDescriptorFactory.fromBitmap(scaled)
}

private fun getCurrentStopID(context: Context):String{
    val prefs = context.getSharedPreferences("bus_widget", MODE_PRIVATE)
    val text = prefs.getString("currentStopID", "")?:""
    return text
}
fun getRouteColor(index: Int): Color {
    return when (index) {
        1 -> Color(0xFF8B0000)
        2 -> Color(0xFFFF8C00)
        4 -> Color(0xFF00008B)
        5 -> Color(0xFF000000)
        else -> Color.Gray
    }
}