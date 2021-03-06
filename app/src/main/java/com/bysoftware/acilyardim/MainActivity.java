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
        //id leri tan??ml??yoruz
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.yardim);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        isim = findViewById(R.id.kullanici_adi);
        kimlik = findViewById(R.id.kimlik_no);
        tel = findViewById(R.id.tel);
        mail = findViewById(R.id.email);
        zoom = findViewById(R.id.zoom);
        //LoginActivtiy s??n??f??ndaki anl??k konum al??namazsa g??sterilecek olan uyar?? mesaj??n?? ??a????r??yoruz
        LoginActivity.alertDialog(this);
        //veri taban??m??z?? ??a????r??yoruz
        dbHandler = new DBHandler(MainActivity.this);
        dbHandler.open();

        //konum ekran??n?? b??y??ltme butonuna t??klan??ld??????nda olacaklar
        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //konum haritas??n?? g??steren MapsActivity s??n??f??n?? a????yoruz
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });

        //kullan??c?? bilgilerindeki ayarlar butonuna t??klan??ld??????nda olacaklar
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kullan??c?? bilgilerinin d??zenlenece??i EditProfileActivity s??n??f??n?? a????yoruz
                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
            }
        });

        //????k???? yapma butonuna t??klan??ld??????nda olacaklar
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uyar?? mesaj?? g??steriyoruz
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Oturum Kapat??l??yor");
                builder.setMessage("????k???? yapmak istedi??inize emin misiniz?");
                //evet butonuna t??klan??rsa
                builder.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Daha sonra uygulama a????ld??????nda giri?? ekran??na y??nlendirilmesi i??in
                        //shared preferences ile oturumun kapat??ld??????n?? kaydediyoruz
                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("id", "0");
                        editor.commit();
                        //giri?? ekran??n?? a????yoruz
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                builder.setPositiveButton("Hay??r", null);
                builder.show();
            }
        });
        //LoginActivitiy s??n??f??ndaki konum izninin al??n??p al??nmad??????n?? kontrol eden checkLocationPermission metodunu ??a????r??yoruz
        checkLocationPermission(MainActivity.this);
        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //shared preferences ile kaydetti??imiz kimlik numaras??n?? al??yoruz
        SharedPreferences sharedpreferences1 = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String kullanici_kimligi = sharedpreferences1.getString("tc", "");
        //kimlik numaras??na g??re GetUser metoduna g??re di??er bilgileri al??yoruz
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

        //al??nan bilgilere g??re ki??isel bilgiler k??sm??ndaki edittext leri dolduruyoruz
        isim.setText(login_ad + " " + login_soyad);
        kimlik.setText(login_kimlikno);
        mail.setText(login_mail);
        tel.setText(login_telefon);

        //a??a????da bahsedece??imiz son konum bilgisini alabilmek i??in getLocation metodunu ??a????r??yoruz
        getLocation();
        // yard??m ??a????r butonuna t??klan??ld??????nda olacaklar
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tekrar getLocation metodunu ??a????r??yoruz
                getLocation();
                //adres bo?? de??ilse
                if (adress != null) {
                    //uyar?? mesaj?? g??steriyoruz
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Yard??m Mesaj??");
                    builder.setMessage("Yard??m mesaj?? g??ndermek istedi??inize emin misiniz?");
                    //evet se??ene??ine t??klan??rsa
                    builder.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // MailSender s??n??f?? ile gerekli bilgileri mail olarak g??nderiyoruz
                            MailSender.toast = "Yard??m mesaj??n??z g??venlik birimlerine ula??t??r??ld??!";
                            MailSender.title = "Yard??m Bildiriliyor";
                            MailSender.message = "L??tfen Bekleyiniz...";
                            MailSender mg = new MailSender(MainActivity.this, "erkanm11t@gmail.com",
                                    login_ad + " " + login_soyad + " Yard??m ??stiyor!", login_kimlikno +
                                    " kimlik numaral?? " + login_ad + " " + login_soyad + " ki??isi," + "\n\n" +
                                    adress + "\n(" + "lat: " + lat + " long: " + lng + ")" + "\n\nkonumunda yard??m istiyor." +
                                    "\n\n" + "Telefon numaras??: " + login_telefon +
                                    "\n\n" + "Mail Adresi: " + login_mail);
                            // Mail G??nderme i??lemini ba??lat??yoruz
                            mg.execute();
                        }
                    });
                    //hay??r se??ene??ine t??klan??rsa uyar?? mesaj??n?? kapat??yoruz
                    builder.setPositiveButton("Hay??r", null);
                    builder.show();
                    //adres bo??sa a??a????daki mesaj?? g??steriyoruz
                } else {
                    Toast.makeText(MainActivity.this, "Konum bilgileri al??namad??.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //konum haritas??n??n g??sterilebilmesi i??in id tan??ml??yoruz
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
    }

    //konum haritas??nda g??sterilecek bilgiler
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //LatLng latLng = new LatLng(-34, 151);
        mGoogleMap = googleMap;
        //konum izni al??nmad??ysa izin al??yoruz
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        //konum al??nd??ysa
        if (client != null) {
            @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //lokasyon bilgileri al??nd??ysa
                    if (location != null) {
                        //harita ??zerinde konumu g??steriyoruz
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Buraday??m!");
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        googleMap.addMarker(markerOptions);
                        //al??namad??ysa a??a????daki toast mesaj??n?? g??steriyoruz
                    } else {
                        Toast.makeText(MainActivity.this, "Konum bilgisi al??namad??", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //konum bilgisini al??p ??ehir,??lke,mahalle ??eklinde yaz??ya d??kt??????m??z metod
    @SuppressLint("MissingPermission")
    public void getLocation() {
        //son konumu al??yoruz
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                //al??nd??ysa
                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        //konum bilgilerini yaz??ya d??k??yoruz
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
    protected void onDestroy() { //Ekran Kapat??ld?????? zaman konum kontrol?? durduralacak.
        Log.v("A", "onDestory");
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);//durduruluyor
        }

    }
}