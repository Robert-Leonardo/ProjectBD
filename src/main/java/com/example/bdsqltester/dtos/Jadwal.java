package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class Jadwal {
    public long id_jadwal;
    public long id_kelas;
    public String nama_kelas; // Untuk tampilan di UI
    public long id_pelajaran;
    public String nama_pelajaran; // Untuk tampilan di UI
    public long id_guru;
    public String nama_guru; // Untuk tampilan di UI
    public String hari;
    public LocalTime jam_mulai;
    public LocalTime jam_selesai;

    // Konstruktor untuk mengambil dari ResultSet
    public Jadwal(ResultSet rs) throws SQLException {
        this.id_jadwal = rs.getLong("id_jadwal");
        this.id_kelas = rs.getLong("id_kelas");
        this.nama_kelas = rs.getString("nama_kelas"); // Asumsi join dengan tabel KELAS
        this.id_pelajaran = rs.getLong("id_pelajaran");
        this.nama_pelajaran = rs.getString("nama_pelajaran"); // Asumsi join dengan tabel MATA_PELAJARAN
        this.id_guru = rs.getLong("id_guru");
        this.nama_guru = rs.getString("nama_guru"); // Asumsi join dengan tabel GURU
        this.hari = rs.getString("hari");
        this.jam_mulai = rs.getTime("jam_mulai").toLocalTime();
        this.jam_selesai = rs.getTime("jam_selesai").toLocalTime();
    }

    // Konstruktor standar (opsional, jika membuat objek baru dari input)
    public Jadwal(long id_jadwal, long id_kelas, String nama_kelas, long id_pelajaran, String nama_pelajaran, long id_guru, String nama_guru, String hari, LocalTime jam_mulai, LocalTime jam_selesai) {
        this.id_jadwal = id_jadwal;
        this.id_kelas = id_kelas;
        this.nama_kelas = nama_kelas;
        this.id_pelajaran = id_pelajaran;
        this.nama_pelajaran = nama_pelajaran;
        this.id_guru = id_guru;
        this.nama_guru = nama_guru;
        this.hari = hari;
        this.jam_mulai = jam_mulai;
        this.jam_selesai = jam_selesai;
    }

    public LocalTime getJam_selesai() {
        return jam_selesai;
    }

    public LocalTime getJam_mulai() {
        return jam_mulai;
    }

    public String getHari() {
        return hari;
    }

    public String getNama_guru() {
        return nama_guru;
    }

    public String getNama_kelas() {
        return nama_kelas;
    }

    public String getNama_pelajaran() {
        return nama_pelajaran;
    }

    @Override
    public String toString() {
        return hari + ", " + nama_kelas + " (" + nama_pelajaran + " oleh " + nama_guru + ") " + jam_mulai + " - " + jam_selesai;
    }
}
