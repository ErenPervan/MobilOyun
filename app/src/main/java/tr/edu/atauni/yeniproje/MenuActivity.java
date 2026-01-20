package tr.edu.atauni.yeniproje;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Ana Menü Activity - Oyunun Giriş Ekranı
 * 
 * Bu Activity, oyuncuyu karşılayan ilk ekrandır (LAUNCHER Activity).
 * Oyuncu buradan oyuna başlayabilir, tema seçebilir ve skorlara bakabilir.
 * 
 * Özellikler:
 * - 🎮 Oyuna Başla: MainActivity'ye yönlendirir
 * - 🎨 Tema Seçimi: SharedPreferences ile kayıt edilen 3 farklı tema
 * - 🏆 Skor Tablosu: Firestore'dan en iyi 10 skoru gösterir
 * 
 * @author Senior Android Engineer
 */
public class MenuActivity extends AppCompatActivity {

    // UI Elemanları
    private Button btnOyunaBasla, btnTemalar, btnSkorlar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // ==================== UI ELEMANLARINI BAĞLA ====================
        btnOyunaBasla = findViewById(R.id.btnOyunaBasla);
        btnTemalar = findViewById(R.id.btnTemalar);
        btnSkorlar = findViewById(R.id.btnSkorlar);

        // ==================== BUTON CLICK LISTENER'LARI ====================
        
        // OYUNA BAŞLA - MainActivity'ye geçiş yap
        btnOyunaBasla.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // TEMALAR - Tema seçim dialog'unu göster
        btnTemalar.setOnClickListener(v -> {
            showThemeDialog();
        });

        // SKORLAR - Skor tablosunu göster
        btnSkorlar.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, SkorTableActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Tema Seçim Dialog'u (3 Tema - Güncellenmiş)
     * 
     * Kullanıcıya 3 farklı tema seçeneği sunar:
     * - Tema 0: Kartlar (kart1.png - kart32.png)
     * - Tema 1: Hayvanlar (hayvan1.png - hayvan32.png)
     * - Tema 2: İkonlar (icon1.png - icon32.png)
     * 
     * Seçilen tema ThemeHelper ile SharedPreferences'a kaydedilir.
     */
    private void showThemeDialog() {
        // Mevcut temayı al
        int currentTheme = ThemeHelper.getSelectedTheme(this);
        
        // Tema isimleri (3 tema)
        final String[] themes = {
            "🎴 Kartlar Teması",
            "🦁 Hayvanlar Teması",
            "🎨 İkonlar Teması"
        };
        
        // AlertDialog Builder oluştur
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎨 Tema Seçin");
        builder.setIcon(android.R.drawable.ic_menu_gallery);
        
        // SingleChoiceItems: Radio button'lu liste
        // Parametreler: (items, checkedItem, listener)
        builder.setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
            // Kullanıcı tema seçti (0, 1, veya 2)
            ThemeHelper.saveSelectedTheme(MenuActivity.this, which);
            
            // Bilgi mesajı göster
            String selectedThemeName = themes[which];
            Toast.makeText(MenuActivity.this, 
                "✅ " + selectedThemeName + " seçildi!", 
                Toast.LENGTH_SHORT).show();
            
            // Dialog'u kapat
            dialog.dismiss();
        });
        
        // İptal butonu ekle
        builder.setNegativeButton("❌ İptal", (dialog, which) -> dialog.dismiss());
        
        // Dialog'u göster
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * onResume() - Activity tekrar görünür olduğunda çağrılır
     * 
     * Gerekirse UI güncellemesi için kullanılabilir.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // UI güncellemeleri buraya eklenebilir
    }
}
