package tr.edu.atauni.yeniproje;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class Kart extends AppCompatButton {
    public  static  enum Durum{
        ACIK,KAPALI
    }
    public Durum mevcutDurum;
    private Drawable arkaPlan;
    private Drawable onPlan;
    public int resId;
    public Kart(Context cnt,int kartId,int resimId)
    {
        super(cnt);
         arkaPlan = cnt.getDrawable(R.drawable.arkaplan);
         setBackground(arkaPlan);
         mevcutDurum = Durum.KAPALI;
         setId(kartId);
         resId=resimId;
        setBackground(arkaPlan);
        if(resimId==0){
            onPlan = cnt.getDrawable(R.drawable.kart1);
        }
        if(resimId==2){
            onPlan = cnt.getDrawable(R.drawable.kart2);
        }
        if(resimId==4){
            onPlan = cnt.getDrawable(R.drawable.kart3);
        }
        if(resimId==6){
            onPlan = cnt.getDrawable(R.drawable.kart4);
        }
        if(resimId==8){
            onPlan = cnt.getDrawable(R.drawable.kart5);
        }
        if(resimId==10){
            onPlan = cnt.getDrawable(R.drawable.kart6);
        }
        if(resimId==12){
            onPlan = cnt.getDrawable(R.drawable.kart7);
        }
        if(resimId==14){
            onPlan = cnt.getDrawable(R.drawable.kart8);
        }
        if(resimId==16){
            onPlan = cnt.getDrawable(R.drawable.kart9);
        }


    }
    public void dondur(){
        if(mevcutDurum==Durum.KAPALI){
            setBackground(onPlan);
            mevcutDurum= Durum.ACIK;
        }
        else {
            setBackground(arkaPlan);
            mevcutDurum = Durum.KAPALI;
        }
    }
}
