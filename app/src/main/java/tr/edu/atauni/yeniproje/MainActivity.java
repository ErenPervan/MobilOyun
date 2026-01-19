package tr.edu.atauni.yeniproje;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Ana Ekran Activity Sınıfı
 * 
 * Bu Activity oyunun giriş ekranıdır. Kullanıcıdan isim alır, zorluk seviyesi seçtirtir
 * ve oyunu başlatır. Ayrıca Firebase bildirim entegrasyonu ve HTTP istekleri içerir.
 * 
 * Öğrenme Notları:
 * - Activity yaşam döngüsü (lifecycle)
 * - SQLite veritabanı işlemleri
 * - OkHttp ile asenkron network işlemleri
 * - Firebase Cloud Messaging (FCM) entegrasyonu
 * - Android izin yönetimi (Runtime Permissions)
 * 
 * @author Yazılım Mühendisliği Öğrencisi
 */
public class MainActivity extends AppCompatActivity {

    // ==================== ÜYE DEĞİŞKENLER ====================
    
    dbHelper oyundb; // Veritabanı yardımcı sınıfı
    int seciliZorlukSeviyesi = 0; // Seçili zorluk seviyesi (0=seçilmedi, 1=kolay, 2=orta, 3=zor)
    
    // UI Elemanları (View referansları)
    TextView bilgiTxtV;
    EditText isimTxt;
    Button kolay, orta, zor, oynaButon;

