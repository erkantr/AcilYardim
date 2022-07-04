package com.bysoftware.acilyardim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.bysoftware.acilyardim.MainActivity.MyPREFERENCES;

//giriş yap (login) sınıfı
public class LoginActivity extends AppCompatActivity {

    public static int login_id;
    public static String login_kimlikno;
    public static String login_ad;
    public static String login_soyad;
    public static String login_telefon;
    public static String login_sifre;
    public static String login_durum;
    public static String login_mail;
    public static AlertDialog dialog;

    EditText kimlik, password;
    Button login_btn;
    TextView forgot_password, registerbtn;
    DBHandler dbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //gerekli id leri tanımlıyoruz
        kimlik = findViewById(R.id.editTextKimlikno1);
        password = findViewById(R.id.editTextPassword1);
        login_btn = findViewById(R.id.login);
        registerbtn = findViewById(R.id.registernow);
        forgot_password = findViewById(R.id.paswword);
        //kimlik numarasını maksimum 11 haneli rakam olacak şekilde ayarlıyoruz
        kimlik.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        //veri tabanımızı işlem yapabilmek için açıyoruz
        dbHandler = new DBHandler(LoginActivity.this);
        dbHandler.open();
        //aşağıda bahsedeceğimiz metodları aktifleştiriyoruz
        alertDialog(this);
        checkLocationPermission(this);
        changeStatusBarColor();

        //shared preferences ile şifre unutma durumuna karşı daha önce kaydettiğimiz kimlik numarasından diğer bilgileri alıyoruz.
        SharedPreferences sharedpreferences1 = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String kullanici_kimligi = sharedpreferences1.getString("tc", "");
        Cursor cursor = dbHandler.GetUser(kullanici_kimligi);

        if (cursor != null && cursor.getCount() > 0) {
            login_ad = cursor.getString(cursor.getColumnIndexOrThrow("ad"));
            login_soyad = cursor.getString(cursor.getColumnIndexOrThrow("soyad"));
            login_sifre = cursor.getString(cursor.getColumnIndexOrThrow("sifre"));
            login_mail = cursor.getString(cursor.getColumnIndexOrThrow("mail"));
        }

        //şifremi unuttum butonuna tıklanıldığında olacaklar
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uyarı mesajı gösteriyoruz
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Şifre Hatırlatma");
                builder.setMessage("Şifre mail adresinize gönderilecek devam etmek istediğinize emin misiniz?");
                //evet butonuna tıklanırsa
                builder.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MailSender.toast = "Şifreniz mail adresinize gönderildi.";
                        MailSender.title = "Şifre Gönderiliyor";
                        MailSender.message = "Şifreniz mail adresinize gönderiliyor.";
                        //daha önce hesap oluşturulduysa
                        if (login_mail != null) {
                            //shared pereferences dan aldığımız bilgileri mail olarak gönderiyoruz
                            MailSender mg = new MailSender(LoginActivity.this, login_mail,
                                    "Acil Yardım Şifreniz", login_ad + " " + login_soyad +
                                    " Acil Yardım şifreniz: " + login_sifre + "\n\nLütfen kimseyle paylaşmayın.");
                            // Mail Gönderme işlemini başlattığımız nokta
                            mg.execute();
                            //oluşturulmadıysa aşağıdaki mesajı gösteriyoruz
                        } else {
                            Toast.makeText(LoginActivity.this, "Daha önceden oluşturulmuş hesabınız bulunmamaktadır.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setPositiveButton("Hayır", null);
                builder.show();
            }
        });

        //giriş yap butonuna tıklandığında olacaklar
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //girilen bilgileri alıyoruz
                String kimlikn = kimlik.getText().toString();
                String pass = password.getText().toString();
                if (kimlikn.equals("") || pass.equals("")) {
                    Toast.makeText(LoginActivity.this, "Tüm alanlar zorunludur", Toast.LENGTH_SHORT).show();
                    //boş değilse
                } else {
                    //bilgilerin doğru olup olmadığını DBHandler sınıfındaki checkTCandPassword metoduyla sorguluyoruz
                    if (dbHandler.checkTCandPassword(kimlikn, pass)) {
                        //doğruysa diğer tüm bilgileri GetUser metoduyla alıyoruz
                        Cursor cursor = dbHandler.GetUser(kimlikn.trim());
                        cursor.moveToFirst();
                        if (cursor != null && cursor.getCount() > 0) {
                            login_kimlikno = cursor.getString(cursor.getColumnIndexOrThrow("kimlikno"));
                            login_ad = cursor.getString(cursor.getColumnIndexOrThrow("ad"));
                            login_soyad = cursor.getString(cursor.getColumnIndexOrThrow("soyad"));
                            login_telefon = cursor.getString(cursor.getColumnIndexOrThrow("telefon"));
                            login_sifre = cursor.getString(cursor.getColumnIndexOrThrow("sifre"));
                            login_mail = cursor.getString(cursor.getColumnIndexOrThrow("mail"));
                            login_durum = cursor.getString(cursor.getColumnIndexOrThrow("durum"));
                            login_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));

                            //shared preferences ile giriş yapan kişinin kimlik numarasını ve oturumunun açık olup olmama durumunu
                            //daha sonra uygulama açıldığında bilgileri alabilmek için depoluyoruz
                            SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("id", login_durum);
                            editor.putString("tc", login_kimlikno);
                            /*editor.putString("Key", value); */
                            editor.commit();

                            //ana sayfayı açıyoruz
                            Intent anasayfa = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(anasayfa);
                            finish();
                        }
                        //bilgiler doğru değilse aşağıdaki mesajı gösteriyoruz
                    } else {
                        Toast.makeText(LoginActivity.this, "Geçersiz bilgiler", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //kayıt ol butonuna tıklanıldığında olacaklar;
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // kayıt olma ekranını açıyoruz
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    //Diğer ekrana geçmek için butona tıklanıldığında kayıt ekranını açıyoruz
    public void onLoginClick(View View) {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    //LocationChangeReceiver sınıfındaki anlık konum bilgisine göre konum bilgisi kapalıysa gösterilecek uyarı mesajının yapısı
    public static void alertDialog(Context context) {
        LocationChangeReceiver receiver;
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        receiver = new LocationChangeReceiver();
        context.registerReceiver(receiver, filter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konum Bilgisi Alınamadı");
        builder.setMessage("Lütfen konum ayarlarınızı kontrol edin.");
        builder.setCancelable(false);
        LoginActivity.dialog = builder.create();

        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);

            }
        });
    }

    //konum izni reddedildiğinde gösterilecek olan uyarı mesajının yapısı
    public static void permissionAlertDialog(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konum İzni Alınamadı");
        builder.setMessage("Uygulamayı kullanabilmek için konum izni vermeniz gerekiyor.");
        builder.setCancelable(false);
        LoginActivity.dialog = builder.create();

        //tamam butonuna tıklanırsa,
        builder.setNegativeButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //checkLocationPermission metodu ile tekrar konum izni istiyoruz
                checkLocationPermission(context);
            }
        }).show();
    }

    // konum izninin alınıp alınmadığını kontrol etme metodu (alınmadıysa tekrar izin ister)
    public static void checkLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            //izin alınamadıysa
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            //permissionAlertDialog metodu ile uyarı mesajı gösteriyoruz
            permissionAlertDialog(activity);
        }
    }

    // status bar rengini değiştirme metodu
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //rengi transparent olarak ayarlıyoruz (giriş ekranının ana rengi beyaz olduğu için)
            window.setStatusBarColor(getResources().getColor(R.color.Transparent));
        }
    }
}