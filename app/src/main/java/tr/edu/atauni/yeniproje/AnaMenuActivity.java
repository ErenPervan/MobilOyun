package tr.edu.atauni.yeniproje;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class AnaMenuActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // İzin sonucu işlemleri
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ana_menu);

        // Bildirim izni iste (Android 13+)
        askNotificationPermission();

        Button btnOyna = findViewById(R.id.btnOyna);
        Button btnAyarlar = findViewById(R.id.btnAyarlar);
        Button btnSkorlar = findViewById(R.id.btnSkorlar);
        Button btnCikis = findViewById(R.id.btnCikis);

        btnOyna.setOnClickListener(v -> startActivity(new Intent(AnaMenuActivity.this, OyunActivity.class)));
        btnAyarlar.setOnClickListener(v -> startActivity(new Intent(AnaMenuActivity.this, AyarlarActivity.class))); // AyarlarActivity oluşturacağız
        btnSkorlar.setOnClickListener(v -> startActivity(new Intent(AnaMenuActivity.this, SkorActivity.class))); // SkorActivity oluşturacağız

        btnCikis.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}