    /**
     * Bildirim İzni İçin ActivityResultLauncher
     * 
     * Android 13 (API 33) ve üzeri versiyonlarda bildirim göndermek için
     * runtime'da kullanıcıdan izin almak zorunludur.
     * Bu launcher, izin isteği sonucunu callback olarak alır.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Kullanıcı izin verdi
                    Log.d("FBMesaj", "Bildirim izni verildi");
                } else {
                    // Kullanıcı izin vermedi - uyarı göster
                    Toast.makeText(this, "Bildirim izni vermelisiniz", Toast.LENGTH_LONG).show();
                }
            });

    /**
     * onCreate() - Activity yaşam döngüsünün başlangıcı
     * 
     * Activity ilk oluşturulduğunda çalışır. UI elemanları burada başlatılır,
     * veritabanı bağlantısı kurulur, click listener'lar atanır.
     * 
     * @param savedInstanceState Önceki durumdan kaydedilmiş veriler (varsa)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Modern Android UI: Ekranı tam ekran (edge-to-edge) yapar
        EdgeToEdge.enable(this);
        
        // Layout XML dosyasını Activity'ye bağla
        setContentView(R.layout.activity_main);

        // ==================== SYSTEM BARS PADDING AYARLAMA ====================
        // Modern Android'de sistem çubukları (status bar, navigation bar) altına içerik girebilir
        // Bu kod bloğu, içeriğin sistem çubuklarının altında kalmaması için padding ekler
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ==================== UI ELEMANLARINI BAĞLAMA ====================
        // findViewById() ile XML'deki view'ları Java koduna bağlıyoruz
        bilgiTxtV = findViewById(R.id.bilgiTv);
        isimTxt = findViewById(R.id.isimEdtTxt);
        oynaButon = findViewById(R.id.oyunaBaslaBtn);
        kolay = findViewById(R.id.kolayBtn);
        orta = findViewById(R.id.ortaBtn);
        zor = findViewById(R.id.zorBtn);

        // ==================== VERİTABANI BAŞLATMA ====================
        // dbHelper sınıfının instance'ını oluştur
        // Constructor'a Context geçilerek veritabanı bağlantısı kurulur
        oyundb = new dbHelper(this);

        // ==================== KAYITLI VERİLERİ OKUMA ====================
        
        // 1. Kullanıcının kayıtlı ismini veritabanından oku ve EditText'e yerleştir
        String kayitliIsim = oyundb.isimOku();
        if (kayitliIsim != null && !kayitliIsim.isEmpty()) {
            // Null safety: İsim varsa kutuya yaz (kullanıcı deneyimi için)
            isimTxt.setText(kayitliIsim);
        }

        // 2. Kayıtlı zorluk seviyesini oku (0=seçilmedi, 1-3 arası değer)
        seciliZorlukSeviyesi = oyundb.zorlukSeviyesiOku();

        // 3. Tüm ayarları oku (opsiyonel - debug/log amaçlı)
        Cursor cr = oyundb.ayarlariOku();
        if (cr != null) {
            // Cursor üzerinde döngü ile tüm satırları gez
            while (cr.moveToNext()) {
                // Sütun indexleri: 0:id, 1:name, 2:value
                // İsterseniz burada log veya toast ile ayarları gösterebilirsiniz
                // String name = cr.getString(1);
                // String val = cr.getString(2);
                // Log.d("Ayarlar", "Ayar: " + name + " = " + val);
            }
            // ÖNEMLİ: Cursor kullanımı bittikten sonra MUTLAKA kapatılmalı!
            // Aksi halde bellek sızıntısı (memory leak) oluşur
            cr.close();
        }

        // ==================== ZORLUK SEVİYESİ BUTONLARI ====================
        
        // Başlangıç durumu: Tüm butonları varsayılan renge (mavi) ayarla
        butonlariSifirla();

        // Veritabanından okunan zorluk seviyesine göre ilgili butonu aktif (gri) yap
        if (seciliZorlukSeviyesi == 1) {
            butonSec(kolay);
        } else if (seciliZorlukSeviyesi == 2) {
            butonSec(orta);
        } else if (seciliZorlukSeviyesi == 3) {
            butonSec(zor);
        }

        // KOLAY Butonu Click Listener
        // Lambda expression kullanılarak modern Java syntax
        kolay.setOnClickListener(view -> {
            seciliZorlukSeviyesi = 1; // Değişkeni güncelle
            oyundb.zorlukSeviyesiKaydet(1); // Veritabanına kaydet
            butonSec(kolay); // Buton renklerini güncelle (bu buton gri, diğerleri mavi)
        });

        // ORTA Butonu Click Listener
        orta.setOnClickListener(view -> {
            seciliZorlukSeviyesi = 2;
            oyundb.zorlukSeviyesiKaydet(2);
            butonSec(orta);
        });

        // ZOR Butonu Click Listener
        zor.setOnClickListener(view -> {
            seciliZorlukSeviyesi = 3;
            oyundb.zorlukSeviyesiKaydet(3);
            butonSec(zor);
        });

        // ==================== OYUNA BAŞLA BUTONU ====================
        oynaButon.setOnClickListener(view -> {
            // Validasyon 1: Zorluk seviyesi seçilmiş mi?
            if (seciliZorlukSeviyesi == 0) {
                Toast.makeText(getApplicationContext(), 
                    "Lütfen bir zorluk seviyesi seçiniz", 
                    Toast.LENGTH_SHORT).show();
                return; // Metottan çık (oyun başlatılmasın)
            }

            // Kullanıcının girdiği ismi al ve boşlukları temizle
            String oyuncuIsim = isimTxt.getText().toString().trim();
            
            // Validasyon 2: İsim en az 3 karakter mi?
            if (oyuncuIsim.length() >= 3) {
                // İsmi veritabanına kaydet
                oyundb.isimKaydet(oyuncuIsim);

                // HTTP POST isteği gönder (sunucuya kullanıcı bilgisi)
                // Bu metot asenkron çalışır (arka planda)
                httpPost2();

                // ==================== YENİ ACTIVITY'E GEÇİŞ ====================
                // Intent nesnesi oluştur - OyunActivity'ye geçiş için
                Intent actInt = new Intent(MainActivity.this, OyunActivity.class);
                
                // Intent ile veri taşıma (putExtra metodu ile)
                // Bu veriler OyunActivity'de getIntent().getStringExtra() ile alınacak
                actInt.putExtra("oyuncuIsm", oyuncuIsim);
                actInt.putExtra("zorlukSeviyesi", seciliZorlukSeviyesi);
                
                // Yeni Activity'yi başlat (ekran geçişi)
                startActivity(actInt);
                
                // Kullanıcıya bilgi mesajı
                Toast.makeText(MainActivity.this, 
                    "Oyun başlatılıyor... Bol şans " + oyuncuIsim + "!", 
                    Toast.LENGTH_SHORT).show();

            } else {
                // Validasyon hatası: İsim çok kısa
                Toast.makeText(getApplicationContext(), 
                    "Oyuncu ismi en az 3 karakter olmalı", 
                    Toast.LENGTH_LONG).show();
            }
        });

        // ==================== DİĞER BAŞLANGIÇ İŞLEMLERİ ====================
        
        // Bildirim izni iste (Android 13+)
        askNotificationPermission();
        
        // Firebase Cloud Messaging token'ını al (push notification için gerekli)
        firebaseTokenAl();
        
        // Notification Channel oluştur (Android 8.0+ için zorunlu)
        createNotificationChannel();
        
        // FCM Topic'e abone ol (broadcast bildirimler için)
        subscribeToFCMTopic();
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Butonları Sıfırlama Metodu
     * 
     * Tüm zorluk seviyesi butonlarını varsayılan renge (mavi) döndürür.
     * Bu metot, buton seçimi yapılmadan önce tüm butonları eşitlemek için kullanılır.
     */
    private void butonlariSifirla() {
        kolay.setBackgroundColor(Color.BLUE);
        orta.setBackgroundColor(Color.BLUE);
        zor.setBackgroundColor(Color.BLUE);
    }

