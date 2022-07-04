package com.bysoftware.acilyardim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

//veri tabanını oluşturduğumuz ve yapısını düzenlediğimiz metodları içeren sınıf
public class DBHandler extends SQLiteOpenHelper {

    Context context;
    private DBHandler mDbHelper;
    private SQLiteDatabase mDb;
    private static final String TAG = "DBHandler";
    private static String DATABASE_NAME = "Database.db";
    private static int DATABASE_VERSION = 3;
    //veri tabanımızın yapısı
    private static String createTable = "create table kullanicilar (" +
            "_id integer PRIMARY KEY" + "," +
            "kimlikno VARCHAR" + "," +
            "ad VARCHAR" + "," +
            "soyad VARCHAR" + "," +
            "telefon VARCHAR" + "," +
            "mail VARCHAR" + "," +
            "sifre VARCHAR" + "," +
            "durum VARCHAR)";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {

            db.execSQL(createTable);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading application's database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data!");
        db.execSQL("DROP TABLE IF EXISTS " + "kullanicilar");
        onCreate(db);
    }

    //veri tabanını diğer sınıflarda çağıracağımız metod
    public DBHandler open() throws SQLException {
        mDbHelper = new DBHandler(context);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    //kayıt olma kısmındaki hesap bilgilerini kayıt etme metodu
    public void storeUser(DBModel dbModel) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String kimlikno = dbModel.getKimlikno();
            String ad = dbModel.getAd();
            String soyad = dbModel.getSoyad();
            String sifre = dbModel.getSifre();
            String telefon = dbModel.getTelefon();
            String mail = dbModel.getMail();
            String durum = dbModel.getDurum();
            ContentValues contentValues = new ContentValues();

            contentValues.put("kimlikno", kimlikno);
            contentValues.put("ad", ad);
            contentValues.put("soyad", soyad);
            contentValues.put("telefon", telefon);
            contentValues.put("mail", mail);
            contentValues.put("sifre", sifre);
            contentValues.put("durum", durum);

            sqLiteDatabase.insert("kullanicilar", null, contentValues);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    //kimlik bilgilerinden kullanıcı bilgilerini alma metodu
    public Cursor GetUser(String giris) {
        Cursor cursor = mDb.query(true, "kullanicilar", new String[]{"_id", "kimlikno", "ad", "soyad", "telefon", "mail",
                        "sifre", "durum"},
                "kimlikno" + " like '%" + giris + "%'", null,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    //bilgileri güncelleme metodu
    public void editUser(int _id, String kimlikno, String ad, String soyad, String telefon, String mail, String sifre, String durum) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("kimlikno", kimlikno);
        contentValues.put("ad", ad);
        contentValues.put("soyad", soyad);
        contentValues.put("telefon", telefon);
        contentValues.put("mail", mail);
        contentValues.put("sifre", sifre);
        contentValues.put("durum", durum);

        mDb.update("kullanicilar", contentValues, "_id" + " = ?",
                new String[]{String.valueOf(_id)});
    }

    //kimlik numarası ile şifrenin doğru olup olmadığını kontrol etme metodu
    public Boolean checkTCandPassword(String kimlikno, String sifre) {
        Cursor cursor = mDb.rawQuery("Select * from kullanicilar where kimlikno = ? and sifre = ? ", new String[]{kimlikno, sifre});
        if (cursor.getCount() > 0) {

            return true;
        } else {
            return false;
        }

    }

    //kimlik numarasının daha önce kayıt edilip edilmediğini kontrol etme metodu
    public Boolean checkTC(String kimlikno) {
        Cursor cursor;
        cursor = mDb.rawQuery("Select * from kullanicilar where kimlikno = ?", new String[]{kimlikno});
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }


}
