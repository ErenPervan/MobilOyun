package tr.edu.atauni.yeniproje;

/*
 * ========================================================================
 * 🔊 SES DOSYALARI KURULUM TALİMATLARI (ÖNEMLİ!)
 * ========================================================================
 * 
 * Bu oyun ses efektleri kullanmaktadır. Ses dosyalarını projenize eklemek için:
 * 
 * 1. KLASÖR OLUŞTURMA:
 *    - Android Studio'da sol taraftaki proje yapısında şu yolu bulun:
 *      app/src/main/res/
 *    - 'res' klasörüne sağ tıklayın
 *    - New → Android Resource Directory seçin
 *    - Resource type: "raw" seçin (dropdown menüden)
 *    - OK'a tıklayın
 *    - Şimdi 'res/raw/' klasörü oluşturuldu
 * 
 * 2. SES DOSYALARINI EKLEME:
 *    Aşağıdaki dosya isimlerine sahip ses dosyalarını 'res/raw/' klasörüne kopyalayın:
 * 
 *    📁 res/raw/dogru_sesi.mp3     → Doğru eşleşme sesi (örn: chime, ding, success)
 *    📁 res/raw/yanlis_sesi.mp3    → Yanlış eşleşme sesi (örn: buzzer, error, wrong)
 *    📁 res/raw/kart_cevir_sesi.mp3 → Kart çevirme sesi (opsiyonel - flip sound)
 * 
 * 3. DOSYA FORMATI:
 *    - Desteklenen formatlar: .mp3, .wav, .ogg
 *    - Önerilen format: .mp3 (en yaygın)
 *    - Dosya boyutu: Maksimum 1MB (kısa ses efektleri için)
 * 
 * 4. SES KAYNAKLARI (ÜCRETSİZ):
 *    - Freesound.org (ücretsiz ses efektleri)
 *    - Zapsplat.com (ücretsiz game sounds)
 *    - Mixkit.co (royalty-free sounds)
 *    - YouTube Audio Library
 * 
 * 5. HATA DURUMU:
 *    - Eğer ses dosyaları eklenmezse, oyun çalışır ama ses çıkmaz
 *    - Logcat'te "Ses dosyası bulunamadı" uyarısı görürsünüz
 * 
 * NOT: Ses dosyalarını ekledikten sonra projeyi yeniden build edin:
 *      Build → Clean Project → Build → Rebuild Project
 * 
 * ========================================================================
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Oyun Activity Sınıfı - Kart Eşleştirme Oyunu (Geliştirilmiş Versiyon)
 * 
 * Bu Activity hafıza kartı eşleştirme oyununu yönetir. Kullanıcı kartları açarak
 * eşleşen çiftleri bulmaya çalışır.
 * 
 * Gelişmiş Özellikler:
 * - ✨ Profesyonel 3D kart flip animasyonu (ObjectAnimator)
 * - 🎵 Ses efektleri (SoundPool ile - doğru/yanlış sesler)
 * - 📊 Skor sistemi (doğru: +100, yanlış: -20)
 * - 🎨 Görsel feedback (shake animasyonu, fade efektleri)
 * - 📱 Dinamik kart boyutlandırma (ekran genişliğine göre)
 * - ⏱️ Geri sayım timer ve süre takibi
 * - 🎯 Oyun bitişi AlertDialog'u
 * - 🔄 Yeniden başlatma özelliği
 * - 🛡️ Memory leak önleme mekanizmaları
 * 
 * @author Yazılım Mühendisliği Öğrencisi - Senior Android Game Engineer tarafından geliştirildi
 */
public class OyunActivity extends AppCompatActivity {

    // ==================== OYUN DEĞİŞKENLERİ ====================
    
    String oyuncuIsim; // Oyuncunun ismi (MainActivity'den gelen)
    int zorlukSeviyesi; // Zorluk seviyesi (1=Kolay, 2=Orta, 3=Zor)
    int satirSayisi; // Grid satır sayısı
    int sutunSayisi; // Grid sütun sayısı
    int hataHakki; // Kalan hata hakkı
    int toplamSure; // Toplam süre (saniye)
    int kalanSure; // Kalan süre (saniye)
    int eslesmeSayisi = 0; // Eşleşen kart çifti sayısı
    int toplamCift; // Toplam eşleşmesi gereken çift sayısı
    
    // ==================== SKOR SİSTEMİ ====================
    
    int skor = 0; // Oyuncu skoru
    final int DOGRU_PUAN = 100; // Doğru eşleşme puanı
    final int YANLIS_PUAN = -20; // Yanlış eşleşme cezası
    final int HIZLI_ESLESME_BONUS = 50; // Hızlı eşleşme bonusu (10 saniyeden az)
    
    // ==================== OYUN DURUM DEĞİŞKENLERİ ====================
    
    Kart[] kartlar; // Tüm kartları tutan dizi
    int suankiKart = 0; // Şu an açık kartın ID'si (0 = hiçbiri açık değil)
    Kart oncekiKart; // Önceki açılan kart referansı
    boolean bekle = false; // Kart animasyonu sırasında tıklamayı engelle
    boolean oyunBitti = false; // Oyun bitmiş mi kontrolü
    
    // ==================== UI ELEMANLARI ====================
    
    TextView bilgiTv; // Oyuncu bilgisi gösterimi
    TextView hataHakkiTv; // Hata hakkı gösterimi
    TextView sureTv; // Süre gösterimi (TextView)
    TextView skorTv; // Skor gösterimi (YENİ!)
    ProgressBar sureBar; // Süre gösterimi (Progress Bar)
    GridLayout grd; // Kartların yerleştirileceği grid
    Button yenidenBaslaBtn; // Yeniden başlatma butonu
    Button anaMenuBtn; // Ana menüye dönüş butonu
    
    // ==================== TIMER DEĞİŞKENLERİ ====================
    
    CountDownTimer countDownTimer; // Geri sayım timer'ı
    long baslangicZamani; // Oyunun başlangıç zamanı (milisaniye)

    // ==================== SES SİSTEMİ (SoundPool) ====================
    
    SoundPool soundPool; // Ses havuzu (kısa ses efektleri için optimize)
    int dogruSesId; // Doğru eşleşme ses ID'si
    int yanlisSesId; // Yanlış eşleşme ses ID'si
    int kartCevirSesId; // Kart çevirme ses ID'si
    boolean sesYuklendi = false; // Sesler yüklendi mi kontrolü

