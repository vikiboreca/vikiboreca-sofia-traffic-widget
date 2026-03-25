package BACKEND.Service

import BACKEND.DATA.Extra.TripResponse
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.transit.realtime.GtfsRealtime
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoordinateService() {

    suspend fun getCoordinates(busID:String):Pair<Float, Float>{
        val url: URL = URI.create("https://gtfs.sofiatraffic.bg/api/v1/vehicle-positions").toURL()
        val inputStream: InputStream = url.openStream()
        val feed = GtfsRealtime.FeedMessage.parseFrom(inputStream)
        feed.entityList.forEach { entity ->
            if(entity.hasVehicle()){
                val vehicle = entity.vehicle
                if(vehicle.vehicle.id!=busID) return@forEach

                return Pair(vehicle.position.latitude, vehicle.position.longitude)
            }
        }
        return Pair(0f, 0f)
    }

    suspend fun getPrevStops(stpoID:String):List<String>{
        val url = "https://api.livetransport.eu/sofia/vehicle/11%2F2684/trip"
        val text = getStringResponse(url)
        val trip: TripResponse = Gson().fromJson(text, object: TypeToken<TripResponse>(){}.type)
        return getStopList(trip, stpoID)
    }

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

    private fun getStopList(trip: TripResponse, stopID:String):ArrayList<String>{
        val list:ArrayList<String> = ArrayList()
        val endIndex = trip.trip.stops.indexOfFirst { it->it.id == stopID }
        Log.d("fuck2", endIndex.toString())
        Log.d("fuck2", trip.trip.stops.toString())
        for(i in trip.nextStop until endIndex){
            list.add(trip.trip.stops[i].id)
        }
        return list
    }
}