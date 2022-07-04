package com.bysoftware.acilyardim;

//veri tabanı model sınıfı
public class DBModel {

    private String kimlikno;
    private String ad;
    private String soyad;
    private String telefon;
    private String mail;
    private String sifre;
    private String durum;

    public DBModel(String kimlikno, String ad, String soyad, String telefon, String mail, String sifre, String durum) {
        this.kimlikno = kimlikno;
        this.ad = ad;
        this.soyad = soyad;
        this.telefon = telefon;
        this.mail = mail;
        this.sifre = sifre;
        this.durum = durum;
    }

    public String getKimlikno() {
        return kimlikno;
    }

    public void setKimlikno(String kimlikno) {
        this.kimlikno = kimlikno;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getSifre() {
        return sifre;
    }

    public void setSifre(String sifre) {
        this.sifre = sifre;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
