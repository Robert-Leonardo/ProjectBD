package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Guru {
    private long id_guru;
    private String nama_guru;
    private String username_guru;
    private String password_guru; // Ini akan menyimpan hashed password

    // Konstruktor kosong
    public Guru() {
    }

    // Konstruktor dengan semua field
    public Guru(long id_guru, String nama_guru, String username_guru, String password_guru) {
        this.id_guru = id_guru;
        this.nama_guru = nama_guru;
        this.username_guru = username_guru;
        this.password_guru = password_guru;
    }

    // Konstruktor dari ResultSet
    public Guru(ResultSet rs) throws SQLException {
        this.id_guru = rs.getLong("id_guru");
        this.nama_guru = rs.getString("nama_guru");
        this.username_guru = rs.getString("username_guru");
        this.password_guru = rs.getString("password_guru");
    }

    // --- Getters ---
    public long getId_guru() { return id_guru; }
    public String getNama_guru() { return nama_guru; }
    public String getUsername_guru() { return username_guru; }
    public String getPassword_guru() { return password_guru; }

    // --- Setters (Jika diperlukan, untuk form binding atau modifikasi) ---
    public void setId_guru(long id_guru) { this.id_guru = id_guru; }
    public void setNama_guru(String nama_guru) { this.nama_guru = nama_guru; }
    public void setUsername_guru(String username_guru) { this.username_guru = username_guru; }
    public void setPassword_guru(String password_guru) { this.password_guru = password_guru; }

    @Override
    public String toString() {
        return nama_guru + " (" + username_guru + ")";
    }
}