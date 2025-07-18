package com.example.myapplication.REST;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WidgetClickHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        new Thread(() -> {
            String response = doPostRequest();

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sofia_trafic_widget);
            views.setTextViewText(R.id.appwidget_text, response);

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
            //Log.d("test", response);
        }).start();
    }
    private String doPostRequest(){
        try {
            URL url = new URL("http://localhost:8080/api/scrap");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonInput = "{\"stop\":\"0821\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                return br.readLine();
            } else {
                return "Error: " + conn.getResponseCode();
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
}