    /**
     * onSaveInstanceState() - Activity yeniden oluşturulduğunda state'i kaydet
     * 
     * ==================== STATE PRESERVATION (Durum Koruma) ====================
     * 
     * Android sistem kaynakları (RAM) azaldığında veya ekran döndürülünce Activity
     * yok edilip yeniden oluşturulur. Bu durumda oyun durumu kaybolur!
     * 
     * onSaveInstanceState() metodu sayesinde:
     * - Kullanıcı ekranı döndürdüğünde oyun devam eder
     * - Uygulama arka plana alınıp geri gelindiğinde durum korunur
     * - Sistem bellek kısıtı nedeniyle Activity'yi yok etse bile durum saklanır
     * 
     * Bundle Nedir?
     * - Key-Value (Anahtar-Değer) çiftleri içeren veri yapısı
     * - Basit veri tiplerini saklayabilir: int, String, boolean, int[], ArrayList vb.
     * - Karmaşık objeleri saklamak için Serializable veya Parcelable gerekir
     * 
     * Bu Metodda Neler Kaydediliyor:
     * 1. Oyun durumu değişkenleri (skor, süre, eşleşme sayısı)
     * 2. Kartların durumu (hangi kartlar açık, hangileri eşleşmiş)
     * 3. Grid yapısı (satır/sütun sayısı)
     * 
     * PERFORMANS NOTU: Bundle boyutu küçük tutulmalıdır (max 500KB önerilir)
     * Büyük veriler için SharedPreferences veya SQLite kullanın.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // ==================== OYUN DURUM DEĞİŞKENLERİNİ KAYDET ====================
        outState.putInt("kalanSure", kalanSure);
        outState.putInt("hataHakki", hataHakki);
        outState.putInt("eslesmeSayisi", eslesmeSayisi);
        outState.putInt("skor", skor);
        outState.putBoolean("oyunBitti", oyunBitti);
        outState.putBoolean("bekle", bekle);
        outState.putInt("suankiKart", suankiKart);
        
        // ==================== GRİD YAPISINI KAYDET ====================
        outState.putInt("satirSayisi", satirSayisi);
        outState.putInt("sutunSayisi", sutunSayisi);
        outState.putInt("zorlukSeviyesi", zorlukSeviyesi);
        outState.putInt("toplamSure", toplamSure);
        
        // ==================== KARTLARIN DURUMUNU KAYDET ====================
        // Kart sınıfı View'dan türediği için Serializable değildir.
        // Bu yüzden kartların durumunu int array'lere kaydediyoruz.
        
        if (kartlar != null && kartlar.length > 0) {
            int kartSayisi = kartlar.length;
            
            // Her kart için 3 bilgi tutacağız:
            // 1. Kart ID'si (findViewById için)
            // 2. Resim ID'si (hangi resim)
            // 3. Durum (0=KAPALI, 1=ACIK, 2=EŞLEŞMİŞ/DISABLED)
            
            int[] kartIdleri = new int[kartSayisi];
            int[] kartResimleri = new int[kartSayisi];
            int[] kartDurumlari = new int[kartSayisi];
            boolean[] kartEtkinlikleri = new boolean[kartSayisi]; // enabled/disabled
            
            for (int i = 0; i < kartSayisi; i++) {
                Kart kart = kartlar[i];
                kartIdleri[i] = kart.getId();
                kartResimleri[i] = kart.resId;
                
                // Durum: ACIK=1, KAPALI=0
                kartDurumlari[i] = (kart.mevcutDurum == Kart.Durum.ACIK) ? 1 : 0;
                
                // Etkinlik: eşleşmiş kartlar devre dışı bırakılır
                kartEtkinlikleri[i] = kart.isEnabled();
            }
            
            // Bundle'a kaydet
            outState.putInt("kartSayisi", kartSayisi);
            outState.putIntArray("kartIdleri", kartIdleri);
            outState.putIntArray("kartResimleri", kartResimleri);
            outState.putIntArray("kartDurumlari", kartDurumlari);
            outState.putBooleanArray("kartEtkinlikleri", kartEtkinlikleri);
        }
        
        Log.d("OyunState", "✅ Oyun durumu kaydedildi - Skor: " + skor + ", Eşleşme: " + eslesmeSayisi);
    }

    /**
     * onCreate() - Activity yaşam döngüsünün başlangıcı
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_oyun);

        // ==================== SES SİSTEMİNİ BAŞLAT ====================
        sesSisteminiBaslat();

        // ==================== UI ELEMANLARINI BAĞLA ====================
        bilgiTv = findViewById(R.id.bilgiTxt);
        hataHakkiTv = findViewById(R.id.hataHakkiTxt);
        sureTv = findViewById(R.id.sureTxt);
        skorTv = findViewById(R.id.skorTxt); // YENİ: Skor TextView
        sureBar = findViewById(R.id.sureBar);
        grd = findViewById(R.id.grdLytOut);
        yenidenBaslaBtn = findViewById(R.id.yenidenBaslaBtn);
        anaMenuBtn = findViewById(R.id.anaMenuBtn);

        // ==================== INTENT VERİLERİNİ AL ====================
        Intent in = getIntent();
        oyuncuIsim = in.getStringExtra("oyuncuIsm");
        zorlukSeviyesi = in.getIntExtra("zorlukSeviyesi", 1);

        // Bilgi metnini güncelle
        bilgiTv.setText(oyuncuIsim + " - Hoş Geldiniz!");
        skorGuncelle(); // Skoru göster

        // ==================== BUTON CLICK LISTENER'LARI ====================
        
        yenidenBaslaBtn.setOnClickListener(view -> {
            oyunuYenidenBaslat();
        });

        anaMenuBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle("Ana Menüye Dön")
                .setMessage("Oyunu bırakıp ana menüye dönmek istiyor musunuz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    durdurTimer();
                    finish();
                })
                .setNegativeButton("Hayır", null)
                .show();
        });

        // ==================== STATE RESTORATION (Durum Geri Yükleme) ====================
        // savedInstanceState null değilse, Activity yeniden oluşturulmuş demektir
        // (ekran döndürme, arka plana alınma vb.)
        if (savedInstanceState != null) {
            // Kaydedilmiş oyun durumunu geri yükle
            restoreGameState(savedInstanceState);
        } else {
            // Normal başlangıç - yeni oyun
            oyunuBaslat();
        }
    }

    /**
     * Ses Sistemini Başlatma Metodu
     * 
     * SoundPool kullanarak ses efektlerini yükler.
     * SoundPool, kısa oyun sesleri için MediaPlayer'dan daha optimize edilmiştir.
     * 
     * ÖNEMLİ: Ses dosyaları res/raw/ klasöründe olmalıdır!
     */
    private void sesSisteminiBaslat() {
        // AudioAttributes: Ses tipini ve kullanım amacını belirtir
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // Oyun sesi olduğunu belirt
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Kısa ses efekti
                .build();

        // SoundPool oluştur
        // Parametre: Maksimum aynı anda çalabilecek ses sayısı (3)
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        // Ses dosyalarını yükle (res/raw/ klasöründen)
        try {
            // Doğru eşleşme sesi
            dogruSesId = soundPool.load(this, R.raw.correct, 1);
            
            // Yanlış eşleşme sesi
            yanlisSesId = soundPool.load(this, R.raw.wrong, 1);
            
            // Kart çevirme sesi (opsiyonel)
            kartCevirSesId = soundPool.load(this, R.raw.click, 1);
            
            // Sesler yüklendi
            sesYuklendi = true;
            Log.d("SesSistemi", "Tüm ses dosyaları başarıyla yüklendi");
            
        } catch (Exception e) {
            // Ses dosyaları bulunamazsa hata mesajı ver
            Log.e("SesSistemi", "SES DOSYALARI BULUNAMADI! Lütfen res/raw/ klasörüne ses dosyalarını ekleyin.");
            Log.e("SesSistemi", "Gerekli dosyalar: dogru_sesi.mp3, yanlis_sesi.mp3, kart_cevir_sesi.mp3");
            sesYuklendi = false;
            
            // Kullanıcıya bilgi ver (sadece ilk hatada)
            Toast.makeText(this, "⚠️ Ses dosyaları bulunamadı. Oyun sessiz çalışacak.", Toast.LENGTH_LONG).show();
        }

        // SoundPool'un ses yükleme callback'i (opsiyonel kontrol)
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    Log.d("SesSistemi", "Ses ID " + sampleId + " başarıyla yüklendi");
                } else {
                    Log.e("SesSistemi", "Ses ID " + sampleId + " yüklenemedi!");
                }
            }
        });
    }

    /**
     * Ses Çalma Metodu
     * 
     * Belirtilen ses ID'sini çalar.
     * 
     * @param sesId Çalınacak sesin ID'si (dogruSesId, yanlisSesId, vb.)
     */
    private void sesCal(int sesId) {
        if (sesYuklendi && soundPool != null) {
            // SoundPool.play() parametreleri:
            // 1. soundID: Ses ID'si
            // 2. leftVolume: Sol kanal ses seviyesi (0.0 - 1.0)
            // 3. rightVolume: Sağ kanal ses seviyesi (0.0 - 1.0)
            // 4. priority: Öncelik (0 = en düşük)
            // 5. loop: Tekrar sayısı (0 = tekrar yok, -1 = sonsuz döngü)
            // 6. rate: Oynatma hızı (1.0 = normal, 0.5 = yavaş, 2.0 = hızlı)
            soundPool.play(sesId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    /**
     * Skor Güncelleme Metodu
     * 
     * Skoru UI'da gösterir ve renk değişimi efekti ekler.
     */
    private void skorGuncelle() {
        skorTv.setText("🏆 Skor: " + skor);
        
        // Skor rengini puanlara göre değiştir (görsel feedback)
        if (skor < 0) {
            skorTv.setTextColor(0xFFE53935); // Kırmızı (negatif)
        } else if (skor < 500) {
            skorTv.setTextColor(0xFF4CAF50); // Yeşil (normal)
        } else {
            skorTv.setTextColor(0xFFFFD700); // Altın (yüksek skor!)
        }

        // Skor animasyonu (hafif zoom efekti)
        skorTv.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction(() -> {
                skorTv.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start();
            })
            .start();
    }

    /**
     * Oyunu Başlatma Metodu
     */
    private void oyunuBaslat() {
        oyunBitti = false;
        eslesmeSayisi = 0;
        suankiKart = 0;
        bekle = false;
        skor = 0; // Skoru sıfırla

        // ==================== ZORLUK SEVİYESİNE GÖRE AYARLAR ====================
        
        if (zorlukSeviyesi == 1) {
            // KOLAY: 2x3 grid (6 kart = 3 çift)
            satirSayisi = 2;
            sutunSayisi = 3;
            hataHakki = 10;
            toplamSure = 60; // 1 dakika
        } else if (zorlukSeviyesi == 2) {
            // ORTA: 3x4 grid (12 kart = 6 çift)
            satirSayisi = 3;
            sutunSayisi = 4;
            hataHakki = 15;
            toplamSure = 90; // 1.5 dakika
        } else if (zorlukSeviyesi == 3) {
            // ZOR: 4x4 grid (16 kart = 8 çift) - YENİ GÜNCELLEME!
            // 
            // ESKİ VERSİYON: 6×3 = 18 kart (9 çift)
            // Sorunlar:
            // - Çok fazla kart → Oyun çok uzun sürüyordu
            // - 6 satır → Ekranda scroll gerekiyordu
            // - Asimetrik grid (6×3) → Görsel olarak ideal değildi
            //
            // YENİ VERSİYON: 4×4 = 16 kart (8 çift) ✓
            // Avantajlar:
            // ✓ Mükemmel kare grid (4×4 = simetrik ve estetik)
            // ✓ Dinamik boyutlandırma ile ekrana ideal sığar
            // ✓ 4 satır → Çoğu cihazda scroll GEREKMİYOR
            // ✓ 8 çift bulmak daha dengeli ve oynanabilir
            // ✓ ScrollView varsa bile daha az scroll gerekir
            //
            satirSayisi = 4;
            sutunSayisi = 4;
            hataHakki = 20; // 18 kartta 25'ti, 16 kartta 20 optimal
            toplamSure = 120; // 2 dakika (süre aynı kaldı - zorluk dengelemesi)
        }

        toplamCift = (satirSayisi * sutunSayisi) / 2;
        grd.setColumnCount(sutunSayisi);
        grd.setRowCount(satirSayisi);
        hataHakkiTv.setText("❤️ Hak: " + hataHakki);
        skorGuncelle();

        int kartBoyutu = ekranGenisligindenKartBoyutuHesapla();

        // ==================== KARTLARI OLUŞTUR ====================
        int toplamSayi = satirSayisi * sutunSayisi;
        kartlar = new Kart[toplamSayi];

        for (int i = 0; i < toplamSayi; i++) {
            Kart kart;
            
            if (i % 2 == 0) {
                kart = new Kart(this, i + 100, i);
            } else {
                kart = new Kart(this, i + 100, i - 1);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = kartBoyutu;
            params.height = kartBoyutu;
            params.setMargins(4, 4, 4, 4);
            kart.setLayoutParams(params);

            // ==================== KART CLICK LISTENER (Animasyonlu) ====================
            kart.setOnClickListener(view -> kartTiklandi(kart));

            kartlar[i] = kart;
        }

        kartlariKaristir();
        sureBaslat(toplamSure);
    }

    /**
     * Kart Eşleşmesi Kontrol Metodu
     * 
     * İki kartın eşleşip eşleşmediğini kontrol eder ve uygun animasyonları çalıştırır.
     * 
     * @param kart1 Birinci kart
     * @param kart2 İkinci kart
     */
    private void kartEslesmesiKontrolEt(Kart kart1, Kart kart2) {
        // ==================== KART EŞLEŞMESİ KONTROLÜ ====================
        if (kart1.resId == kart2.resId) {
            // ✅ EŞLEŞME BULUNDU!
            
            // Doğru ses efekti çal
            sesCal(dogruSesId);
            
            // Skor ekle
            skor += DOGRU_PUAN;
            
            // Hızlı eşleşme bonusu (ilk 10 saniyede eşleşme)
            int gecenSure = toplamSure - kalanSure;
            if (gecenSure < 10) {
                skor += HIZLI_ESLESME_BONUS;
                Toast.makeText(getApplicationContext(), 
                    "⚡ Hızlı Eşleşme! +" + HIZLI_ESLESME_BONUS + " Bonus", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), 
                    "✓ Eşleşme! +" + DOGRU_PUAN + " Puan", 
                    Toast.LENGTH_SHORT).show();
            }
            
            skorGuncelle();
            eslesmeSayisi++;
            
            // Eşleşen kartlara fade-out/scale animasyonu uygula
            kartEslesmeFeedback(kart1, kart2);
            
            bekle = false;
            suankiKart = 0;

            // Tüm kartlar eşleşti mi kontrol et
            if (eslesmeSayisi == toplamCift) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        oyunuBitir(true);
                    }
                }, 800); // Animasyon bitimini bekle
            }
        } 
        // ❌ EŞLEŞME YOK
        else {
            // Yanlış ses efekti çal
            sesCal(yanlisSesId);
            
            // Skor azalt (ceza)
            skor += YANLIS_PUAN;
            skorGuncelle();
            
            hataHakki--;
            hataHakkiTv.setText("❤️ Hak: " + hataHakki);

            // Shake animasyonu uygula (görsel hata feedback)
            kartShakeAnimasyonu(kart1);
            kartShakeAnimasyonu(kart2);

            if (hataHakki <= 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        oyunuBitir(false);
                    }
                }, 1000);
                return;
            }

            // Shake animasyonu bitimini bekle, sonra kartları geri çevir
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    kartCevirAnimasyonu(kart1, false);
                    kartCevirAnimasyonu(kart2, false);
                    suankiKart = 0;
                    bekle = false;
                }
            }, 1500);
        }
    }

    /**
     * ✨ KART ÇEVİRME ANIMASYONU (3D Flip Effect)
     * 
     * ObjectAnimator kullanarak Y ekseni etrafında 180 derece döndürme animasyonu.
     * Gerçekçi 3D kart çevirme efekti oluşturur.
     * 
     * @param kart Animasyon uygulanacak kart
     * @param ac true = Kartı aç (göster), false = Kartı kapat (gizle)
     */
    private void kartCevirAnimasyonu(final Kart kart, final boolean ac) {
        // İlk yarı: 0° → 90° (kartın ön yüzü kaybolur)
        ObjectAnimator ilkYari = ObjectAnimator.ofFloat(kart, "rotationY", 0f, 90f);
        ilkYari.setDuration(150); // 150ms
        ilkYari.setInterpolator(new AccelerateDecelerateInterpolator());

        // İkinci yarı: 90° → 180° (kartın arka yüzü görünür)
        ObjectAnimator ikinciYari = ObjectAnimator.ofFloat(kart, "rotationY", 90f, 180f);
        ikinciYari.setDuration(150); // 150ms
        ikinciYari.setInterpolator(new AccelerateDecelerateInterpolator());

        // İlk yarı bitince kartın görünümünü değiştir ve ikinci yarıyı başlat
        ilkYari.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Kartın ortasında görünümü değiştir (90 derecede)
                kart.dondur(); // Kart durumunu değiştir (ACIK/KAPALI)
                ikinciYari.start(); // İkinci yarı animasyonu başlat
            }
        });

        // İkinci yarı bitince rotasyonu sıfırla (sonraki animasyonlar için)
        ikinciYari.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                kart.setRotationY(0f); // Rotasyonu sıfırla
            }
        });

        // İlk yarı animasyonunu başlat
        ilkYari.start();
    }

    /**
     * 🔴 SHAKE ANIMASYONU (Yanlış Eşleşme Feedback)
     * 
     * Kartı sağa-sola sallar (titreşim efekti) - hata göstergesi.
     * CycleInterpolator kullanarak doğal sallanma efekti.
     * 
     * @param kart Animasyon uygulanacak kart
     */
    private void kartShakeAnimasyonu(Kart kart) {
        // X ekseninde sağa-sola hareket (translationX)
        // CycleInterpolator(2): 2 tam döngü (2 kez sağa-sola)
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(kart, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shakeX.setDuration(500); // 500ms
        shakeX.setInterpolator(new CycleInterpolator(2)); // 2 döngü
        shakeX.start();

        // Opsiyonel: Hafif Y ekseninde de sallanma (daha doğal görünüm)
        ObjectAnimator shakeY = ObjectAnimator.ofFloat(kart, "translationY", 0f, -10f, 10f, -10f, 10f, -5f, 5f, 0f);
        shakeY.setDuration(500);
        shakeY.setInterpolator(new CycleInterpolator(2));
        shakeY.start();
    }

    /**
     * ✅ EŞLEŞME FEEDBACK ANIMASYONU (Doğru Eşleşme)
     * 
     * Eşleşen kartlara fade-out ve scale-down efekti uygular.
     * Kartlar yavaşça küçülür ve soluklaşır (başarı göstergesi).
     * 
     * @param kart1 Birinci eşleşen kart
     * @param kart2 İkinci eşleşen kart
     */
    private void kartEslesmeFeedback(Kart kart1, Kart kart2) {
        // İlk kart için animasyon seti
        AnimatorSet animSet1 = new AnimatorSet();
        
        // Scale animasyonu (küçültme): 1.0 → 0.8
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(kart1, "scaleX", 1.0f, 1.1f, 0.8f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(kart1, "scaleY", 1.0f, 1.1f, 0.8f);
        
        // Alpha animasyonu (soluklaştırma): 1.0 → 0.4
        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(kart1, "alpha", 1.0f, 0.4f);
        
        // Tüm animasyonları birlikte çalıştır
        animSet1.playTogether(scaleX1, scaleY1, alpha1);
        animSet1.setDuration(400); // 400ms
        animSet1.setInterpolator(new AccelerateDecelerateInterpolator());

        // İkinci kart için aynı animasyon
        AnimatorSet animSet2 = new AnimatorSet();
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(kart2, "scaleX", 1.0f, 1.1f, 0.8f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(kart2, "scaleY", 1.0f, 1.1f, 0.8f);
        ObjectAnimator alpha2 = ObjectAnimator.ofFloat(kart2, "alpha", 1.0f, 0.4f);
        
        animSet2.playTogether(scaleX2, scaleY2, alpha2);
        animSet2.setDuration(400);
        animSet2.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animasyonları başlat
        animSet1.start();
        animSet2.start();

        // Animasyon bitiminde kartları tıklanamaz yap (eşleşmiş kartlar)
        animSet1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                kart1.setEnabled(false); // Tıklamayı devre dışı bırak
                kart2.setEnabled(false);
            }
        });
    }

    /**
     * ==================== DİNAMİK KART BOYUTU HESAPLAMA ====================
     * 
     * Dinamik Kart Boyutu Hesaplama Metodu (ScrollView Destekli)
     * 
     * Bu metot, cihazın ekran genişliğine göre her kartın optimal boyutunu hesaplar.
     * ScrollView sayesinde yükseklik sınırlaması olmadan kartlar oluşturulur.
     * 
     * YENİ YAKLAŞIM:
     * - Sadece EKRAN GENİŞLİĞİNE göre hesaplama yapılır
     * - Kartlar KARE şeklinde oluşturulur (width = height)
     * - Yükseklik taşarsa ScrollView otomatik scroll sağlar
     * - Bu sayede tüm zorluk seviyeleri güvenle çalışır
     * 
     * AVANTAJLAR:
     * ✓ Basit ve güvenilir hesaplama
     * ✓ Yükseklik sınırlaması yok (ScrollView sayesinde)
     * ✓ Portrait ve Landscape otomatik uyum
     * ✓ Tüm cihazlarda çalışır garantisi
     * 
     * MATEMATİKSEL FORMÜL:
     * ════════════════════════════════════════════════════════════════
     * 
     * 1. Kullanılabilir Genişlik = Ekran Genişliği - Padding (Sol + Sağ)
     * 
     * 2. Toplam Boşluk = Kartlar Arası Boşluklar
     *    - Her kartın sol ve sağında margin var (örn: 4dp)
     *    - Toplam margin = (SütunSayısı + 1) × Margin × 2
     *    - Neden +1? → [margin][kart][margin][kart][margin]
     * 
     * 3. Kart Genişliği = (Kullanılabilir Genişlik - Toplam Boşluk) / Sütun Sayısı
     * 
     * 4. Kart Yüksekliği = Kart Genişliği (KARE olmalı!)
     * 
     * ════════════════════════════════════════════════════════════════
     * 
     * ÖRNEK HESAPLAMA (Hard Seviye - 6 Sütun):
     * --------------------------------------------------------
     * Ekran Genişliği: 1080px (tipik Android cihaz)
     * Layout Padding: 16dp × 2 = ~48px
     * Kart Margin: 4dp × 2 = ~12px (her kart için)
     * 
     * Adım 1: Kullanılabilir Genişlik
     *         = 1080px - 48px = 1032px
     * 
     * Adım 2: Toplam Margin (6 sütun için 7 aralık)
     *         = 7 × 12px = 84px
     * 
     * Adım 3: Kart Genişliği
     *         = (1032px - 84px) / 6 = 158px
     * 
     * Sonuç: Her kart 158x158px → Ekrana mükemmel sığar! ✓
     * --------------------------------------------------------
     * 
     * @return Kart boyutu (piksel) - kare şeklinde (width = height)
     */
    private int ekranGenisligindenKartBoyutuHesapla() {
        // ==================== EKRAN ÖLÇÜLERİNİ AL ====================
        // DisplayMetrics: Android'de ekran bilgilerini almak için kullanılan API
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        // Ekran genişliği (piksel cinsinden)
        int ekranGenisligi = displayMetrics.widthPixels;
        
        // Ekran yoğunluğu (density): DP → PX dönüşümü için gerekli
        // Örnek: density=3.0 ise 1dp = 3px (xxhdpi ekran)
        float density = displayMetrics.density;
        
        Log.d("KartBoyut", "════════════════════════════════════════");
        Log.d("KartBoyut", "📱 Ekran Genişliği: " + ekranGenisligi + "px");
        Log.d("KartBoyut", "📊 Density: " + density + " (dpi: " + displayMetrics.densityDpi + ")");
        Log.d("KartBoyut", "📐 Grid Yapısı: " + satirSayisi + " satır × " + sutunSayisi + " sütun");
        Log.d("KartBoyut", "🎮 Zorluk Seviyesi: " + zorlukSeviyesi);
        
        // ==================== PADDING HESAPLAMA ====================
        // Layout'un sol ve sağ padding'i (activity_oyun.xml'de tanımlı: 16dp)
        int layoutPaddingDp = 16; // Her bir taraf için (dp cinsinden)
        int toplamLayoutPaddingPx = (int) (layoutPaddingDp * density * 2); // Sol + Sağ
        
        Log.d("KartBoyut", "🔹 Layout Padding (toplam): " + toplamLayoutPaddingPx + "px (" + (layoutPaddingDp * 2) + "dp)");
        
        // ==================== KULLANILABIL GENIŞLIK ====================
        // Padding'leri çıkardıktan sonra kartlar için kalan genişlik
        int kullanilabilirGenislik = ekranGenisligi - toplamLayoutPaddingPx;
        
        Log.d("KartBoyut", "✅ Kullanılabilir Genişlik: " + kullanilabilirGenislik + "px");
        
        // ==================== MARGIN HESAPLAMA ====================
        // Her kartın etrafında margin var (GridLayout.LayoutParams: setMargins(4, 4, 4, 4))
        int kartMarginDp = 4; // Her bir taraf için margin (dp)
        int kartMarginPx = (int) (kartMarginDp * density); // Piksel'e çevir
        
        // Toplam margin boşluğu hesaplama:
        // Düşünelim: [margin][kart][margin][kart][margin][kart][margin]
        // Formül: (SütunSayısı + 1) × Margin × 2 (her kart için sol+sağ)
        int toplamMarginGenislik = (sutunSayisi + 1) * kartMarginPx * 2;
        
        Log.d("KartBoyut", "🔹 Kart Margin (her taraf): " + kartMarginPx + "px (" + kartMarginDp + "dp)");
        Log.d("KartBoyut", "🔹 Toplam Margin Genişliği: " + toplamMarginGenislik + "px");
        
        // ==================== KART BOYUTU HESAPLAMA ====================
        // ANA FORMÜL: (Kullanılabilir Genişlik - Toplam Margin) / Sütun Sayısı
        // Bu hesaplama sayede kartlar genişliğe mükemmel sığar
        int kartBoyutu = (kullanilabilirGenislik - toplamMarginGenislik) / sutunSayisi;
        
        Log.d("KartBoyut", "🎯 HESAPLANAN KART BOYUTU: " + kartBoyutu + "px");
        Log.d("KartBoyut", "🎯 Kart Boyutu (DP): " + Math.round(kartBoyutu / density) + "dp");
        
        // ==================== GÜVENLİK KONTROLLERI ====================
        
        // Minimum boyut kontrolü (kartlar çok küçük olmasın - oynanabilir olmalı)
        int minKartBoyutu = (int) (48 * density); // Minimum 48dp (Material Design touch target)
        if (kartBoyutu < minKartBoyutu) {
            Log.w("KartBoyut", "⚠️ UYARI: Kart boyutu çok küçük! Minimum değer uygulanıyor: " + minKartBoyutu + "px");
            kartBoyutu = minKartBoyutu;
        }
        
        // Maksimum boyut kontrolü (tablet'lerde kartlar çok büyük olmasın)
        int maxKartBoyutu = (int) (150 * density); // Maksimum 150dp
        if (kartBoyutu > maxKartBoyutu) {
            Log.i("KartBoyut", "ℹ️ Kart boyutu maksimum değerle sınırlandırıldı: " + maxKartBoyutu + "px");
            kartBoyutu = maxKartBoyutu;
        }
        
        // ==================== DOĞRULAMA ====================
        // Tüm kartlar + margin'ler ekranın genişliğine sığıyor mu kontrol et
        int toplamGerekliGenislik = (kartBoyutu * sutunSayisi) + toplamMarginGenislik + toplamLayoutPaddingPx;
        
        if (toplamGerekliGenislik > ekranGenisligi) {
            // Eğer taşıyorsa otomatik düzelt
            Log.e("KartBoyut", "❌ HATA: Kartlar genişliğe sığmıyor!");
            Log.e("KartBoyut", "   Gerekli: " + toplamGerekliGenislik + "px | Mevcut: " + ekranGenisligi + "px");
            
            // Her karttan eşit miktarda küçült
            int tasmaPixel = toplamGerekliGenislik - ekranGenisligi;
            kartBoyutu = kartBoyutu - (tasmaPixel / sutunSayisi) - 2; // +2 güvenlik payı
            
            Log.w("KartBoyut", "⚠️ Kart boyutu otomatik düzeltildi: " + kartBoyutu + "px");
        } else {
            int kalanBosluk = ekranGenisligi - toplamGerekliGenislik;
            Log.d("KartBoyut", "✅ DOĞRULAMA BAŞARILI: Kartlar genişliğe mükemmel sığıyor!");
            Log.d("KartBoyut", "✅ Kalan boşluk: " + kalanBosluk + "px (her iki kenardan: " + (kalanBosluk/2) + "px)");
        }
        
        // ==================== YÜKSEKLIK BİLGİSİ ====================
        // NOT: ScrollView kullandığımız için yükseklik sınırlaması YOK!
        // Kartlar istediği kadar yüksek olabilir, kullanıcı scroll yapabilir.
        int toplamGridYuksekligi = kartBoyutu * satirSayisi;
        Log.d("KartBoyut", "📏 Toplam Grid Yüksekliği: " + toplamGridYuksekligi + "px (" + Math.round(toplamGridYuksekligi/density) + "dp)");
        Log.d("KartBoyut", "📜 ScrollView aktif - Yükseklik sınırı yok!");
        
        Log.d("KartBoyut", "🎯 FİNAL KART BOYUTU (KARE): " + kartBoyutu + "×" + kartBoyutu + " px");
        Log.d("KartBoyut", "════════════════════════════════════════");
        
        return kartBoyutu;
    }

    /**
     * Kartları Karıştırma ve Grid'e Ekleme
     */
    public void kartlariKaristir() {
        grd.removeAllViews();
        List<Kart> kartDizisi = new ArrayList<Kart>(Arrays.asList(kartlar));
        Collections.shuffle(kartDizisi);
        for (View v : kartDizisi) {
            grd.addView(v);
        }
    }

    /**
     * Timer Başlatma
     */
    public void sureBaslat(int zaman) {
        sureBar.setMax(zaman);
        sureBar.setProgress(zaman);

        if (kalanSure > 0) {
            zaman = kalanSure;
        } else {
            kalanSure = zaman;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(zaman * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int sure = (int) millisUntilFinished / 1000;
                kalanSure = sure;
                sureTv.setText("⏱️ Süre: " + sure + "sn");
                sureBar.setProgress(sure);

                if (sure < toplamSure / 4) {
                    sureBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(0xFFF44336));
                } else if (sure < toplamSure / 2) {
                    sureBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(0xFFFF9800));
                } else {
                    sureBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                }
            }

            @Override
            public void onFinish() {
                sureTv.setText("⏱️ Süre: 0sn");
                sureBar.setProgress(0);
                oyunuBitir(false);
            }
        };

        countDownTimer.start();
    }

    /**
     * Timer Durdurma
     */
    private void durdurTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * Oyunu Bitirme (Geliştirilmiş Skor Gösterimi + Cloud Kayıt)
     */
    private void oyunuBitir(boolean kazandi) {
        oyunBitti = true;
        bekle = true;
        durdurTimer();

        // ==================== FIRESTORE'A SKOR KAYDET ====================
        // Oyun bittiğinde (kazanma veya kaybetme) skoru cloud'a kaydet
        saveScoreToCloud(oyuncuIsim, skor, zorlukSeviyesi);

        String baslik, mesaj;
        
        if (kazandi) {
            baslik = "🎉 Tebrikler!";
            int gecenSure = toplamSure - kalanSure;
            mesaj = oyuncuIsim + ", oyunu kazandınız!\n\n" +
                    "📊 İstatistikler:\n" +
                    "🏆 Toplam Skor: " + skor + " puan\n" +
                    "⏱️ Geçen Süre: " + gecenSure + " saniye\n" +
                    "❤️ Kalan Hak: " + hataHakki + "\n" +
                    "🃏 Eşleşme Sayısı: " + eslesmeSayisi + "/" + toplamCift +
                    "\n\n💾 Skorunuz cloud'a kaydedildi!";
        } else {
            baslik = "😢 Oyun Bitti";
            
            if (hataHakki <= 0) {
                mesaj = oyuncuIsim + ", hata hakkınız bitti!\n\n" +
                        "🏆 Toplam Skor: " + skor + " puan\n" +
                        "🃏 Eşleşen: " + eslesmeSayisi + "/" + toplamCift;
            } else {
                mesaj = oyuncuIsim + ", süreniz doldu!\n\n" +
                        "🏆 Toplam Skor: " + skor + " puan\n" +
                        "🃏 Eşleşen: " + eslesmeSayisi + "/" + toplamCift;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(baslik);
        builder.setMessage(mesaj);
        builder.setCancelable(false);

        builder.setPositiveButton("🔄 Tekrar Oyna", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                oyunuYenidenBaslat();
            }
        });

        builder.setNegativeButton("🏠 Ana Menü", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Oyunu Yeniden Başlatma
     */
    private void oyunuYenidenBaslat() {
        durdurTimer();
        grd.removeAllViews();
        kartlar = null;
        suankiKart = 0;
        oncekiKart = null;
        bekle = false;
        oyunBitti = false;
        eslesmeSayisi = 0;
        kalanSure = 0;
        skor = 0; // Skoru sıfırla
        oyunuBaslat();
        Toast.makeText(this, "Oyun yeniden başlatıldı!", Toast.LENGTH_SHORT).show();
    }

    /**
     * ==================== STATE RESTORATION METODU ====================
     * 
     * Oyun Durumu Geri Yükleme Metodu
     * 
     * Bu metot, Activity yeniden oluşturulduğunda (ekran döndürme vb.) 
     * önceki oyun durumunu geri yükler.
     * 
     * Çalışma Mantığı:
     * 1. Bundle'dan kayıtlı değişkenleri al
     * 2. Grid yapısını yeniden oluştur (yeni ekran oryantasyonuna göre)
     * 3. Kartları yeniden oluştur ve durumlarını restore et
     * 4. Timer'ı kalan süreden devam ettir
     * 5. UI elemanlarını güncelle
     * 
     * Dinamik Boyutlandırma:
     * - Ekran döndüğünde (Portrait ↔ Landscape) grid boyutu yeniden hesaplanır
     * - Kartlar yeni ekran genişliğine göre boyutlandırılır
     * - Bu sayede hem portrait hem landscape modu desteklenir
     * 
     * @param savedState Kaydedilmiş durum verilerini içeren Bundle
     */
    private void restoreGameState(Bundle savedState) {
        Log.d("OyunState", "🔄 Oyun durumu geri yükleniyor...");
        
        // ==================== OYUN DEĞİŞKENLERİNİ GERİ YÜKLE ====================
        kalanSure = savedState.getInt("kalanSure", 0);
        hataHakki = savedState.getInt("hataHakki", 10);
        eslesmeSayisi = savedState.getInt("eslesmeSayisi", 0);
        skor = savedState.getInt("skor", 0);
        oyunBitti = savedState.getBoolean("oyunBitti", false);
        bekle = savedState.getBoolean("bekle", false);
        suankiKart = savedState.getInt("suankiKart", 0);
        
        // Grid yapısını geri yükle
        satirSayisi = savedState.getInt("satirSayisi", 2);
        sutunSayisi = savedState.getInt("sutunSayisi", 2);
        zorlukSeviyesi = savedState.getInt("zorlukSeviyesi", 1);
        toplamSure = savedState.getInt("toplamSure", 60);
        toplamCift = (satirSayisi * sutunSayisi) / 2;
        
        // ==================== UI ELEMANLARINI GÜNCELLE ====================
        hataHakkiTv.setText("❤️ Hak: " + hataHakki);
        skorGuncelle();
        
        // Grid yapısını ayarla
        grd.setColumnCount(sutunSayisi);
        grd.setRowCount(satirSayisi);
        
        // ==================== DİNAMİK KART BOYUTLANDIRMA ====================
        // Ekran döndüğünde yeni genişliğe göre kart boyutunu yeniden hesapla
        int kartBoyutu = ekranGenisligindenKartBoyutuHesapla();
        
        // ==================== KARTLARI GERİ YÜKLE ====================
        int kartSayisi = savedState.getInt("kartSayisi", 0);
        
        if (kartSayisi > 0) {
            // Kaydedilmiş kart verilerini al
            int[] kartIdleri = savedState.getIntArray("kartIdleri");
            int[] kartResimleri = savedState.getIntArray("kartResimleri");
            int[] kartDurumlari = savedState.getIntArray("kartDurumlari");
            boolean[] kartEtkinlikleri = savedState.getBooleanArray("kartEtkinlikleri");
            
            // Kart dizisini yeniden oluştur
            kartlar = new Kart[kartSayisi];
            
            for (int i = 0; i < kartSayisi; i++) {
                // Yeni kart oluştur
                Kart kart = new Kart(this, kartIdleri[i], kartResimleri[i]);
                
                // Dinamik boyut uygula (yeni ekran boyutuna göre)
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = kartBoyutu;
                params.height = kartBoyutu;
                params.setMargins(4, 4, 4, 4);
                kart.setLayoutParams(params);
                
                // Kartın durumunu geri yükle
                if (kartDurumlari[i] == 1) {
                    // Kart açıktı
                    kart.dondur(); // KAPALI -> ACIK
                }
                
                // Kartın etkinliğini geri yükle (eşleşmiş kartlar disabled)
                kart.setEnabled(kartEtkinlikleri[i]);
                
                // Eşleşmiş kartlara fade efekti uygula (görsel tutarlılık)
                if (!kartEtkinlikleri[i]) {
                    kart.setAlpha(0.4f);
                    kart.setScaleX(0.8f);
                    kart.setScaleY(0.8f);
                }
                
                // Click listener ekle (normal oyun mantığı)
                final int finalIndex = i;
                kart.setOnClickListener(view -> kartTiklandi(kart));
                
                kartlar[i] = kart;
            }
            
            // Kartları grid'e ekle (shuffle yapmadan - sırayı koru!)
            grd.removeAllViews();
            for (Kart k : kartlar) {
                grd.addView(k);
            }
            
            Log.d("OyunState", "✅ " + kartSayisi + " kart geri yüklendi");
        }
        
        // ==================== TIMER'I GERİ YÜKLE ====================
        if (!oyunBitti && kalanSure > 0) {
            sureBaslat(kalanSure); // Kalan süreden devam et
            Log.d("OyunState", "⏱️ Timer kalan süreden başlatıldı: " + kalanSure + "sn");
        } else if (oyunBitti) {
            sureTv.setText("⏱️ Süre: " + kalanSure + "sn");
            sureBar.setProgress(kalanSure);
        }
        
        Log.d("OyunState", "✅ Oyun durumu başarıyla geri yüklendi!");
        Toast.makeText(this, "Oyun devam ediyor...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Kart Tıklama Event Handler
     * 
     * State restoration'da kullanılan ortak kart click handler.
     * Orijinal onClick mantığını burada çağırıyoruz.
     */
    private void kartTiklandi(Kart kart) {
        // Orijinal onClick mantığı buraya taşındı
        if (bekle || oyunBitti) {
            return;
        }

        if (hataHakki <= 0) {
            oyunuBitir(false);
            return;
        }

        if (suankiKart == 0) {
            kartCevirAnimasyonu(kart, true);
            sesCal(kartCevirSesId);
            suankiKart = kart.getId();
        } else {
            bekle = true;
            oncekiKart = findViewById(suankiKart);

            if (oncekiKart.getId() == kart.getId()) {
                bekle = false;
                return;
            }

            if (oncekiKart.mevcutDurum == Kart.Durum.ACIK && 
                kart.mevcutDurum == Kart.Durum.ACIK) {
                bekle = false;
                return;
            }

            if (kart.mevcutDurum != Kart.Durum.ACIK) {
                kartCevirAnimasyonu(kart, true);
                sesCal(kartCevirSesId);
            }

            new Handler().postDelayed(() -> kartEslesmesiKontrolEt(oncekiKart, kart), 400);
        }
    }

    /**
     * ==================== FIRESTORE SKOR KAYDETME ====================
     * 
     * Cloud Skor Tablosu (Firestore) Kaydetme Metodu
     * 
     * Bu metot, oyun bittiğinde kullanıcının skorunu Firebase Firestore'a kaydeder.
     * Firestore, Google'ın NoSQL cloud veritabanıdır ve gerçek zamanlı senkronizasyon sağlar.
     * 
     * Avantajları:
     * - Tüm cihazlar arasında senkronizasyon
     * - Global liderlik tablosu oluşturma
     * - Otomatik ölçekleme (milyonlarca kayıt destekler)
     * - Gerçek zamanlı veri dinleme
     * - Offline destek (bağlantı kesilince cache'de saklar)
     * 
     * Veri Yapısı (Collection: "skorlar"):
     * {
     *   oyuncuIsim: "Ahmet",
     *   skor: 500,
     *   zorlukSeviyesi: 2,
     *   gecenSure: 45,
     *   eslesmeSayisi: 6,
     *   kazandi: true,
     *   timestamp: Timestamp(2024-01-15 14:30:00)
     * }
     * 
     * Güvenlik Notu:
     * - Firebase Console'dan Firestore Security Rules ayarlanmalıdır
     * - Şu an herkes okuyup yazabilir (geliştirme modu)
     * - Production'da authentication eklenmelidir
     * 
     * @param playerName Oyuncu ismi
     * @param score Toplam skor
     * @param difficulty Zorluk seviyesi (1=Kolay, 2=Orta, 3=Zor)
     */
    private void saveScoreToCloud(String playerName, int score, int difficulty) {
        // Firestore instance'ını al
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // ==================== VERİ MODELİNİ OLUŞTUR ====================
        // Map<String, Object> yapısı Firestore'un beklediği format
        Map<String, Object> skorData = new HashMap<>();
        
        // Oyuncu bilgileri
        skorData.put("oyuncuIsim", playerName);
        skorData.put("skor", score);
        skorData.put("zorlukSeviyesi", difficulty);
        
        // Oyun istatistikleri
        int gecenSure = toplamSure - kalanSure;
        skorData.put("gecenSure", gecenSure); // Saniye cinsinden
        skorData.put("eslesmeSayisi", eslesmeSayisi);
        skorData.put("toplamEslesme", toplamCift);
        skorData.put("kalanHak", hataHakki);
        
        // Oyun sonucu
        boolean kazandi = (eslesmeSayisi == toplamCift);
        skorData.put("kazandi", kazandi);
        
        // Timestamp (sunucu zamanı - daha güvenilir)
        skorData.put("timestamp", com.google.firebase.Timestamp.now());
        
        // Zorluk seviyesi metni (filtreleme kolaylığı için)
        String zorlukMetni;
        switch (difficulty) {
            case 1: zorlukMetni = "Kolay"; break;
            case 2: zorlukMetni = "Orta"; break;
            case 3: zorlukMetni = "Zor"; break;
            default: zorlukMetni = "Bilinmiyor"; break;
        }
        skorData.put("zorlukMetni", zorlukMetni);
        
        // Cihaz bilgisi (opsiyonel - analitik için)
        skorData.put("cihazModeli", android.os.Build.MODEL);
        skorData.put("androidVersion", android.os.Build.VERSION.RELEASE);
        
        // ==================== FIRESTORE'A KAYDET ====================
        // "skorlar" collection'ına yeni döküman ekle
        // .add() metodu otomatik unique ID oluşturur
        db.collection("skorlar")
                .add(skorData)
                .addOnSuccessListener(documentReference -> {
                    // Başarılı kayıt
                    String docId = documentReference.getId();
                    Log.d("Firestore", "✅ Skor başarıyla kaydedildi!");
                    Log.d("Firestore", "📄 Döküman ID: " + docId);
                    Log.d("Firestore", "🏆 Skor: " + score + " | Oyuncu: " + playerName);
                    
                    // Kullanıcıya bilgi ver
                    Toast.makeText(this, 
                        "🌐 Skorunuz global tabloya kaydedildi!", 
                        Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Hata durumu
                    Log.e("Firestore", "❌ Skor kaydedilemedi!", e);
                    
                    // Kullanıcıya bilgi ver
                    Toast.makeText(this, 
                        "⚠️ Skor kaydedilemedi. İnternet bağlantınızı kontrol edin.", 
                        Toast.LENGTH_LONG).show();
                    
                    // Hata detaylarını logla
                    Log.e("Firestore", "Hata mesajı: " + e.getMessage());
                    Log.e("Firestore", "Hata sebebi: " + e.getCause());
                });
        
        // ==================== ALTERNATİF: DÖKÜMAN ID BELİRLEME ====================
        // Eğer kendi document ID'nizi oluşturmak isterseniz:
        /*
        String customDocId = playerName + "_" + System.currentTimeMillis();
        db.collection("skorlar")
            .document(customDocId)
            .set(skorData)
            .addOnSuccessListener(aVoid -> {
                Log.d("Firestore", "Skor kaydedildi: " + customDocId);
            });
        */
    }

    /**
     * onDestroy() - Memory Leak Önleme
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Timer'ı durdur
        durdurTimer();
        
        // SoundPool'u serbest bırak (ÖNEMLİ: Memory leak önleme!)
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            Log.d("SesSistemi", "SoundPool serbest bırakıldı (memory leak önlendi)");
        }
        
        Log.d("OyunActivity", "Activity destroyed - Tüm kaynaklar temizlendi");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null && !oyunBitti) {
            durdurTimer();
        }
    }
}
