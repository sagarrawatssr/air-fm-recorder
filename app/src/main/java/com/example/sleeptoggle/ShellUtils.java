package com.example.sleeptoggle;

import java.io.DataOutputStream;

public class ShellUtils {

    public static void runCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            // Try executing via root shell first, fallback to standard sh
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (Exception e) {
                process = Runtime.getRuntime().exec("sh");
            }

            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }
}
