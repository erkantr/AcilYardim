package com.bysoftware.acilyardim;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

import static com.bysoftware.acilyardim.LoginActivity.dialog;
import static com.bysoftware.acilyardim.MainActivity.alert;

//anlık lokasyon kontrol sınıfı
public class LocationChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // aşağıdaki islocationEnabled metodu ile konumun açık olup olmadığını kontrol ediyoruz
            if (isLocationEnabled(context)) {
                //açıksa
                //uyarı mesajını kapatıyoruz
                dialog.cancel();
                Log.e("location", "Connect ");
                //değilse
            } else {
                //uyarı mesajı gösteriyoruz
                dialog.show();
                Log.e("location", "Location Failure !!!");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //lokasyonun aktif olup olmadığını kontrol eden method
    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }
}
