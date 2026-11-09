package com.example.sleeptoggle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class SleepTimerService extends Service {

    public static final String ACTION_START = "com.example.sleeptoggle.START_TIMER";
    public static final String ACTION_CANCEL = "com.example.sleeptoggle.CANCEL_TIMER";
    public static final String EXTRA_MINUTES = "extra_minutes";

    private static final String CHANNEL_ID = "automation_timer_channel";
    private static final int NOTIF_ID = 2001;

    private CountDownTimer countDownTimer;
    private PowerManager.WakeLock wakeLock;

    private boolean doBt = true;
    private boolean doWifi = true;
    private boolean doHotspot = true;
    private boolean doData = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_CANCEL.equals(action)) {
            cancelTimer();
            stopForeground(true);
            stopSelf();
            broadcastStatus("Idle / Cancelled");
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action)) {
            int minutes = intent.getIntExtra(EXTRA_MINUTES, 30);
            doBt = intent.getBooleanExtra("toggle_bt", true);
            doWifi = intent.getBooleanExtra("toggle_wifi", true);
            doHotspot = intent.getBooleanExtra("toggle_hotspot", true);
            doData = intent.getBooleanExtra("toggle_data", true);

            startTimer(minutes);
        }

        return START_NOT_STICKY;
    }

    private void startTimer(int minutes) {
        cancelTimer();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && wakeLock == null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Timer::WakeLock");
            wakeLock.acquire((minutes * 60L + 60L) * 1000L);
        }

        long totalMillis = minutes * 60L * 1000L;
        startForeground(NOTIF_ID, buildNotification("Timer running: " + minutes + "m remaining"));

        countDownTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long m = millisUntilFinished / 1000 / 60;
                long s = (millisUntilFinished / 1000) % 60;
                String timeStr = String.format("%02d:%02d remaining", m, s);

                broadcastStatus(timeStr);

                if (s == 0) {
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (nm != null) {
                        nm.notify(NOTIF_ID, buildNotification("Shutting off in " + m + "m"));
                    }
                }
            }

            @Override
            public void onFinish() {
                executeActions();
                broadcastStatus("Actions Executed!");
                stopForeground(true);
                stopSelf();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void executeActions() {
        if (doBt) {
            try {
                BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                if (bt != null && bt.isEnabled()) bt.disable();
            } catch (Exception ignored) {}
        }

        if (doWifi) {
            try {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wm != null && wm.isWifiEnabled()) wm.setWifiEnabled(false);
            } catch (Exception ignored) {}
        }

        // Send triggers to MacroDroid for protected operations (Hotspot & Mobile Data)
        if (doHotspot || doData) {
            Intent mdIntent = new Intent("com.arlosoft.macrodroid.MACRO_TRIGGER");
            mdIntent.putExtra("macro_trigger_name", "TimerRoutineTrigger");
            mdIntent.putExtra("disable_hotspot", doHotspot);
            mdIntent.putExtra("disable_data", doData);
            sendBroadcast(mdIntent);
        }

        Toast.makeText(this, "Timer actions executed successfully", Toast.LENGTH_SHORT).show();
    }

    private void broadcastStatus(String text) {
        Intent tickIntent = new Intent("com.example.sleeptoggle.TIMER_TICK");
        tickIntent.putExtra("status_text", text);
        sendBroadcast(tickIntent);
    }

    private Notification buildNotification(String text) {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent cancelIntent = new Intent(this, SleepTimerService.class);
        cancelIntent.setAction(ACTION_CANCEL);
        PendingIntent cancelPi = PendingIntent.getService(
            this, 1, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Automation Timer")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setContentIntent(openPi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPi)
            .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Automation Countdown",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
