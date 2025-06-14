package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Kelas {
    public long id_kelas;
    public String nama_kelas;
    public String tahun_ajaran;
    public long id_wali_kelas; // ID guru yang menjadi wali kelas

    // Konstruktor untuk membuat objek Kelas dari ResultSet database
    public Kelas(ResultSet rs) throws SQLException {
        this.id_kelas = rs.getLong("id_kelas");
        this.nama_kelas = rs.getString("nama_kelas");
        this.tahun_ajaran = rs.getString("tahun_ajaran");
        this.id_wali_kelas = rs.getLong("id_wali_kelas");
        // Perhatikan: Jika id_wali_kelas bisa NULL di DB, Anda perlu menangani rs.wasNull()
        // atau gunakan wrapper class Long di sini.
    }

    // Konstruktor standar (jika Anda ingin membuat objek Kelas secara manual dari kode)
    public Kelas(long id_kelas, String nama_kelas, String tahun_ajaran, long id_wali_kelas) {
        this.id_kelas = id_kelas;
        this.nama_kelas = nama_kelas;
        this.tahun_ajaran = tahun_ajaran;
        this.id_wali_kelas = id_wali_kelas;
    }

    // Metode toString() untuk representasi yang mudah dibaca di UI (misalnya di ChoiceBox atau ListView)
    @Override
    public String toString() {
        return nama_kelas + " (" + tahun_ajaran + ")";
    }

    // --- Getter methods (Optional, but good practice for encapsulation) ---
    public long getId_kelas() {
        return id_kelas;
    }

    public String getNama_kelas() {
        return nama_kelas;
    }

    public String getTahun_ajaran() {
        return tahun_ajaran;
    }

    public long getId_wali_kelas() {
        return id_wali_kelas;
    }

    // --- Setter methods (Optional, if you need to modify object state) ---
    public void setId_kelas(long id_kelas) {
        this.id_kelas = id_kelas;
    }

    public void setNama_kelas(String nama_kelas) {
        this.nama_kelas = nama_kelas;
    }

    public void setTahun_ajaran(String tahun_ajaran) {
        this.tahun_ajaran = tahun_ajaran;
    }

    public void setId_wali_kelas(long id_wali_kelas) {
        this.id_wali_kelas = id_wali_kelas;
    }
}