    /**
     * Buton Seçme Metodu
     * 
     * Parametre olarak verilen butonu gri yapar (seçili görünüm),
     * diğer butonları mavi yapar (seçili değil görünüm).
     * 
     * Çalışma Mantığı:
     * 1. Önce tüm butonları sıfırla (hepsi mavi)
     * 2. Seçilen butonu gri yap
     * 
     * @param btn Seçilecek buton referansı
     */
    private void butonSec(Button btn) {
        butonlariSifirla(); // Önce hepsini varsayılan renge döndür
        btn.setBackgroundColor(Color.GRAY); // Seçili butonu gri yap
    }

    // ==================== FIREBASE İŞLEMLERİ ====================

    /**
     * Firebase Token Alma Metodu
     * 
     * Firebase Cloud Messaging (FCM) kullanarak cihaza özel bir token alır.
     * Bu token, sunucudan bu cihaza push notification göndermek için kullanılır.
     * 
     * Asenkron Çalışma:
     * - getToken() metodu arka planda çalışır
     * - addOnCompleteListener ile sonuç alınır (başarılı/başarısız)
     */
    private void firebaseTokenAl() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // Token alma başarısız - hata loglama
                        Log.w("FBMesaj", "Firebase token alma başarısız", task.getException());
                        
                        // Kullanıcıya bilgi ver (opsiyonel)
                        Toast.makeText(MainActivity.this, 
                            "Bildirim servisi başlatılamadı", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Token başarıyla alındı
                    String token = task.getResult();
                    Log.d("FBMesaj", "Firebase Token: " + token);
                    
                    // Bu token'ı kendi sunucunuza gönderebilirsiniz
                    // Böylece kullanıcıya bildirim gönderebilirsiniz
                });
    }

    /**
     * Notification Channel Oluşturma Metodu
     * 
     * Android 8.0 (API 26) Oreo ve üzeri versiyonlarda bildirim göstermek için
     * NotificationChannel oluşturmak ZORUNLUDUR.
     * 
     * NotificationChannel Nedir?
     * - Kullanıcının bildirimleri kategori bazında kontrol etmesini sağlar
     * - Örnek: "Duyurular", "Mesajlar", "Güncellemeler" gibi
     * - Kullanıcı her kanalı ayrı ayrı açıp kapatabilir, önem seviyesini değiştirebilir
     * 
     * Önem Seviyeleri:
     * - IMPORTANCE_HIGH: Bildirim sesi + ekranda popup gösterim (önemli duyurular için)
     * - IMPORTANCE_DEFAULT: Bildirim sesi + bildirim çubuğu
     * - IMPORTANCE_LOW: Sessiz bildirim
     * 
     * NOT: Kanal bir kez oluşturulduktan sonra, uygulama yeniden yüklense bile kalır.
     * Kanal ayarlarını değiştirmek için kullanıcı cihaz ayarlarından yapmalıdır.
     */
    private void createNotificationChannel() {
        // Android 8.0 (Oreo) ve üzeri için gerekli
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kanal ID'si: Kodda bildirim gönderirken bu ID kullanılacak
            String channelId = "duyuru_kanali";
            
            // Kanal İsmi: Kullanıcının cihaz ayarlarında göreceği isim
            CharSequence channelName = "Duyurular";
            
            // Kanal Açıklaması: Kullanıcı için detaylı bilgi
            String channelDescription = "Oyun duyuruları, güncellemeler ve özel etkinlik bildirimleri";
            
            // Önem Seviyesi: IMPORTANCE_HIGH (sesi var, ekranda gösterilir)
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            // NotificationChannel nesnesi oluştur
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            
            // Opsiyonel: Kanal özelliklerini özelleştir
            channel.enableVibration(true); // Titreşim aktif
            channel.setVibrationPattern(new long[]{100, 200, 300, 400}); // Titreşim deseni
            channel.enableLights(true); // LED ışık aktif (destekleyen cihazlarda)
            channel.setLightColor(android.graphics.Color.BLUE); // LED rengi
            channel.setShowBadge(true); // Uygulama ikonunda badge göster
            
            // NotificationManager ile kanalı sisteme kaydet
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("Notification", "✅ Notification Channel oluşturuldu: " + channelId);
            }
        } else {
            // Android 7.1 ve altında NotificationChannel gerekmez
            Log.d("Notification", "Android 7.1 veya altı - NotificationChannel gerekli değil");
        }
    }

    /**
     * FCM Topic Subscription (Konu Aboneliği) Metodu
     * 
     * Firebase Cloud Messaging (FCM) Topic sistemi, broadcast bildirimler göndermeyi sağlar.
     * 
     * Topic Nedir?
     * - Belirli bir konuya abone olan TÜM kullanıcılara aynı anda bildirim gönderme sistemi
     * - Örnek: "duyurular" topic'ine abone olan herkes güncellemeleri alır
     * - Her uygulama birden fazla topic'e abone olabilir
     * 
     * Kullanım Senaryoları:
     * - "duyurular": Genel duyurular
     * - "etkinlikler": Özel oyun etkinlikleri
     * - "promosyonlar": İndirim ve kampanyalar
     * - "guncellemeler": Yeni özellik duyuruları
     * 
     * Avantajları:
     * - Sunucu tarafında her kullanıcının token'ını saklamaya gerek yok
     * - Firebase Console'dan direkt topic'e bildirim gönderilebilir
     * - Otomatik ölçekleme (1 milyon kullanıcıya bile aynı anda gönderebilir)
     * 
     * NOT: Topic aboneliği cihazda saklanır, uygulama kapansa bile kalır.
     */
    private void subscribeToFCMTopic() {
        // "duyurular" topic'ine abone ol
        String topicName = "duyurular";
        
        FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Abonelik başarılı
                        Log.d("FCMTopic", "✅ '" + topicName + "' topic'ine başarıyla abone olundu!");
                        
                        // Kullanıcıya bilgi ver (opsiyonel)
                        // Toast.makeText(this, "Duyurular aktif!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Abonelik başarısız
                        Log.e("FCMTopic", "❌ Topic aboneliği başarısız: " + topicName, task.getException());
                    }
                });
        
        // İsterseniz birden fazla topic'e abone olabilirsiniz:
        // FirebaseMessaging.getInstance().subscribeToTopic("etkinlikler");
        // FirebaseMessaging.getInstance().subscribeToTopic("promosyonlar");
        
        // Topic'den çıkmak için (unsubscribe):
        // FirebaseMessaging.getInstance().unsubscribeFromTopic("duyurular");
    }

    /**
     * Bildirim İzni İsteme Metodu
     * 
     * Android 13 (API 33) ve üzeri versiyonlarda bildirim göstermek için
     * runtime'da kullanıcıdan izin almak zorunludur.
     * 
     * Çalışma Mantığı:
     * 1. SDK versiyonu kontrol edilir
     * 2. İzin zaten verilmişse işlem yapılmaz
     * 3. İzin verilmemişse kullanıcıya izin dialog'u gösterilir
     */
    private void askNotificationPermission() {
        // Android 13 (Tiramisu) ve üzeri kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // İzin durumunu kontrol et
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED) {
                // İzin zaten var, işlem yapma
                Log.d("FBMesaj", "Bildirim izni zaten verilmiş");
            } else {
                // İzin yok, kullanıcıdan iste
                // requestPermissionLauncher callback'inde sonuç alınacak
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        // Android 12 ve altında bildirim izni otomatik verilir
    }

    // ==================== NETWORK İŞLEMLERİ (OkHttp) ====================

    /**
     * Kullanıcı Okuma (GET İsteği) Metodu
     * 
     * Sunucudan HTTP GET isteği ile veri çeker.
     * OkHttp kütüphanesi kullanılarak asenkron (arka planda) çalışır.
     * 
     * ÖNEMLİ: Network işlemleri ASLA main thread'de (UI thread) yapılmamalıdır!
     * OkHttp'nin enqueue() metodu otomatik olarak arka planda çalışır.
     * UI güncellemeleri için runOnUiThread() kullanılmalıdır.
     */
    public void kullaniciOku() {
        // OkHttpClient nesnesi oluştur (network istekleri için)
        OkHttpClient istemci = new OkHttpClient();
        
        // Request nesnesi oluştur (GET isteği)
        Request req = new Request.Builder()
                .url("https://dbs.atauni.edu.tr/yazilim.php?get=3")
                .build();
        
        // Asenkron istek gönder (arka planda çalışır)
        istemci.newCall(req).enqueue(new Callback() {
            /**
             * İstek Başarısız Olduğunda Çalışır
             * Örnek: İnternet bağlantısı yok, sunucu yanıt vermiyor
             */
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("NetworkHata", "GET isteği başarısız: " + e.getMessage());
                
                // UI güncellemesi yapılacaksa main thread'e geçilmeli
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "Bağlantı hatası! İnternet bağlantınızı kontrol edin.", 
                        Toast.LENGTH_LONG).show();
                });
            }

            /**
             * İstek Başarılı Olduğunda Çalışır
             * NOT: Bu metot arka planda çalışır (worker thread)
             */
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Response body'yi kontrol et
                ResponseBody body = response.body();
                
                if (body != null) {
                    try {
                        // Sunucudan gelen cevabı string olarak al
                        String cevap = body.string();
                        
                        // UI güncellemesi MUTLAKA main thread'de yapılmalı
                        runOnUiThread(() -> {
                            bilgiTxtV.setText(cevap);
                        });
                    } finally {
                        // ÖNEMLİ: ResponseBody kullanıldıktan sonra MUTLAKA kapatılmalı
                        // Aksi halde bellek sızıntısı oluşur
                        body.close();
                    }
                } else {
                    // Body boş geldi (sunucu hatası)
                    Log.w("NetworkHata", "Response body boş geldi");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, 
                            "Sunucu yanıt vermedi", 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * HTTP POST İsteği Metodu
     * 
     * Kullanıcı bilgilerini (isim ve zorluk seviyesi) sunucuya POST isteği ile gönderir.
     * OkHttp kütüphanesi kullanılarak asenkron (arka planda) çalışır.
     * 
     * Gönderilen Veri Formatı: application/x-www-form-urlencoded
     * Örnek: UserName=Ahmet&Zorluk=2
     */
    public void httpPost2() {
        // OkHttpClient nesnesi oluştur
        OkHttpClient istemci = new OkHttpClient();
        
        // POST isteği için body içeriği hazırla
        // Form data formatında: key=value&key2=value2
        String content = "UserName=" + isimTxt.getText().toString() 
                       + "&Zorluk=" + seciliZorlukSeviyesi;

        // RequestBody oluştur (MediaType: form-urlencoded)
        RequestBody govde = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), 
                content
        );
        
        // Request nesnesi oluştur (POST metodu)
        Request istek = new Request.Builder()
                .url("https://dbs.atauni.edu.tr/yazilim.php")
                .post(govde) // POST isteği olduğunu belirt
                .build();

        // Asenkron istek gönder (arka planda çalışır)
        istemci.newCall(istek).enqueue(new Callback() {
            /**
             * İstek Başarısız Olduğunda Çalışır
             * 
             * NOT: Hata mesajları sadece log'a yazılır, kullanıcıya Toast gösterilmez.
             * Çünkü bu HTTP isteği oyunun çalışması için zorunlu değildir (isteğe bağlı analitik).
             * Kullanıcı deneyimini bozmamak için sessizce hata loglanır.
             */
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("NetworkHata", "❌ POST isteği başarısız: " + e.getMessage());
                Log.e("NetworkHata", "Bu hata kullanıcı deneyimini etkilemez (isteğe bağlı özellik).");
                
                // Toast KALDIRILDI - Kullanıcı rahatsız olmayacak
                // Geliştirici Logcat'ten hata görebilir
            }

            /**
             * İstek Başarılı Olduğunda Çalışır
             * NOT: Bu metot arka planda çalışır (worker thread)
             */
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Response body'yi kontrol et
                ResponseBody body = response.body();
                
                if (body != null) {
                    try {
                        // Sunucudan gelen cevabı string olarak al
                        String cevap = body.string();
                        
                        // HTTP status code kontrolü (200 = başarılı)
                        if (response.isSuccessful()) {
                            Log.d("Network", "✅ POST başarılı: " + cevap);
                            Log.d("Network", "Kullanıcı verileri sunucuya gönderildi (analitik).");
                            
                            // Toast KALDIRILDI - Kullanıcıya gereksiz bildirim göstermeyelim
                            // Oyun zaten başladı, ekstra mesaj rahatsız edici olur
                        } else {
                            // HTTP hatası (404, 500, vb.)
                            Log.w("NetworkHata", "⚠️ HTTP Hata Kodu: " + response.code());
                            Log.w("NetworkHata", "Muhtemel sebep: URL yanlış veya sunucu yanıt vermiyor.");
                            Log.w("NetworkHata", "Bu hata oyunu ETKİLEMEZ (isteğe bağlı özellik).");
                            
                            // Toast KALDIRILDI - "Sunucu hatası: 404" mesajı artık görünmeyecek!
                            // Kullanıcı oyun oynarken bu hata ile rahatsız edilmemeli
                        }
                    } finally {
                        // ÖNEMLİ: ResponseBody kullanıldıktan sonra MUTLAKA kapatılmalı
                        body.close();
                    }
                } else {
                    Log.w("NetworkHata", "⚠️ Response body boş geldi.");
                    Log.w("NetworkHata", "Sunucu bağlantı kurdu ama veri göndermedi.");
                    
                    // Toast KALDIRILDI - Kullanıcı bu teknik detayı görmemeli
                }
            }
        });
    }
}
