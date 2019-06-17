package com.papbl.runningapps.model;

import java.io.Serializable;

public class User implements Serializable {
    private String id, nama, email;

    public User(String id, String nama, String email) {
        this.id = id;
        this.nama = nama;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }
}
