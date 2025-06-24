package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AbsensiSiswa {
    private long id_absensi;
    private long id_siswa;
    private String namaSiswa;
    private LocalDate tanggal;
    private String status;
    private String keterangan;

    public AbsensiSiswa() {
    }

    public AbsensiSiswa(long id_absensi, long id_siswa, LocalDate tanggal, String status, String keterangan) {
        this.id_absensi = id_absensi;
        this.id_siswa = id_siswa;
        this.tanggal = tanggal;
        this.status = status;
        this.keterangan = keterangan;
    }

    public AbsensiSiswa(ResultSet rs) throws SQLException {
        this.id_absensi = rs.getLong("id_absensi");
        this.id_siswa = rs.getLong("id_siswa");
        this.tanggal = rs.getDate("tanggal").toLocalDate();
        this.status = rs.getString("status");
        this.keterangan = rs.getString("keterangan");
        // Coba ambil nama siswa jika di-join di query
        try {
            this.namaSiswa = rs.getString("nama_siswa");
        } catch (SQLException e) {
            this.namaSiswa = null;
        }
    }

    // Getters
    public long getId_absensi() { return id_absensi; }
    public long getId_siswa() { return id_siswa; }
    public String getNamaSiswa() { return namaSiswa; }
    public LocalDate getTanggal() { return tanggal; }
    public String getStatus() { return status; }
    public String getKeterangan() { return keterangan; }

    // Setters (penting untuk TableView editing)
    public void setId_absensi(long id_absensi) { this.id_absensi = id_absensi; }
    public void setId_siswa(long id_siswa) { this.id_siswa = id_siswa; }
    public void setNamaSiswa(String namaSiswa) { this.namaSiswa = namaSiswa; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public void setStatus(String status) { this.status = status; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    @Override
    public String toString() {
        return namaSiswa + " - " + status + " (" + tanggal + ")";
    }
}