package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps
import BACKEND.Rest.ScrapperController
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.scale

data class BusMarker(
    val id: String,
    val line: String,
    val latLng: LatLng,
    val iconID: Int
)

suspend fun fetchBusPositions(context: Context, vehicleID: String): List<BusMarker> {
    val bus = getBus(context)
    val coords = ScrapperController().getBusCoordinates(vehicleID)
    return listOf(
        BusMarker(bus?.type.toString(),
            bus?.name.toString(),
            LatLng(coords.first.toDouble(), coords.second.toDouble()),
            getIconIdx(bus?.type ?: 3))
    )
}

@Composable
fun TransitMap(vehicleID:String, context: Context) {
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
                    fetchBusPositions(context, vehicleID) + listOf(busStop)
                }
                Log.d("fuck2", busPositions.toString())
            } catch (e: Exception) {
                // handle error
                Log.d("fuck2", e.toString())
            } finally {
                isLoading = false
            }
            delay(15_000L)
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
                Marker(
                    state = MarkerState(position = bus.latLng),
                    title = bus.line,
                    icon = resizeIcon(context, bus.iconID, 32)
                )
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
    val prefs = context.getSharedPreferences("bus_widget", Context.MODE_PRIVATE)
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
    if(text.isEmpty()) return Pair(42.6977, 23.3219)
    return Gson().fromJson(text, object:TypeToken<Pair<Double, Double>>(){}.type)
}
private fun getStopBus(context: Context): BusMarker{
    val stopCoordinates = getCurrentCoordinates(context)
    val stopLatLng = LatLng(stopCoordinates.first, stopCoordinates.second)
    return BusMarker(
        "3", "Vehicle Stop", stopLatLng, getIconIdx(3)
    )
}
private fun resizeIcon(context: Context, iconRes: Int, sizeDp: Int = 32): BitmapDescriptor {
    val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
    val bitmap = BitmapFactory.decodeResource(context.resources, iconRes)
    val scaled = bitmap.scale(sizePx, sizePx)
    return BitmapDescriptorFactory.fromBitmap(scaled)
}