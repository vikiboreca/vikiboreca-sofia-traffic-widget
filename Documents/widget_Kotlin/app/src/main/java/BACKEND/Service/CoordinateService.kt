package BACKEND.Service

import BACKEND.DATA.Extra.TripBus
import BACKEND.DATA.Extra.TripResponse
import android.util.Log
import com.example.widget_kotlin.BuildConfig
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.ArriveTime
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.transit.realtime.GtfsRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoordinateService() {

    //1
    suspend fun getCoordinates(busID:String): TripBus?{
        val url: URL = URI.create("https://gtfs.sofiatraffic.bg/api/v1/vehicle-positions").toURL()
        val inputStream: InputStream = url.openStream()
        val feed = GtfsRealtime.FeedMessage.parseFrom(inputStream)
        feed.entityList.forEach { entity ->
            if(entity.hasVehicle()){
                val vehicle = entity.vehicle
                if(vehicle.vehicle.id!=busID) return@forEach
                val coords = LatLng(vehicle.position.latitude.toDouble(),
                    vehicle.position.longitude.toDouble()
                )
                return TripBus(vehicle.position.speed, coords)
            }
        }
        return null
    }

    //2
    suspend fun getPrevStops(stpoID:String, arrival: ArriveTime):List<String>{
        val url = "https://api.livetransport.eu/sofia/vehicle/${arrival.prevehicleID}%2F${arrival.aftervehicleID}/trip"
        val text = getStringResponse(url)
        val trip: TripResponse = Gson().fromJson(text, object: TypeToken<TripResponse>(){}.type)
        Log.d("fuck3", trip.toString())
        return getStopList(trip, stpoID)
    }

    //2
    private suspend fun getStringResponse(url:String):String =
        suspendCancellableCoroutine { cont->
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        cont.resume(response.body.string())
                    } else {
                        cont.resumeWithException(IOException("Response code ${response.code}"))
                    }
                }
            })
        }

    //2
    private fun getStopList(trip: TripResponse, stopID:String):ArrayList<String>{
        val list:ArrayList<String> = ArrayList()
        val endIndex = trip.trip.stops.indexOfFirst { it->it.id == stopID }

        for(i in (trip.nextStop - 1).coerceAtLeast(0)..endIndex){
            list.add(trip.trip.stops[i].id)
        }
        return list
    }

    //3
    suspend fun fixRoute(points:List<LatLng>):List<LatLng>{
        return withContext(Dispatchers.IO) {
            val origin = points.first()
            val destination = points.last()
            val waypoints = points.drop(1).dropLast(1)
                .joinToString("|") { "${it.latitude},${it.longitude}" }

            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&waypoints=$waypoints" +
                    "&key=${BuildConfig.MAPS_API_KEY}"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val encoded = json
                .getJSONArray("routes")
                .getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")

            decodePolyline(encoded)
        }
    }
    //3
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var shift = 0; var result = 0
            do {
                val b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (encoded[index - 1].code - 63 >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0; result = 0
            do {
                val b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (encoded[index - 1].code - 63 >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            poly.add(LatLng(lat / 1e5, lng / 1e5))
        }
        return poly
    }
}