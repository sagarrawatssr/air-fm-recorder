    private void executeActions() {
        // 1. Bluetooth Toggle
        if (doBt) {
            try {
                BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                if (bt != null && bt.isEnabled()) {
                    bt.disable();
                } else {
                    ShellUtils.runCommand("cmd bluetooth disable");
                }
            } catch (Exception e) {
                ShellUtils.runCommand("cmd bluetooth disable");
            }
        }

        // 2. Wi-Fi Toggle
        if (doWifi) {
            try {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wm != null && wm.isWifiEnabled()) {
                    wm.setWifiEnabled(false);
                } else {
                    ShellUtils.runCommand("svc wifi disable");
                }
            } catch (Exception e) {
                ShellUtils.runCommand("svc wifi disable");
            }
        }

        // 3. Standalone Hotspot Toggle (Direct via shell)
        if (doHotspot) {
            ShellUtils.runCommand("cmd wifi stop-softap");
        }

        // 4. Standalone Mobile Data Toggle (Direct via shell)
        if (doData) {
            ShellUtils.runCommand("svc data disable");
        }

        Toast.makeText(this, "All selected radios turned off", Toast.LENGTH_SHORT).show();
    }
