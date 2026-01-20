package tr.edu.atauni.yeniproje;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AyarlarActivity extends AppCompatActivity {

    dbHelper db;
    EditText edtIsim;

    // Değişkenler
    int secilenZorluk = 2;
    String secilenTema = "iskambil"; // Varsayılan tema

    // Butonlar
    Button btnKolay, btnOrta, btnZor;
    Button btnSayi, btnHayvan, btnIskambil; // btnEmoji -> btnSayi oldu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        db = new dbHelper(this);
        edtIsim = findViewById(R.id.edtIsim);
        Button btnKaydet = findViewById(R.id.btnKaydet);

        // Zorluk Butonlarını Bağla
        btnKolay = findViewById(R.id.btnKolay);
        btnOrta = findViewById(R.id.btnOrta);
        btnZor = findViewById(R.id.btnZor);

        // Tema Butonlarını Bağla
        btnSayi = findViewById(R.id.btnSayi); // ID değişti
        btnHayvan = findViewById(R.id.btnHayvan);
        btnIskambil = findViewById(R.id.btnIskambil);

        // --- MEVCUT AYARLARI YÜKLE ---
        edtIsim.setText(db.ayarOku("isim"));

        // Zorluğu Çek
        try {
            secilenZorluk = Integer.parseInt(db.ayarOku("zorluk"));
        } catch (Exception e) { secilenZorluk = 2; }

        // Temayı Çek
        secilenTema = db.ayarOku("tema");
        if(secilenTema.isEmpty()) secilenTema = "iskambil";

        // Görünümü Güncelle
        zorlukGuncelle(btnKolay, btnOrta, btnZor);
        temaGuncelle(btnSayi, btnHayvan, btnIskambil);

        // --- TIKLAMA OLAYLARI (ZORLUK) ---
        btnKolay.setOnClickListener(v -> { secilenZorluk = 1; zorlukGuncelle(btnKolay, btnOrta, btnZor); });
        btnOrta.setOnClickListener(v -> { secilenZorluk = 2; zorlukGuncelle(btnKolay, btnOrta, btnZor); });
        btnZor.setOnClickListener(v -> { secilenZorluk = 3; zorlukGuncelle(btnKolay, btnOrta, btnZor); });

        // --- TIKLAMA OLAYLARI (TEMA) ---
        // Emoji yerine Sayı yapıldı
        btnSayi.setOnClickListener(v -> {
            secilenTema = "sayi";
            temaGuncelle(btnSayi, btnHayvan, btnIskambil);
        });

        btnHayvan.setOnClickListener(v -> {
            secilenTema = "hayvan";
            temaGuncelle(btnSayi, btnHayvan, btnIskambil);
        });

        btnIskambil.setOnClickListener(v -> {
            secilenTema = "iskambil";
            temaGuncelle(btnSayi, btnHayvan, btnIskambil);
        });

        // --- KAYDET BUTONU ---
        btnKaydet.setOnClickListener(v -> {
            String isim = edtIsim.getText().toString();
            if(isim.length() < 3){
                Toast.makeText(this, "İsim en az 3 karakter olmalı", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hepsini Veritabanına Yaz
            db.ayarKaydet("isim", isim);
            db.ayarKaydet("zorluk", String.valueOf(secilenZorluk));
            db.ayarKaydet("tema", secilenTema);

            Toast.makeText(this, "Ayarlar Kaydedildi ✅", Toast.LENGTH_SHORT).show();
            finish(); // Menüye dön
        });
    }

    // Seçilen zorluk butonunu parlak, diğerlerini soluk yapar
    private void zorlukGuncelle(Button k, Button o, Button z){
        k.setAlpha(0.5f); o.setAlpha(0.5f); z.setAlpha(0.5f);

        if(secilenZorluk == 1) k.setAlpha(1.0f);
        if(secilenZorluk == 2) o.setAlpha(1.0f);
        if(secilenZorluk == 3) z.setAlpha(1.0f);
    }

    // Seçilen tema butonunu parlak, diğerlerini soluk yapar
    private void temaGuncelle(Button s, Button h, Button i){
        s.setAlpha(0.5f); h.setAlpha(0.5f); i.setAlpha(0.5f);

        if(secilenTema.equals("sayi")) s.setAlpha(1.0f); // "emoji" yerine "sayi" kontrolü
        if(secilenTema.equals("hayvan")) h.setAlpha(1.0f);
        if(secilenTema.equals("iskambil")) i.setAlpha(1.0f);
    }
}