package tr.edu.atauni.yeniproje;



import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class Bildirim extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FBToken",token);
    }
    //Bildirim ile alınan tokenı sunucuya gönderen metodu yazınız.

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FBMesaj",message.getNotification().getTitle());
        Log.d("FBMesaj",message.getNotification().getBody());
        String icerik = message.getNotification().getBody();
        String baslik = message.getNotification().getTitle();

        NotificationChannel kanal= new NotificationChannel("bildiri","bildiri",NotificationManager.IMPORTANCE_HIGH);
        kanal.enableVibration(true);
        kanal.setVibrationPattern(new long[]{1000,1000,0,0,1000,0,1000});


        Notification bildiri= new Notification.Builder(getApplicationContext())
                .setChannelId("bildiri")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(baslik)
                .setContentText(icerik)
                .build();

        NotificationManager nm= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        nm.createNotificationChannel(kanal);

        nm.notify(new Random().nextInt(),bildiri);

    }
}
