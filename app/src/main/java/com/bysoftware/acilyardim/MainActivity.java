package com.bysoftware.acilyardim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.bysoftware.acilyardim.LoginActivity.checkLocationPermission;
import static com.bysoftware.acilyardim.LoginActivity.login_ad;
import static com.bysoftware.acilyardim.LoginActivity.login_durum;
import static com.bysoftware.acilyardim.LoginActivity.login_id;
import static com.bysoftware.acilyardim.LoginActivity.login_kimlikno;
import static com.bysoftware.acilyardim.LoginActivity.login_mail;
import static com.bysoftware.acilyardim.LoginActivity.login_sifre;
import static com.bysoftware.acilyardim.LoginActivity.login_soyad;
import static com.bysoftware.acilyardim.LoginActivity.login_telefon;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button button;
    ImageView logout, settings, zoom;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFragment;
    Location location;
    public static AlertDialog alert;
    double lat;
    double lng;
    String adress;
    List<Address> addresses;
    FusedLocationProviderClient client;
    public static final String MyPREFERENCES = "MyPrefs";
    private LocationChangeReceiver receiver;
    DBHandler dbHandler;
    TextInputEditText isim, kimlik, tel, mail;

    @SuppressLint("UseCheckPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //id leri tanımlıyoruz
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.yardim);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        isim = findViewById(R.id.kullanici_adi);
        kimlik = findViewById(R.id.kimlik_no);
        tel = findViewById(R.id.tel);
        mail = findViewById(R.id.email);
        zoom = findViewById(R.id.zoom);
        //LoginActivtiy sınıfındaki anlık konum alınamazsa gösterilecek olan uyarı mesajını çağırıyoruz
        LoginActivity.alertDialog(this);
        //veri tabanımızı çağırıyoruz
        dbHandler = new DBHandler(MainActivity.this);
        dbHandler.open();

        //konum ekranını büyültme butonuna tıklanıldığında olacaklar
        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //konum haritasını gösteren MapsActivity sınıfını açıyoruz
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        //kullanıcı bilgilerindeki ayarlar butonuna tıklanıldığında olacaklar
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kullanıcı bilgilerinin düzenleneceği EditProfileActivity sınıfını açıyoruz
                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            }
        });

        //çıkış yapma butonuna tıklanıldığında olacaklar
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uyarı mesajı gösteriyoruz
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Oturum Kapatılıyor");
                builder.setMessage("Çıkış yapmak istediğinize emin misiniz?");
                //evet butonuna tıklanırsa
                builder.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Daha sonra uygulama açıldığında giriş ekranına yönlendirilmesi için
                        //shared preferences ile oturumun kapatıldığını kaydediyoruz
                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("id", "0");
                        editor.commit();
                        //giriş ekranını açıyoruz
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                builder.setPositiveButton("Hayır", null);
                builder.show();
            }
        });
        //LoginActivitiy sınıfındaki konum izninin alınıp alınmadığını kontrol eden checkLocationPermission metodunu çağırıyoruz
        checkLocationPermission(MainActivity.this);
        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //shared preferences ile kaydettiğimiz kimlik numarasını alıyoruz
        SharedPreferences sharedpreferences1 = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String kullanici_kimligi = sharedpreferences1.getString("tc", "");
        //kimlik numarasına göre GetUser metoduna göre diğer bilgileri alıyoruz
        Cursor cursor = dbHandler.GetUser(kullanici_kimligi);

        if (cursor != null && cursor.getCount() > 0) {
            login_kimlikno = cursor.getString(cursor.getColumnIndexOrThrow("kimlikno"));
            login_ad = cursor.getString(cursor.getColumnIndexOrThrow("ad"));
            login_soyad = cursor.getString(cursor.getColumnIndexOrThrow("soyad"));
            login_telefon = cursor.getString(cursor.getColumnIndexOrThrow("telefon"));
            login_sifre = cursor.getString(cursor.getColumnIndexOrThrow("sifre"));
            login_mail = cursor.getString(cursor.getColumnIndexOrThrow("mail"));
            login_durum = cursor.getString(cursor.getColumnIndexOrThrow("durum"));
            login_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        }

        //alınan bilgilere göre kişisel bilgiler kısmındaki edittext leri dolduruyoruz
        isim.setText(login_ad + " " + login_soyad);
        kimlik.setText(login_kimlikno);
        mail.setText(login_mail);
        tel.setText(login_telefon);

        //aşağıda bahsedeceğimiz son konum bilgisini alabilmek için getLocation metodunu çağırıyoruz
        getLocation();
        // yardım çağır butonuna tıklanıldığında olacaklar
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tekrar getLocation metodunu çağırıyoruz
                getLocation();
                //adres boş değilse
                if (adress != null) {
                    //uyarı mesajı gösteriyoruz
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Yardım Mesajı");
                    builder.setMessage("Yardım mesajı göndermek istediğinize emin misiniz?");
                    //evet seçeneğine tıklanırsa
                    builder.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // MailSender sınıfı ile gerekli bilgileri mail olarak gönderiyoruz
                            MailSender.toast = "Yardım mesajınız güvenlik birimlerine ulaştırıldı!";
                            MailSender.title = "Yardım Bildiriliyor";
                            MailSender.message = "Lütfen Bekleyiniz...";
                            MailSender mg = new MailSender(MainActivity.this, "erkanm11t@gmail.com",
                                    login_ad + " " + login_soyad + " Yardım İstiyor!", login_kimlikno +
                                    " kimlik numaralı " + login_ad + " " + login_soyad + " kişisi," + "\n\n" +
                                    adress + "\n(" + "lat: " + lat + " long: " + lng + ")" + "\n\nkonumunda yardım istiyor." +
                                    "\n\n" + "Telefon numarası: " + login_telefon +
                                    "\n\n" + "Mail Adresi: " + login_mail);
                            // Mail Gönderme işlemini başlatıyoruz
                            mg.execute();
                        }
                    });
                    //hayır seçeneğine tıklanırsa uyarı mesajını kapatıyoruz
                    builder.setPositiveButton("Hayır", null);
                    builder.show();
                    //adres boşsa aşağıdaki mesajı gösteriyoruz
                } else {
                    Toast.makeText(MainActivity.this, "Konum bilgileri alınamadı.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //konum haritasının gösterilebilmesi için id tanımlıyoruz
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
    }

    //konum haritasında gösterilecek bilgiler
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //LatLng latLng = new LatLng(-34, 151);
        mGoogleMap = googleMap;
        //konum izni alınmadıysa izin alıyoruz
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        //konum alındıysa
        if (client != null) {
            @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //lokasyon bilgileri alındıysa
                    if (location != null) {
                        //harita üzerinde konumu gösteriyoruz
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Buradayım!");
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        googleMap.addMarker(markerOptions);
                        //alınamadıysa aşağıdaki toast mesajını gösteriyoruz
                    } else {
                        Toast.makeText(MainActivity.this, "Konum bilgisi alınamadı", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //konum bilgisini alıp şehir,ülke,mahalle şeklinde yazıya döktüğümüz metod
    @SuppressLint("MissingPermission")
    public void getLocation() {
        //son konumu alıyoruz
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                //alındıysa
                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        //konum bilgilerini yazıya döküyoruz
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        String newtext = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getSubLocality() + " Mahallesi, " + addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                        adress = newtext;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onDestroy() { //Ekran Kapatıldığı zaman konum kontrolü durduralacak.
        Log.v("A", "onDestory");
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);//durduruluyor
        }

    }
}