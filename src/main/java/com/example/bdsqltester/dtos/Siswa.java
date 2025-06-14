package com.example.bdsqltester.dtos;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Siswa {
    public long id_siswa;
    public String nomor_induk;
    public String nama_siswa;
    public LocalDate tanggal_lahir; // Menggunakan LocalDate untuk DatePicker
    public String alamat_rumah;
    public long id_kelas; // ID kelas tempat siswa berada

    // Konstruktor untuk mengambil dari ResultSet
    public Siswa(ResultSet rs) throws SQLException {
        this.id_siswa = rs.getLong("id_siswa");
        this.nomor_induk = rs.getString("nomor_induk");
        this.nama_siswa = rs.getString("nama_siswa");
        this.tanggal_lahir = rs.getDate("tanggal_lahir").toLocalDate(); // Konversi ke LocalDate
        this.alamat_rumah = rs.getString("alamat_rumah");
        this.id_kelas = rs.getLong("id_kelas");
    }

    // Konstruktor standar (jika diperlukan untuk membuat objek baru)
    public Siswa(long id_siswa, String nomor_induk, String nama_siswa, LocalDate tanggal_lahir, String alamat_rumah, long id_kelas) {
        this.id_siswa = id_siswa;
        this.nomor_induk = nomor_induk;
        this.nama_siswa = nama_siswa;
        this.tanggal_lahir = tanggal_lahir;
        this.alamat_rumah = alamat_rumah;
        this.id_kelas = id_kelas;
    }

    @Override
    public String toString() {
        return nama_siswa + " (" + nomor_induk + ")";
    }
}
