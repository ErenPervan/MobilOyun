package tr.edu.atauni.yeniproje;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Veritabanı Yönetici Sınıfı
 * 
 * Bu sınıf, oyun ayarlarını ve skor bilgilerini yerel SQLite veritabanında saklar.
 * SQLiteOpenHelper sınıfından türetilmiştir ve veritabanı oluşturma/güncelleme işlemlerini yönetir.
 * 
 * @author Yazılım Mühendisliği Öğrencisi
 */
public class dbHelper extends SQLiteOpenHelper {
    
    // Veritabanı adı ve versiyonu
    public static String DB_NAME = "oyun.db";
    public static int versiyon = 3;

    /**
     * Constructor - Veritabanı bağlantısını başlatır
     * @param cntx Uygulama context'i
     */
    public dbHelper(Context cntx) {
        super(cntx, DB_NAME, null, versiyon);
    }

    /**
     * onCreate() - Veritabanı ilk kez oluşturulduğunda çalışır
     * 
     * İki tablo oluşturulur:
     * 1. ayarlar: Kullanıcı ismi ve zorluk seviyesi gibi ayarları saklar
     * 2. skorlar: Oyun skorlarını saklar
     * 
     * @param db SQLiteDatabase nesnesi
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // NOT: SQLite'da veri tipi olarak STRING değil TEXT kullanılır (standart)
        // PRIMARY KEY AUTOINCREMENT ile her kayda otomatik artan ID atanır
        db.execSQL("CREATE TABLE ayarlar(ID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT)");
        db.execSQL("CREATE TABLE skorlar(ID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, value INTEGER)");

        // Başlangıç varsayılan verilerini ekle
        // Kullanıcı ilk açılışta "Oyuncu" ismiyle ve zorluk seviyesi "2" (orta) ile başlar
        db.execSQL("INSERT INTO ayarlar(name,value) VALUES('isim','Oyuncu')");
        db.execSQL("INSERT INTO ayarlar(name,value) VALUES('zorlukSeviyesi','2')");
    }

    /**
     * onUpgrade() - Veritabanı versiyonu güncellendiğinde çalışır
     * 
     * Örnek: Versiyon 2'den 3'e güncellendiğinde yeni bir ayar ekleniyor
     * 
     * @param db SQLiteDatabase nesnesi
     * @param oldVersion Eski versiyon numarası
     * @param newVersion Yeni versiyon numarası
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 2 && newVersion == 3) {
            // Eğer kullanıcı eski versiyondan güncelleme yapıyorsa, eksik ayarı ekle
            // NOT: Eğer ayar zaten varsa hata verebilir, production'da try-catch kullanılabilir
            db.execSQL("INSERT INTO ayarlar (name,value) VALUES('zorlukSeviyesi','2')");
        }
    }

    /**
     * İsim Okuma Metodu
     * 
     * Veritabanından kullanıcının kayıtlı ismini getirir.
     * Eğer kayıt yoksa varsayılan olarak "Oyuncu" döner.
     * 
     * @return Kullanıcının kayıtlı ismi
     */
    public String isimOku() {
        SQLiteDatabase db = getReadableDatabase();
        String sorgu = "SELECT id,name,value FROM ayarlar WHERE name='isim'";
        Cursor crs = db.rawQuery(sorgu, null);

        String sonuc = "Oyuncu"; // Varsayılan değer (kayıt yoksa)
        
        // Cursor null kontrolü ve veri var mı kontrolü
        // moveToFirst() metodu Cursor'ı ilk satıra taşır ve veri varsa true döner
        if (crs != null && crs.moveToFirst()) {
            // 2. index (value kolonu) - 0:id, 1:name, 2:value
            sonuc = crs.getString(2);
            // ÖNEMLİ: Cursor kullanımı bittikten sonra MUTLAKA kapatılmalıdır!
            // Aksi halde bellek sızıntısı (memory leak) oluşur
            crs.close();
        }
        
        // NOT: getReadableDatabase() ile alınan db nesnesi otomatik yönetilir,
        // bu yüzden db.close() çağırmaya gerek yoktur (SQLiteOpenHelper yönetir)
        return sonuc;
    }

    /**
     * İsim Kaydetme Metodu
     * 
     * Kullanıcının girdiği ismi veritabanına kaydeder.
     * 
     * DÜZELTME: rawQuery yerine execSQL kullanılmıştır (UPDATE için doğru yöntem)
     * SQL Injection saldırılarına karşı parameterized query (?) kullanılmıştır
     * 
     * @param oyuncuIsim Kaydedilecek oyuncu ismi
     */
    public void isimKaydet(String oyuncuIsim) {
        SQLiteDatabase db = getWritableDatabase();
        
        // Güvenli yöntem: ? işareti SQL Injection saldırılarını engeller
        // Örnek: Kullanıcı "'; DROP TABLE ayarlar; --" gibi zararlı kod girerse
        // parameterized query sayesinde bu sadece text olarak işlenir
        String sorgu = "UPDATE ayarlar SET value = ? WHERE name = 'isim'";
        db.execSQL(sorgu, new Object[]{oyuncuIsim});
    }

