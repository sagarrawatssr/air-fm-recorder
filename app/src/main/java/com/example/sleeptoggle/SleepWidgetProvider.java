package com.example.sleeptoggle;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SleepWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_SLEEP_NOW = "com.example.sleeptoggle.ACTION_SLEEP_NOW";
    public static final String ACTION_TOGGLE_BT = "com.example.sleeptoggle.ACTION_TOGGLE_BT";
    public static final String ACTION_TOGGLE_WIFI = "com.example.sleeptoggle.ACTION_TOGGLE_WIFI";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Button 1: Immediate Sleep Execution
            Intent sleepIntent = new Intent(context, SleepWidgetProvider.class);
            sleepIntent.setAction(ACTION_SLEEP_NOW);
            PendingIntent sleepPi = PendingIntent.getBroadcast(context, 0, sleepIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btn_sleep_all, sleepPi);

            // Button 2: Toggle Bluetooth
            Intent btIntent = new Intent(context, SleepWidgetProvider.class);
            btIntent.setAction(ACTION_TOGGLE_BT);
            PendingIntent btPi = PendingIntent.getBroadcast(context, 1, btIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btn_toggle_bt, btPi);

            // Button 3: Toggle Wi-Fi
            Intent wifiIntent = new Intent(context, SleepWidgetProvider.class);
            wifiIntent.setAction(ACTION_TOGGLE_WIFI);
            PendingIntent wifiPi = PendingIntent.getBroadcast(context, 2, wifiIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btn_toggle_wifi, wifiPi);

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ACTION_SLEEP_NOW.equals(action)) {
            // Turn off Bluetooth
            if (btAdapter != null && btAdapter.isEnabled()) {
                btAdapter.disable();
            }

            // Turn off Wi-Fi
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }

            // Broadcast trigger to MacroDroid via Intent Trigger
            Intent mdIntent = new Intent("com.arlosoft.macrodroid.MACRO_TRIGGER");
            mdIntent.putExtra("macro_trigger_name", "SleepNowWidget");
            context.sendBroadcast(mdIntent);

            Toast.makeText(context, "Executing Sleep Routine", Toast.LENGTH_SHORT).show();

        } else if (ACTION_TOGGLE_BT.equals(action)) {
            if (btAdapter != null) {
                if (btAdapter.isEnabled()) {
                    btAdapter.disable();
                    Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
                } else {
                    btAdapter.enable();
                    Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (ACTION_TOGGLE_WIFI.equals(action)) {
            if (wifiManager != null) {
                boolean state = wifiManager.isWifiEnabled();
                wifiManager.setWifiEnabled(!state);
                Toast.makeText(context, state ? "Wi-Fi Disabled" : "Wi-Fi Enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
