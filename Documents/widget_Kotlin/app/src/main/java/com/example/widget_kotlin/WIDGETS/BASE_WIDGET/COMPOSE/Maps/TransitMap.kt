package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Maps
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

data class BusMarker(
    val id: String,
    val line: String,
    val latLng: LatLng
)

// Temporary fake data — we'll replace this with a real API later
suspend fun fetchBusPositions(): List<BusMarker> {
    delay(500)
    return listOf(
        BusMarker("1", "Bus 9",  LatLng(42.6977, 23.3219)),
        BusMarker("2", "Bus 84", LatLng(42.7010, 23.3300)),
        BusMarker("3", "Tram 1", LatLng(42.6950, 23.3150)),
    )
}

@Composable
fun TransitMap() {
    val sofia = LatLng(42.6977, 23.3219)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sofia, 14f)
    }

    var busPositions by remember { mutableStateOf<List<BusMarker>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isLoading = true
            try {
                busPositions = fetchBusPositions()
            } catch (e: Exception) {
                // handle error
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
                    title = bus.line
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