package com.bysoftware.acilyardim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import static com.bysoftware.acilyardim.MainActivity.MyPREFERENCES;

//açılış ekranı
public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //shared preferences ile oturum durumunu alıyoruz
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                String kaydedilen_durum = sharedpreferences.getString("id", "");

                //konumun aktifliğini kontrol ediyoruz
                if (isLocationEnabled()) {
                    //oturum açıksa ana ekrana yönlendiriyoruz
                    if (kaydedilen_durum.equals("1")) {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        //değilse giriş ekranına yönlendiriyoruz
                    } else {
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                    }
                    //aktif değilse aşağıdaki mesajı gösteriyoruz
                } else {
                    Toast.makeText(SplashScreen.this, "Konum Bilgisi Alınamadı", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }, secondsDelayed * 1000);
    }

    // konumun açık olup olmadığını kontrol ediyoruz
    boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) SplashScreen.this.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }
}