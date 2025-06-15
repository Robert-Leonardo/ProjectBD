package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MataPelajaran {
    private long id_pelajaran;
    private String nama_pelajaran;

    // Konstruktor kosong (opsional, tapi seringkali berguna untuk beberapa framework/lib)
    public MataPelajaran() {
    }

    // Konstruktor dengan semua field
    public MataPelajaran(long id_pelajaran, String nama_pelajaran) {
        this.id_pelajaran = id_pelajaran;
        this.nama_pelajaran = nama_pelajaran;
    }

    // Konstruktor yang mengambil data dari ResultSet (Mirip dengan Siswa DTO yang sudah Anda punya)
    public MataPelajaran(ResultSet rs) throws SQLException {
        this.id_pelajaran = rs.getLong("id_pelajaran");
        this.nama_pelajaran = rs.getString("nama_pelajaran");
    }

    // Getter untuk id_pelajaran
    public long getId_pelajaran() {
        return id_pelajaran;
    }

    // Setter untuk id_pelajaran
    public void setId_pelajaran(long id_pelajaran) {
        this.id_pelajaran = id_pelajaran;
    }

    // Getter untuk nama_pelajaran
    public String getNama_pelajaran() {
        return nama_pelajaran;
    }

    // Setter untuk nama_pelajaran
    public void setNama_pelajaran(String nama_pelajaran) {
        this.nama_pelajaran = nama_pelajaran;
    }

    @Override
    public String toString() {
        // Ini akan membantu menampilkan objek MataPelajaran di UI (misalnya di ChoiceBox atau ListView)
        return nama_pelajaran;
    }
}
