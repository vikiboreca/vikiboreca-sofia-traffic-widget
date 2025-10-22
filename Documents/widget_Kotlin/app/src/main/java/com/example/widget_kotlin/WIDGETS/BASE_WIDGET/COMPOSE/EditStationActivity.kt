package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPair
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs
import kotlin.math.roundToInt

class EditStationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            ContentScreen()
        }
    }
    @Composable
    private fun ContentScreen(){
        MaterialTheme {
            val editStation = getStationPair()
            if(editStation!=null){
                UIContent()
            }
        }
    }

    private fun getStationPair(): StationPair?{
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val id:String? = prefs.getString("Chosen Station ID", "")
        val name:String? = prefs.getString("Chosen Station Name", "")

        if(id.isNullOrEmpty() || name.isNullOrEmpty()) return null
        return StationPair(id, name)
    }

    private fun getStationPairAdvanced():StationPairAdvanced{
        val prefs = getSharedPreferences("bus_widget", MODE_PRIVATE)
        val gson = Gson()

        val pair = getStationPair()
        val listString = prefs.getString("PairList", null)
        val list:ArrayList<StationPairAdvanced> = gson.fromJson(listString, object : TypeToken<ArrayList<StationPairAdvanced>>() {}.type)
        val advanced =  list.stream().filter { it -> it.original == pair }.findFirst()
        return advanced.get()
    }

    @Composable
    private fun UIContent(){
        val advancedStation = getStationPairAdvanced()
        Column{
            Text(advancedStation.original.Name)
            DualActionSlider({}, {})
        }
    }

    @Composable
    fun DualActionSlider(onSave: () -> Unit, onDelete: () -> Unit) {
        val trackWidth = 300.dp
        val thumbSize = 60.dp

        var offsetX by remember { mutableStateOf(0f) }
        var actionTriggered by remember { mutableStateOf(false) }

        val maxOffset = with(LocalDensity.current) { (trackWidth / 2 - thumbSize / 2).toPx() }

        val backgroundColor by animateColorAsState(
            when {
                (offsetX > 0) ->
                    {val color = Color(0xFF4CAF50)
                        val alpha = abs(offsetX / (trackWidth.value / 2))
                        color.copy(alpha = alpha)
                } // green
                (offsetX < 0) -> {val color = Color(0xFFF44336)
                    val alpha = abs(offsetX / (trackWidth.value / 2))
                    color.copy(alpha = alpha)
                }
                else -> Color(0xFFDDDDDD) // neutral gray
            },
            label = ""
        )

        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(thumbSize)
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > maxOffset * 0.7 -> {
                                    onSave()
                                    actionTriggered = true
                                }
                                offsetX < -maxOffset * 0.7 -> {
                                    onDelete()
                                    actionTriggered = true
                                }
                            }
                            // Reset after action
                            offsetX = 0f
                            actionTriggered = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxOffset, maxOffset)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Icons/text hints
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑️ Delete", color = Color.White.copy(alpha = 0.8f))
                Text("Save ✅", color = Color.White.copy(alpha = 0.8f))
            }

            // Thumb handle
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        offsetX > 0 -> "💾"
                        offsetX < 0 -> "🗑️"
                        else -> "↔️"
                    },
                    fontSize = 22.sp
                )
            }
        }
    }


}