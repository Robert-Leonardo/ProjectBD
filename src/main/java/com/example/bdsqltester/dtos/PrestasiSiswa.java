package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PrestasiSiswa {
    private long id_prestasi;
    private long id_siswa;
    private String namaSiswa; // Akan di-join dari tabel SISWA
    private String namaPrestasi;
    private String tingkat;
    private String jenisLomba;
    private String deskripsi;

    public PrestasiSiswa() {
    }

    public PrestasiSiswa(long id_prestasi, long id_siswa, String namaPrestasi, String tingkat, String jenisLomba, String deskripsi) {
        this.id_prestasi = id_prestasi;
        this.id_siswa = id_siswa;
        this.namaPrestasi = namaPrestasi;
        this.tingkat = tingkat;
        this.jenisLomba = jenisLomba;
        this.deskripsi = deskripsi;
    }

    public PrestasiSiswa(ResultSet rs) throws SQLException {
        this.id_prestasi = rs.getLong("id_prestasi");
        this.id_siswa = rs.getLong("id_siswa");
        this.namaPrestasi = rs.getString("nama_prestasi");
        this.tingkat = rs.getString("tingkat");
        this.jenisLomba = rs.getString("jenis_lomba");
        this.deskripsi = rs.getString("deskripsi");
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

    // Setters (jika diperlukan untuk pengeditan dari UI)
    public void setId_prestasi(long id_prestasi) { this.id_prestasi = id_prestasi; }
    public void setId_siswa(long id_siswa) { this.id_siswa = id_siswa; }
    public void setNamaSiswa(String namaSiswa) { this.namaSiswa = namaSiswa; }
    public void setNamaPrestasi(String namaPrestasi) { this.namaPrestasi = namaPrestasi; }
    public void setTingkat(String tingkat) { this.tingkat = tingkat; }
    public void setJenisLomba(String jenisLomba) { this.jenisLomba = jenisLomba; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() {
        return namaPrestasi + " (" + tingkat + ")";
    }
}