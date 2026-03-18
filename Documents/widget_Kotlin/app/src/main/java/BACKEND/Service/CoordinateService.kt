package BACKEND.Service

import com.google.transit.realtime.GtfsRealtime
import java.io.InputStream
import java.net.URI
import java.net.URL
class CoordinateService(val busID:String) {
    val url: URL = URI.create("https://gtfs.sofiatraffic.bg/api/v1/vehicle-positions").toURL()

    suspend fun getCoordinates(busID:String):Pair<Double, Double>?{
        val inputStream: InputStream = url.openStream()
        val feed = GtfsRealtime.FeedMessage.parseFrom(inputStream)
        feed.entityList.forEach { entity ->
            
        }
        return null
    }
}