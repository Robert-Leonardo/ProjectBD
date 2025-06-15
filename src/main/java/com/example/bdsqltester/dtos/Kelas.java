package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Kelas {
    private long id_kelas;
    private String nama_kelas;
    private String tahun_ajaran;
    private Long id_wali_kelas;
    private String nama_wali_kelas;

    public Kelas() {}

    public Kelas(long id_kelas, String nama_kelas, String tahun_ajaran, Long id_wali_kelas) {
        this.id_kelas = id_kelas;
        this.nama_kelas = nama_kelas;
        this.tahun_ajaran = tahun_ajaran;
        this.id_wali_kelas = id_wali_kelas;
    }

    public Kelas(ResultSet rs) throws SQLException {
        this.id_kelas = rs.getLong("id_kelas");
        this.nama_kelas = rs.getString("nama_kelas");
        this.tahun_ajaran = rs.getString("tahun_ajaran");

        long waliKelasId = rs.getLong("id_wali_kelas");
        this.id_wali_kelas = rs.wasNull() ? null : waliKelasId;

        try {
            this.nama_wali_kelas = rs.getString("nama_wali_kelas");
            if (rs.wasNull()) {
                this.nama_wali_kelas = null;
            }
        } catch (SQLException e) {
            this.nama_wali_kelas = null;
        }
    }

    public long getId_kelas() { return id_kelas; }
    public String getNama_kelas() { return nama_kelas; }
    public String getTahun_ajaran() { return tahun_ajaran; }
    public Long getId_wali_kelas() { return id_wali_kelas; }
    public String getNama_wali_kelas() { return nama_wali_kelas; }

    @Override
    public String toString() {
        // Ini akan digunakan di ChoiceBox atau untuk representasi string dasar
        return nama_kelas + " (" + tahun_ajaran + ")";
    }
}