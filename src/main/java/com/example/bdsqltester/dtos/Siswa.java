package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Siswa {
    public long id_siswa;
    public String nomor_induk;
    public String nama_siswa;
    public LocalDate tanggal_lahir;
    public String alamat_rumah;
    public long id_kelas; // Ini id kelas di database
    // Tambahan untuk menampilkan nama kelas siswa di TableView
    public String nama_kelas_terkini;
    public String tahun_ajaran_kelas_terkini;

    public Siswa(long id_siswa, String nomor_induk, String nama_siswa, LocalDate tanggal_lahir, String alamat_rumah, long id_kelas) {
        this.id_siswa = id_siswa;
        this.nomor_induk = nomor_induk;
        this.nama_siswa = nama_siswa;
        this.tanggal_lahir = tanggal_lahir;
        this.alamat_rumah = alamat_rumah;
        this.id_kelas = id_kelas;
        // Default
        this.nama_kelas_terkini = "Belum Ada Kelas";
        this.tahun_ajaran_kelas_terkini = "-";
    }
    // Konstruktor dengan semua field, termasuk nama_kelas_terkini (jika diperlukan untuk manual construction)
    public Siswa(long id_siswa, String nomor_induk, String nama_siswa, LocalDate tanggal_lahir, String alamat_rumah, long id_kelas, String nama_kelas_terkini, String tahun_ajaran_kelas_terkini) {
        this.id_siswa = id_siswa;
        this.nomor_induk = nomor_induk;
        this.nama_siswa = nama_siswa;
        this.tanggal_lahir = tanggal_lahir;
        this.alamat_rumah = alamat_rumah;
        this.id_kelas = id_kelas;
        this.nama_kelas_terkini = nama_kelas_terkini;
        this.tahun_ajaran_kelas_terkini = tahun_ajaran_kelas_terkini;
    }

    public Siswa(ResultSet rs) throws SQLException {
        this.id_siswa = rs.getLong("id_siswa");
        this.nomor_induk = rs.getString("nomor_induk");
        this.nama_siswa = rs.getString("nama_siswa");
        this.tanggal_lahir = rs.getDate("tanggal_lahir").toLocalDate();
        this.alamat_rumah = rs.getString("alamat_rumah");
        long kelasId = rs.getLong("id_kelas");
        this.id_kelas = rs.wasNull() ? 0 : kelasId; // Handle NULL id_kelas
        // Try to get nama_kelas from RS (if JOINed)
        try {
            this.nama_kelas_terkini = rs.getString("nama_kelas_terkini"); // Alias dari JOIN query
            if (rs.wasNull()) this.nama_kelas_terkini = "Belum Ada Kelas";
        } catch (SQLException e) {
            this.nama_kelas_terkini = "Belum Ada Kelas"; // Default jika tidak ada di ResultSet
        }
        try {
            this.tahun_ajaran_kelas_terkini = rs.getString("tahun_ajaran_kelas_terkini");
            if (rs.wasNull()) this.tahun_ajaran_kelas_terkini = "-";
        } catch (SQLException e) {
            this.tahun_ajaran_kelas_terkini = "-";
        }
    }

    // Getters
    public long getId_siswa() { return id_siswa; }
    public String getNomor_induk() { return nomor_induk; }
    public String getNama_siswa() { return nama_siswa; }
    public LocalDate getTanggal_lahir() { return tanggal_lahir; }
    public String getAlamat_rumah() { return alamat_rumah; }
    public long getId_kelas() { return id_kelas; } // Mengembalikan ID kelas di DB
    public String getNama_kelas_terkini() { return nama_kelas_terkini; } // Untuk tampilan
    public String getTahun_ajaran_kelas_terkini() { return tahun_ajaran_kelas_terkini; }


    @Override
    public String toString() {
        return nama_siswa + " (" + nomor_induk + ")";
    }
}