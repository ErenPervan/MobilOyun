package tr.edu.atauni.yeniproje;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tema Yönetim Yardımcı Sınıfı (3 Tema Destekli - Güncellenmiş)
 * 
 * Bu sınıf, kullanıcının seçtiği temayı SharedPreferences'a kaydeder ve okur.
 * SharedPreferences, Android'de basit key-value çiftlerini kalıcı olarak saklamak için kullanılır.
 * 
 * Temalar (Her tema 32 resim içerir - 8x8 grid desteği):
 * - 0: Kartlar Teması (kart1.png - kart32.png)
 * - 1: Hayvanlar Teması (hayvan1.png - hayvan32.png)
 * - 2: İkonlar Teması (icon1.png - icon32.png)
 * 
 * Kullanım:
 * - Tema kaydetme: ThemeHelper.saveSelectedTheme(context, 1);
 * - Tema okuma: int theme = ThemeHelper.getSelectedTheme(context);
 * 
 * @author Senior Android Engineer
 */
public class ThemeHelper {

    // SharedPreferences dosya adı
    private static final String PREF_NAME = "GameThemePrefs";
    
    // Tema key'i
    private static final String KEY_THEME = "selected_theme";
    
    // Varsayılan tema (0 = Kartlar Teması)
    private static final int DEFAULT_THEME = 0;
    
    // Tema sayısı (0, 1, 2 = 3 tema)
    public static final int THEME_COUNT = 3;

    /**
     * Seçilen Temayı Kaydetme Metodu
     * 
     * SharedPreferences'a tema indeksini kaydeder.
     * 
     * @param context Application context
     * @param themeIndex Tema indeksi (0 = Klasik, 1 = Özel)
     */
    public static void saveSelectedTheme(Context context, int themeIndex) {
        // SharedPreferences instance'ını al (MODE_PRIVATE = sadece bu uygulama erişebilir)
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Editor ile değeri kaydet
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME, themeIndex);
        
        // apply() metodu asenkron olarak kaydeder (commit() senkron - UI'ı bloklar)
        editor.apply();
    }

    /**
     * Seçilen Temayı Okuma Metodu
     * 
     * SharedPreferences'tan tema indeksini okur.
     * Eğer kayıtlı tema yoksa, varsayılan temayı döner (0).
     * 
     * @param context Application context
     * @return Tema indeksi (0 veya 1)
     */
    public static int getSelectedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // getInt(key, defaultValue)
        // Eğer key yoksa, defaultValue döner
        return prefs.getInt(KEY_THEME, DEFAULT_THEME);
    }

    /**
     * Tema Sıfırlama Metodu (Opsiyonel)
     * 
     * Temayı varsayılan değere (0) sıfırlar.
     * 
     * @param context Application context
     */
    public static void resetTheme(Context context) {
        saveSelectedTheme(context, DEFAULT_THEME);
    }

    /**
     * Tema Bilgisi Alma Metodu (3 Tema için Güncellenmiş)
     * 
     * Seçili temanın ismini döner (UI'da göstermek için kullanışlı).
     * 
     * @param context Application context
     * @return Tema ismi (String)
     */
    public static String getThemeName(Context context) {
        int theme = getSelectedTheme(context);
        switch (theme) {
            case 0: return "🎴 Kartlar";
            case 1: return "🦁 Hayvanlar";
            case 2: return "🎨 İkonlar";
            default: return "Bilinmeyen Tema";
        }
    }

    /**
     * Tema Resim Dizisi Alma Metodu (Dinamik - Tüm Dosyalar Destekleniyor!)
     * 
     * NOT: Bu metot artık OyunActivity'deki dinamik yükleme ile uyumlu.
     * Dosya sayıları:
     * - Kartlar: 33 dosya (kart1-33.png)
     * - Hayvanlar: 34 dosya (hayvan1-34.png)
     * - İkonlar: 34 dosya (icon1-34.png)
     * 
     * @param context Application context
     * @return Kart resimleri dizisi (int[])
     * @deprecated OyunActivity artık loadThemeImages() ile dinamik yükleme yapıyor
     */
    public static int[] getThemeImages(Context context) {
        int theme = getSelectedTheme(context);
        String prefix;
        int maxCount;
        
        switch (theme) {
            case 0:
                prefix = "kart";
                maxCount = 33; // kart1-33.png
                break;
            case 1:
                prefix = "hayvan";
                maxCount = 34; // hayvan1-34.png
                break;
            case 2:
                prefix = "icon";
                maxCount = 34; // icon1-34.png
                break;
            default:
                prefix = "kart";
                maxCount = 33;
                break;
        }
        
        // Dinamik olarak dosyaları bul
        java.util.ArrayList<Integer> images = new java.util.ArrayList<>();
        for (int i = 1; i <= maxCount; i++) {
            String imageName = prefix + i;
            int resId = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName()
            );
            if (resId != 0) {
                images.add(resId);
            }
        }
        
        // ArrayList'i int[] array'e çevir
        int[] result = new int[images.size()];
        for (int i = 0; i < images.size(); i++) {
            result[i] = images.get(i);
        }
        
        return result;
    }
}
