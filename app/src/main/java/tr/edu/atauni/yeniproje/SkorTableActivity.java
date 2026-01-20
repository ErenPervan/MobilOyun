package tr.edu.atauni.yeniproje;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Skor Tablosu Activity
 * 
 * Firebase Firestore'dan en yüksek 10 skoru çeker ve gösterir.
 * 
 * Firestore Query:
 * - Collection: "skorlar"
 * - Order: "skor" field (descending - büyükten küçüğe)
 * - Limit: 10
 * 
 * @author Senior Android UI/UX Engineer
 */
public class SkorTableActivity extends AppCompatActivity {

    // UI Elemanları
    private TextView txtSkorListesi;
    private ProgressBar progressBar;
    private Button btnGeriDon;
    
    // Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skor_table);

        // ==================== UI ELEMANLARINI BAĞLA ====================
        txtSkorListesi = findViewById(R.id.txtSkorListesi);
        progressBar = findViewById(R.id.progressBar);
        btnGeriDon = findViewById(R.id.btnGeriDon);

        // Firestore instance'ını al
        db = FirebaseFirestore.getInstance();

        // Geri dön butonu
        btnGeriDon.setOnClickListener(v -> finish());

        // Skorları yükle
        loadScoresFromFirestore();
    }

    /**
     * Firestore'dan Top 10 Skorları Yükleme Metodu
     * 
     * Asenkron olarak Firestore'dan veri çeker.
     * Başarılı olursa UI'ı günceller, hata olursa kullanıcıya bildirir.
     */
    private void loadScoresFromFirestore() {
        // Progress bar'ı göster
        progressBar.setVisibility(View.VISIBLE);
        txtSkorListesi.setText("Skorlar yükleniyor...");

        // Firestore Query oluştur
        // collection("skorlar"): "skorlar" koleksiyonundan veri çek
        // orderBy("skor", Query.Direction.DESCENDING): "skor" field'ına göre büyükten küçüğe sırala
        // limit(10): En fazla 10 döküman getir
        db.collection("skorlar")
            .orderBy("skor", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    // Progress bar'ı gizle
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // ✅ Başarılı - Skorları göster
                        displayScores(task.getResult());
                    } else {
                        // ❌ Hata - Hata mesajı göster
                        Log.e("Firestore", "Skorlar yüklenemedi!", task.getException());
                        txtSkorListesi.setText("❌ Skorlar yüklenemedi.\n\nİnternet bağlantınızı kontrol edin.");
                        Toast.makeText(SkorTableActivity.this, 
                            "Firestore'dan veri çekilemedi!", 
                            Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    /**
     * Skorları UI'da Gösterme Metodu
     * 
     * Firestore'dan gelen QuerySnapshot'i parse eder ve TextView'de gösterir.
     * 
     * @param querySnapshot Firestore'dan dönen query sonucu
     */
    private void displayScores(QuerySnapshot querySnapshot) {
        if (querySnapshot == null || querySnapshot.isEmpty()) {
            // Hiç skor yok
            txtSkorListesi.setText("📊 Henüz kayıtlı skor yok.\n\nİlk skoru siz kaydedin!");
            return;
        }

        // StringBuilder ile skor listesini oluştur
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 TOP 10 SKORLAR\n");
        sb.append("═══════════════════════════\n\n");

        int sira = 1;
        
        // QuerySnapshot'teki tüm dökümanları gez
        for (QueryDocumentSnapshot document : querySnapshot) {
            // Firestore dökümanından veri al
            // document.getString("fieldName"): String field
            // document.getLong("fieldName"): Number field (Long olarak döner)
            String oyuncuIsim = document.getString("oyuncuIsim");
            Long skorLong = document.getLong("skor");
            Long zorlukLong = document.getLong("zorlukSeviyesi");
            Boolean kazandi = document.getBoolean("kazandi");

            // Null kontrolü
            if (oyuncuIsim == null) oyuncuIsim = "Bilinmiyor";
            int skor = (skorLong != null) ? skorLong.intValue() : 0;
            int zorluk = (zorlukLong != null) ? zorlukLong.intValue() : 1;

            // Zorluk emoji
            String zorlukEmoji;
            switch (zorluk) {
                case 1: zorlukEmoji = "🟢 Kolay"; break;
                case 2: zorlukEmoji = "🟡 Orta"; break;
                case 3: zorlukEmoji = "🔴 Zor"; break;
                default: zorlukEmoji = "⚪ ?"; break;
            }

            // Kazanma durumu emoji
            String durumEmoji = (kazandi != null && kazandi) ? "✅" : "❌";

            // Madalya emojileri (ilk 3 için)
            String medaliyon;
            if (sira == 1) medaliyon = "🥇";
            else if (sira == 2) medaliyon = "🥈";
            else if (sira == 3) medaliyon = "🥉";
            else medaliyon = sira + ".";

            // Skor satırını oluştur
            sb.append(medaliyon).append(" ")
              .append(oyuncuIsim).append("\n")
              .append("   🏆 ").append(skor).append(" puan | ")
              .append(zorlukEmoji).append(" ")
              .append(durumEmoji).append("\n")
              .append("───────────────────────────\n");

            sira++;
        }

        // TextView'e yazdır
        txtSkorListesi.setText(sb.toString());

        Log.d("SkorTable", "✅ " + (sira - 1) + " skor başarıyla gösterildi");
    }

    /**
     * Skorları Yenileme Metodu (Opsiyonel)
     * 
     * Kullanıcı "Yenile" butonuna basarsa skorları tekrar yükler.
     * (activity_skor_table.xml'e yenile butonu ekleyebilirsiniz)
     */
    public void refreshScores(View view) {
        loadScoresFromFirestore();
        Toast.makeText(this, "Skorlar yenileniyor...", Toast.LENGTH_SHORT).show();
    }
}
