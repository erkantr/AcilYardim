package com.bysoftware.acilyardim;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//mail gönderme sınıfı
public class MailSender extends AsyncTask<Void, Void, String> {

    private Context context;
    private Session session;

    // Mesajın gideceği mail adresi, konu ve mesaj değişkenleri
    private String alici_mail_adresi;
    private String konu;
    private String mesaj;
    static String title;
    static String message;
    static String toast;

    private ProgressDialog progressDialog;

    //mail gönderme metodu
    public MailSender(Context context, String alici_mail_adresi, String konu, String mesaj) {
        this.context = context;
        this.alici_mail_adresi = alici_mail_adresi;
        this.konu = konu;
        this.mesaj = mesaj;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Mail giderken kullanıcıyı bekleten progress dialog
        progressDialog = ProgressDialog.show(context, title, message, false, false);
    }

    @Override
    protected String doInBackground(Void... voids) {

        Properties props = new Properties();

        //Mail konfigürasyon ayarları
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {

                    // Yapılandırma verilerini bu şekilde alıp mailimize giriş yapıyoruz
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MailData.MAIL, MailData.SIFRE);
                    }

                });

        try {

            MimeMessage mm = new MimeMessage(session);

            //Gönnderici adresi
            mm.setFrom(new InternetAddress(MailData.MAIL));

            //Alıcı adresi
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(alici_mail_adresi));

            //Konu
            mm.setSubject(konu);

            // Normal Mesaj
            //mm.setText(mesaj);

            // UTF-8 Destekli Mesaj
            mm.setContent(mesaj, "text/plain; charset=UTF-8");

            //Mail gönderme anı
            Transport.send(mm);

            return "basarili";

        } catch (MessagingException e) {

            Log.e("hata", "Hata : " + e.toString());
            e.printStackTrace();

            return null;

        }

    }

    @Override
    protected void onPostExecute(String sonuc) {

        progressDialog.dismiss();

        // Eğer dönen sonuç boş değilse mesajı gönderiyoruz
        if (sonuc != null) {

            Toast.makeText(context, toast, Toast.LENGTH_LONG).show();

        } else {

            Toast.makeText(context, "İşlem Başarısız, Daha Sonra Tekrar Deneyiniz!", Toast.LENGTH_LONG).show();

        }

    }

}
