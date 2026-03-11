package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.FILTERER

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.EditStationList
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.DATA.HELPERS.StationPairAdvanced
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.WIDGETS.BASE.BaseWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FiltererGlance: BaseWidget() {

    @Composable
    override fun UIContent(context: Context, id: GlanceId, prefs: Preferences) {
        super.UIContent(context, id, prefs)


        Scaffold(
            titleBar = { CustomTitleBar("something") },
            backgroundColor = Color(0xFFd9e5fc).toColorProvider(),
            content = {}
        )
    }

    @Composable
    private fun CustomTitleBar(
        text: String
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFafd8f0))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "\uD83D\uDD04",
                style = TextStyle(fontSize = 20.sp, color = ColorProvider(Color.Black, Color.White), fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.clickable {

                }
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            val scale = if (text.length > 14) 14f / text.length else 1f
            Text(
                text = text,
                style = TextStyle(
                    fontSize = (20f * scale).sp,
                    color = ColorProvider(Color.Black, Color.White),
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.clickable(actionStartActivity<EditStationList>())
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
    }
    private fun Color.toColorProvider() = ColorProvider(this, this)
}