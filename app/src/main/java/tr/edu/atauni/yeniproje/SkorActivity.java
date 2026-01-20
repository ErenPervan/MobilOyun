package tr.edu.atauni.yeniproje;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SkorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skor);

        dbHelper db = new dbHelper(this);
        TextView tv = findViewById(R.id.txtSkorListesi);
        android.widget.Button btnMenu = findViewById(R.id.btnSkorMenu);
        btnMenu.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                finish(); // Bu sayfayı kapatır, alttaki Ana Menü görünür
            }
        });
        Cursor c = db.skorlariGetir();
        StringBuilder sb = new StringBuilder();
        int sira = 1;

        while(c.moveToNext()){
            sb.append(sira).append(". ")
                    .append(c.getString(0)).append(" : ") // İsim
                    .append(c.getInt(1)).append(" Puan\n"); // Puan
            sb.append("----------------\n");
            sira++;
        }

        if(sira == 1) tv.setText("Henüz kayıtlı skor yok.");
        else tv.setText(sb.toString());
    }
}