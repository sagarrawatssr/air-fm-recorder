package com.example.sleeptoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etDelay;
    private TextView tvStatus;
    private CheckBox cbBluetooth, cbWifi, cbHotspot, cbData;
    private Button btnStart, btnCancel;

    private final BroadcastReceiver timerUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status_text");
            if (status != null) {
                tvStatus.setText("Status: " + status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDelay = findViewById(R.id.et_delay_minutes);
        tvStatus = findViewById(R.id.tv_status);
        cbBluetooth = findViewById(R.id.cb_bluetooth);
        cbWifi = findViewById(R.id.cb_wifi);
        cbHotspot = findViewById(R.id.cb_hotspot);
        cbData = findViewById(R.id.cb_data);
        btnStart = findViewById(R.id.btn_start);
        btnCancel = findViewById(R.id.btn_cancel);

        // Preset shortcuts
        findViewById(R.id.btn_preset_15).setOnClickListener(v -> etDelay.setText("15"));
        findViewById(R.id.btn_preset_30).setOnClickListener(v -> etDelay.setText("30"));
        findViewById(R.id.btn_preset_60).setOnClickListener(v -> etDelay.setText("60"));

        btnStart.setOnClickListener(v -> startAutomationTimer());
        btnCancel.setOnClickListener(v -> cancelAutomationTimer());
    }

    private void startAutomationTimer() {
        String input = etDelay.getText().toString().trim();
        if (input.isEmpty() || Integer.parseInt(input) <= 0) {
            Toast.makeText(this, "Please enter a valid delay in minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        int minutes = Integer.parseInt(input);

        Intent intent = new Intent(this, SleepTimerService.class);
        intent.setAction(SleepTimerService.ACTION_START);
        intent.putExtra(SleepTimerService.EXTRA_MINUTES, minutes);
        intent.putExtra("toggle_bt", cbBluetooth.isChecked());
        intent.putExtra("toggle_wifi", cbWifi.isChecked());
        intent.putExtra("toggle_hotspot", cbHotspot.isChecked());
        intent.putExtra("toggle_data", cbData.isChecked());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        tvStatus.setText("Status: Running (" + minutes + "m set)");
    }

    private void cancelAutomationTimer() {
        Intent intent = new Intent(this, SleepTimerService.class);
        intent.setAction(SleepTimerService.ACTION_CANCEL);
        startService(intent);
        tvStatus.setText("Status: Cancelled / Idle");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.sleeptoggle.TIMER_TICK");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(timerUpdateReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(timerUpdateReceiver);
        } catch (Exception ignored) {}
    }
}
