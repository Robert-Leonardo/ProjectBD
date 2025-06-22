package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class PrestasiSiswa {
    private long id_prestasi;
    private long id_siswa;
    private String namaSiswa; // Akan di-join dari tabel SISWA
    private String namaPrestasi;
    private String tingkat;
    private String jenisLomba;
    private String deskripsi;
    private LocalDate tanggalPrestasi;

    public PrestasiSiswa() {
    }

    public PrestasiSiswa(long id_prestasi, long id_siswa, String namaPrestasi, String tingkat, String jenisLomba, String deskripsi, LocalDate tanggalPrestasi) {
        this.id_prestasi = id_prestasi;
        this.id_siswa = id_siswa;
        this.namaPrestasi = namaPrestasi;
        this.tingkat = tingkat;
        this.jenisLomba = jenisLomba;
        this.deskripsi = deskripsi;
        this.tanggalPrestasi = tanggalPrestasi;
    }

    public PrestasiSiswa(ResultSet rs) throws SQLException {
        this.id_prestasi = rs.getLong("id_prestasi");
        this.id_siswa = rs.getLong("id_siswa");
        this.namaPrestasi = rs.getString("nama_prestasi");
        this.tingkat = rs.getString("tingkat");
        this.jenisLomba = rs.getString("jenis_lomba");
        this.deskripsi = rs.getString("deskripsi");
        this.tanggalPrestasi = rs.getDate("tanggal_prestasi").toLocalDate();
        // Coba ambil nama siswa jika di-join di query
        try {
            this.namaSiswa = rs.getString("nama_siswa");
        } catch (SQLException e) {
            this.namaSiswa = null; // Default jika tidak ada di ResultSet
        }
    }

    // Getters
    public long getId_prestasi() { return id_prestasi; }
    public long getId_siswa() { return id_siswa; }
    public String getNamaSiswa() { return namaSiswa; }
    public String getNamaPrestasi() { return namaPrestasi; }
    public String getTingkat() { return tingkat; }
    public String getJenisLomba() { return jenisLomba; }
    public String getDeskripsi() { return deskripsi; }
    public LocalDate getTanggalPrestasi() { return tanggalPrestasi; }

    // Setters (jika diperlukan untuk pengeditan dari UI)
    public void setId_prestasi(long id_prestasi) { this.id_prestasi = id_prestasi; }
    public void setId_siswa(long id_siswa) { this.id_siswa = id_siswa; }
    public void setNamaSiswa(String namaSiswa) { this.namaSiswa = namaSiswa; }
    public void setNamaPrestasi(String namaPrestasi) { this.namaPrestasi = namaPrestasi; }
    public void setTingkat(String tingkat) { this.tingkat = tingkat; }
    public void setJenisLomba(String jenisLomba) { this.jenisLomba = jenisLomba; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setTanggalPrestasi(LocalDate tanggalPrestasi) { this.tanggalPrestasi = tanggalPrestasi; }

    @Override
    public String toString() {
        return namaPrestasi + " (" + tingkat + ")";
    }
}