package tr.edu.atauni.yeniproje;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

/**
 * Kart Sınıfı - Tema Destekli (Refactored)
 * 
 * Bu sınıf, hafıza oyunundaki tek bir kartı temsil eder.
 * Kart iki duruma sahiptir: ACIK (ön yüz görünür) ve KAPALI (arka yüz görünür).
 * 
 * YENİ ÖZELLİK: Tema Desteği
 * - Artık kart resimleri dinamik olarak tema dizisinden alınır
 * - Hardcoded if-else blokları yerine tema array'i kullanılır
 * - ThemeHelper ile entegre çalışır
 * 
 * @author Senior Android UI/UX Engineer (Refactored)
 */
public class Kart extends AppCompatButton {
    
    /**
     * Kart Durumu Enum
     * - ACIK: Kartın ön yüzü görünür (resim)
     * - KAPALI: Kartın arka yüzü görünür (arkaplan)
     */
    public static enum Durum {
        ACIK, KAPALI
    }
    
    public Durum mevcutDurum; // Şu anki durum
    private Drawable arkaPlan; // Arka yüz (kapalı kart görseli)
    private Drawable onPlan;   // Ön yüz (kart resmi)
    public int resId;          // Resim resource ID'si (eşleşme kontrolü için)
    
    /**
     * Kart Constructor - 3 TEMA SİSTEMİ (Güncellenmiş)
     * 
     * @param cnt Context
     * @param kartId Kartın unique ID'si
     * @param resimResourceId Kart resminin resource ID'si (R.drawable.xxx)
     */
    public Kart(Context cnt, int kartId, int resimResourceId) {
        super(cnt);
        
        // ==================== KART ARKA YÜZÜ (KAPALI DURUM) ====================
        // Tüm kartlarda aynı arka yüz kullanılır: kartarkaplan.png
        // Bu, kapalı kartın görseli (oyuncu henüz açmadığında gösterilen resim)
        try {
            arkaPlan = cnt.getDrawable(R.drawable.kartarkaplan);
            
            // Eğer kartarkaplan bulunamazsa, fallback olarak default icon kullan
            if (arkaPlan == null) {
                arkaPlan = cnt.getDrawable(android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            // Hata durumunda fallback: Android'in default ikonu
            arkaPlan = cnt.getDrawable(android.R.drawable.ic_menu_gallery);
        }
        
        // Başlangıçta arka planı göster (kart kapalı)
        setBackground(arkaPlan);
        
        // Başlangıç durumu: KAPALI
        mevcutDurum = Durum.KAPALI;
        
        // Kart ID'sini ata (View ID olarak kullanılır)
        setId(kartId);
        
        // Resim resource ID'sini kaydet (eşleşme kontrolü için)
        resId = resimResourceId;
        
        // ==================== KART ÖN YÜZÜ (AÇIK DURUM) ====================
        // Tema sisteminden alınan kart resmi (kart1-32, hayvan1-32, veya icon1-32)
        try {
            onPlan = cnt.getDrawable(resimResourceId);
            
            // Eğer drawable bulunamazsa, placeholder göster
            if (onPlan == null) {
                // Fallback: Android'in default yıldız ikonu
                onPlan = cnt.getDrawable(android.R.drawable.star_big_on);
            }
        } catch (Exception e) {
            // Hata durumunda placeholder kullan
            onPlan = cnt.getDrawable(android.R.drawable.star_big_on);
        }
    }
    
    /**
     * Kart Çevirme Metodu
     * 
     * Kartın durumunu değiştirir:
     * - KAPALI → ACIK: Arka plan yerine ön plan (resim) gösterilir
     * - ACIK → KAPALI: Resim yerine arka plan gösterilir
     * 
     * NOT: Animasyon OyunActivity'de kartCevirAnimasyonu() ile yapılır.
     * Bu metot sadece background'ı değiştirir.
     */
    public void dondur() {
        if (mevcutDurum == Durum.KAPALI) {
            // Kartı aç - resmi göster
            setBackground(onPlan);
            mevcutDurum = Durum.ACIK;
        } else {
            // Kartı kapat - arka planı göster
            setBackground(arkaPlan);
            mevcutDurum = Durum.KAPALI;
        }
    }
}

