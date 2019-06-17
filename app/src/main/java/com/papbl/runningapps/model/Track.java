package com.papbl.runningapps.model;

public class Track {
    private String id, jarak, tanggal, durasi, kalori;
    private double latFrom, lngFrom, latDest, lngDest;

    public Track(String id, String jarak, String tanggal, String durasi, String kalori, double latFrom, double lngFrom, double latDest, double lngDest) {
        this.id = id;
        this.jarak = jarak;
        this.tanggal = tanggal;
        this.durasi = durasi;
        this.kalori = kalori;
        this.latFrom = latFrom;
        this.lngFrom = lngFrom;
        this.latDest = latDest;
        this.lngDest = lngDest;
    }

    public double getLatFrom() {
        return latFrom;
    }

    public double getLngFrom() {
        return lngFrom;
    }

    public double getLatDest() {
        return latDest;
    }

    public double getLngDest() {
        return lngDest;
    }

    public String getId() {
        return id;
    }

    public String getJarak() {
        return jarak;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getDurasi() {
        return durasi;
    }

    public String getKalori() {
        return kalori;
    }


}
