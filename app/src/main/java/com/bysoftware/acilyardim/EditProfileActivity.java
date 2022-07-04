package com.bysoftware.acilyardim;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

//profil düzenleme sınıfı
public class EditProfileActivity extends AppCompatActivity {

    EditText kimlikno, ad, soyad, telefon, sifre, email;
    Button btn_register;
    ImageView back;
    DBHandler dbHandler;
    String kimlik, isim, soyisim, tel, pass, mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //tasarım kısmındaki gerekli id leri tanımlıyoruz
        ad = findViewById(R.id.editTextName);
        soyad = findViewById(R.id.editTextSurname);
        telefon = findViewById(R.id.editTextNumber);
        kimlikno = findViewById(R.id.editTextKimlikNo);
        sifre = findViewById(R.id.editTextPassword);
        email = findViewById(R.id.editTextMail);
        btn_register = findViewById(R.id.registerButton);
        back = findViewById(R.id.back);
        //kimlik numarasını maksimum 11 haneli rakam olacak şekilde ayarlıyoruz
        kimlikno.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        //veri tabanımızı işlem yapabilmek için açıyoruz
        dbHandler = new DBHandler(this);
        dbHandler.open();
        LoginActivity.alertDialog(this);

        //edittextleri kullanıcı bilgilerine göre dolduruyoruz
        ad.setText(login_ad);
        soyad.setText(login_soyad);
        telefon.setText(login_telefon);
        kimlikno.setText(login_kimlikno);
        sifre.setText(login_sifre);
        email.setText(login_mail);

        //geri butonuna tıklanıldığında olacaklar
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ana sayfaya yönlendiriyoruz
                startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                finish();
            }
        });

        //kaydet butonuna tıklanıldığında olacaklar
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //edittext içerisindeki bilgileri alıyoruz
                kimlik = kimlikno.getText().toString().trim();
                isim = ad.getText().toString().trim();
                soyisim = soyad.getText().toString().trim();
                tel = telefon.getText().toString().trim();
                pass = sifre.getText().toString().trim();
                mail = email.getText().toString().trim();

                //herhangi bir alanın boş bırakıp bırakılmadığını kontrol ediyoruz
                if (kimlik.equals("") || isim.equals("") || soyisim.equals("") || tel.equals("") || pass.equals("") || mail.equals("")) {
                    Toast.makeText(EditProfileActivity.this, "Tüm alanlar zorunludur", Toast.LENGTH_SHORT).show();
                    //boş değilse;
                } else {
                    //mail adresi doğru girildiyse
                    if (mail.endsWith("@gmail.com")) {
                        //kimlik numarası 11 haneden küçükse uyarı mesajı veriyoruz
                        if (kimlik.length() < 11) {
                            Toast.makeText(EditProfileActivity.this, "Lütfen geçerli bir kimlik numarası girin", Toast.LENGTH_SHORT).show();
                            //değilse
                        } else {
                            //uyarı mesajı gösteriyoruz
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                            builder.setTitle("Bilgiler Güncelleniyor");
                            builder.setMessage("Bilgilerinizi güncellemek istediğinize emin misiniz?");
                            // evet seçeneğine tıklanılırsa
                            builder.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //bilgileri güncelleyip,
                                    Cursor cursor = dbHandler.GetUser(kimlik);
                                    LoginActivity.login_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                                    login_ad = isim;
                                    login_kimlikno = kimlik;
                                    login_soyad = soyisim;
                                    login_telefon = tel;
                                    login_sifre = pass;
                                    login_mail = mail;
                                    login_durum = "1";
                                    dbHandler.editUser(login_id, login_kimlikno, login_ad, login_soyad, login_telefon, login_sifre, login_mail, login_durum);
                                    //ana sayfaya yönlendiriyoruz
                                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            //hayır butonuna tıklanırsa uyarı mesajını kapatıyoruz
                            builder.setPositiveButton("Hayır", null);
                            builder.show();

                        }
                        //mail adresi yanlış girildiyse aşağıdaki mesajı veriyoruz
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Lütfen geçerli bir mail adresi girin", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}