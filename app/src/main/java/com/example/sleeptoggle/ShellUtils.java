package com.example.sleeptoggle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import rikka.shizuku.Shizuku;

public class ShellUtils {

    public static void runCommand(String command) {
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == 0) {
                // Execute privileged command through Shizuku ADB bridge
                Process process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, null);
                process.waitFor();
            } else {
                // Fallback standard shell (works if device has root)
                Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                process.waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
