package com.example.sofiawidget.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.sofiawidget.R;

/**
 * Implementation of App Widget functionality.
 */
public class BaseWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        var views = new RemoteViews(context.getPackageName(), R.layout.base_widget);
        Intent intent = new Intent(context, BaseWidget.class);
        intent.setAction("GetData");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        var pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.button_click_me, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);
        if("GetData".equals(intent.getAction())){
            int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if(widgetID != AppWidgetManager.INVALID_APPWIDGET_ID){
                var views = new RemoteViews(context.getPackageName(), R.layout.base_widget);
                var prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
                int number = prefs.getInt(widgetID + "", 0);
                number++;
                prefs.edit().putInt(widgetID+"", number).apply();
                views.setTextViewText(R.id.counter, Integer.toString(number));
                AppWidgetManager.getInstance(context).updateAppWidget(widgetID, views);
            }
        }
    }
}
