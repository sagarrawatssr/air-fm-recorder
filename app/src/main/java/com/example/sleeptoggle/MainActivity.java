import rikka.shizuku.Shizuku;
import android.content.pm.PackageManager;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Request Shizuku permission if available and not granted
    if (Shizuku.pingBinder()) {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(1001);
        }
    }

    // ... rest of your onCreate code ...
}