    /**
     * Zorluk Seviyesi Okuma Metodu
     * 
     * Veritabanından kayıtlı zorluk seviyesini getirir.
     * 1 = Kolay, 2 = Orta, 3 = Zor
     * 
     * @return Zorluk seviyesi (1-3 arası integer)
     */
    public int zorlukSeviyesiOku() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor crs = db.rawQuery("SELECT value FROM ayarlar WHERE name='zorlukSeviyesi'", null);

        int sonuc = 1; // Varsayılan: Kolay seviye
        
        // Cursor kontrolü ve veri okuma
        if (crs != null && crs.moveToFirst()) {
            // 0. index'teki değeri integer olarak al (value kolonu)
            sonuc = crs.getInt(0);
            crs.close(); // Bellek yönetimi için Cursor'ı kapat
        }
        
        return sonuc;
    }

    /**
     * Zorluk Seviyesi Kaydetme Metodu
     * 
     * Kullanıcının seçtiği zorluk seviyesini veritabanına kaydeder.
     * 
     * DÜZELTME: Önceki versiyonda boşluk eksikliği vardı, düzeltildi
     * SQL Injection'a karşı güvenli parameterized query kullanılmıştır
     * 
     * @param seviye Zorluk seviyesi (1=Kolay, 2=Orta, 3=Zor)
     */
    public void zorlukSeviyesiKaydet(int seviye) {
        SQLiteDatabase db = getWritableDatabase();
        
        // Güvenli UPDATE sorgusu - ? işareti ile parametre bağlama
        db.execSQL("UPDATE ayarlar SET value = ? WHERE name = 'zorlukSeviyesi'", new Object[]{seviye});
    }

    /**
     * Tüm Ayarları Okuma Metodu
     * 
     * Veritabanındaki tüm ayarları Cursor olarak döner.
     * DİKKAT: Bu metot Cursor döndüğü için çağıran tarafta MUTLAKA close() edilmelidir!
     * 
     * Kullanım Örneği:
     * Cursor cr = oyundb.ayarlariOku();
     * if (cr != null) {
     *     while (cr.moveToNext()) {
     *         String name = cr.getString(1);
     *         String value = cr.getString(2);
     *     }
     *     cr.close(); // MUTLAKA KAPAT!
     * }
     * 
     * @return Tüm ayarları içeren Cursor nesnesi
     */
    public Cursor ayarlariOku() {
        SQLiteDatabase db = getReadableDatabase();
        // SELECT * ile tüm kolonları getir: ID, name, value
        return db.rawQuery("SELECT * FROM ayarlar", null);
    }

    /**
     * Genel Ayar Okuma Metodu
     * 
     * Belirtilen ayarın değerini döner.
     * 
     * @param ayarAdi Ayar ismi (örn: "isim", "zorluk", "tema")
     * @return Ayar değeri (String), bulunamazsa boş string
     */
    public String ayarOku(String ayarAdi) {
        SQLiteDatabase db = getReadableDatabase();
        String sorgu = "SELECT value FROM ayarlar WHERE name = ?";
        Cursor crs = db.rawQuery(sorgu, new String[]{ayarAdi});

        String sonuc = "";
        
        if (crs != null && crs.moveToFirst()) {
            sonuc = crs.getString(0);
            crs.close();
        }
        
        return sonuc;
    }

    /**
     * Genel Ayar Kaydetme Metodu
     * 
     * Belirtilen ayarı veritabanına kaydeder veya günceller.
     * Eğer ayar yoksa yeni kayıt oluşturur (INSERT), varsa günceller (UPDATE).
     * 
     * @param ayarAdi Ayar ismi (örn: "tema", "zorluk")
     * @param deger Ayar değeri
     */
    public void ayarKaydet(String ayarAdi, String deger) {
        SQLiteDatabase db = getWritableDatabase();
        
        // Önce ayar var mı kontrol et
        Cursor crs = db.rawQuery("SELECT value FROM ayarlar WHERE name = ?", new String[]{ayarAdi});
        
        if (crs != null && crs.moveToFirst()) {
            // Ayar mevcut, güncelle
            db.execSQL("UPDATE ayarlar SET value = ? WHERE name = ?", new Object[]{deger, ayarAdi});
            crs.close();
        } else {
            // Ayar yok, yeni kayıt oluştur
            db.execSQL("INSERT INTO ayarlar (name, value) VALUES (?, ?)", new Object[]{ayarAdi, deger});
            if (crs != null) crs.close();
        }
    }

    /**
     * Skor Kaydetme Metodu
     * 
     * Oyun bittiğinde kullanıcının skorunu veritabanına kaydeder.
     * 
     * @param kullaniciAdi Kullanıcı adı
     * @param puan Elde edilen puan
     */
    public void skorKaydet(String kullaniciAdi, int puan) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO skorlar (username, value) VALUES (?, ?)", 
                   new Object[]{kullaniciAdi, puan});
    }

    /**
     * Skorları Getirme Metodu
     * 
     * En yüksek skorları sıralı olarak getirir (büyükten küçüğe).
     * 
     * @return Skorları içeren Cursor (username, value kolonları)
     */
    public Cursor skorlariGetir() {
        SQLiteDatabase db = getReadableDatabase();
        // ORDER BY value DESC: Skorları büyükten küçüğe sırala
        // LIMIT 10: En iyi 10 skoru getir
        return db.rawQuery("SELECT username, value FROM skorlar ORDER BY value DESC LIMIT 10", null);
    }
}
