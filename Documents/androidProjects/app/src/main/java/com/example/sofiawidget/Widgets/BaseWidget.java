package com.example.sofiawidget.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


import com.example.sofiawidget.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        var pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
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
            BaseWidgetClickAction(context, intent);
        }

        private void BaseWidgetClickAction(Context context, Intent intent){
            if("GetData".equals(intent.getAction())){
                int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                if(widgetID != AppWidgetManager.INVALID_APPWIDGET_ID){
                    var views = new RemoteViews(context.getPackageName(), R.layout.base_widget);
                        ReceiveData("0821", new Callback(){
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if(response.isSuccessful()){
                                    var value = response.body().string();
                                    views.setTextViewText(R.id.response, value);
                                    AppWidgetManager.getInstance(context).updateAppWidget(widgetID, views);
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Log.d("Base", "idk " + e);
                                views.setTextViewText(R.id.response, "Error");
                                AppWidgetManager.getInstance(context).updateAppWidget(widgetID, views);
                            }
                        });
                }
            }
        }
        private void ReceiveData(String StopID, Callback call){
            var client = new OkHttpClient();
            String url = "http://192.168.68.118:8080/api/scrap";
            String jsonBody = "{\"stop\":\"" + StopID + "\"}";
            var body = RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();
            client.newCall(request).enqueue(call);
        }
}
