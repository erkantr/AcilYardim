package com.bysoftware.acilyardim;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static com.bysoftware.acilyardim.LoginActivity.login_ad;
import static com.bysoftware.acilyardim.LoginActivity.login_durum;
import static com.bysoftware.acilyardim.LoginActivity.login_id;
import static com.bysoftware.acilyardim.LoginActivity.login_kimlikno;
import static com.bysoftware.acilyardim.LoginActivity.login_mail;
import static com.bysoftware.acilyardim.LoginActivity.login_sifre;
import static com.bysoftware.acilyardim.LoginActivity.login_soyad;
import static com.bysoftware.acilyardim.LoginActivity.login_telefon;
import static com.bysoftware.acilyardim.MainActivity.MyPREFERENCES;

//kayıt olma ekranı
public class RegisterActivity extends AppCompatActivity {

    EditText kimlikno, ad, soyad, telefon, sifre, email;
    Button btn_register;
    DBHandler dbHandler;
    String kimlik, isim, soyisim, tel, pass, mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //id leri tanımlıyoruz
        ad = findViewById(R.id.editTextName);
        soyad = findViewById(R.id.editTextSurname);
        telefon = findViewById(R.id.editTextNumber);
        kimlikno = findViewById(R.id.editTextKimlikNo);
        sifre = findViewById(R.id.editTextPassword);
        email = findViewById(R.id.editTextMail);
        btn_register = findViewById(R.id.registerButton);
        //kimlik numarasını maksimum 11 haneli rakam olacak şekilde ayarlıyoruz
        kimlikno.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        //veri tabanımızı çağırıyoruz
        dbHandler = new DBHandler(this);
        dbHandler.open();
        LoginActivity.alertDialog(this);

        //kayıt ol butonuna tıklanıldığında olacaklar
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //edittext den bilgileri alıyoruz
                kimlik = kimlikno.getText().toString().trim();
                isim = ad.getText().toString().trim();
                soyisim = soyad.getText().toString().trim();
                tel = telefon.getText().toString().trim();
                pass = sifre.getText().toString().trim();
                mail = email.getText().toString().trim();

                //bilgiler boşsa aşağıdaki mesajı gösteriyoruz
                if (kimlik.equals("") || isim.equals("") || soyisim.equals("") || tel.equals("") || pass.equals("") || mail.equals("")) {
                    Toast.makeText(RegisterActivity.this, "Tüm alanlar zorunludur", Toast.LENGTH_SHORT).show();
                } else {
                    //kimlik numarası daha önce kayıt edilmediyse
                    if (!dbHandler.checkTC(kimlik)) {
                        //mail adresi doğru yazıldıysa
                        if (mail.endsWith("@gmail.com")) {
                            //kimlik numarası 11 haneden küçükse uyarı mesajı veriyoruz
                            if (kimlik.length() < 11) {
                                Toast.makeText(RegisterActivity.this, "Lütfen geçerli bir kimlik numarası girin", Toast.LENGTH_SHORT).show();
                                //değilse
                            } else {
                                //storeUser metodu ile bilgileri kaydediyoruz
                                dbHandler.storeUser(new DBModel(kimlik, isim, soyisim, tel, mail, pass, "1"));
                                Cursor cursor = dbHandler.GetUser(kimlik);
                                login_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                                login_ad = isim;
                                login_kimlikno = kimlik;
                                login_soyad = soyisim;
                                login_telefon = tel;
                                login_sifre = pass;
                                login_mail = mail;
                                login_durum = "1";

                                //shared preferences ile kimlik numarasını ve oturum durumunu kaydediyoruz
                                SharedPreferences sharedpreferences = getSharedPreferences( MyPREFERENCES, Context.MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("id", login_durum);
                                editor.putString("tc", login_kimlikno);
                                editor.commit();
                                //ana ekranı açıyoruz
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            //mail adresi yanlış yazıldıysa aşağıdaki mesajı gösterioruz
                        } else {
                            Toast.makeText(RegisterActivity.this, "Lütfen geçerli bir mail adresi girin", Toast.LENGTH_SHORT).show();
                        }
                        //kimlik numarası ile daha önce kayıt olunduysa aşağıdaki mesajı veriyoruz
                    } else {
                        Toast.makeText(RegisterActivity.this, "Zaten bir hesabınız bulunuyor", Toast.LENGTH_SHORT).show();

                    }

                }
            }
        });
    }

    //Diğer ekrana geçmek için butona tıklanıldığında giriş ekranını açıyoruz
    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}