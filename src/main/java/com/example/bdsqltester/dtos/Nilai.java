package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Nilai {
    private long id_nilai;
    private long id_siswa;
    private long id_pelajaran;
    private String jenis_ujian;
    private double nilai;
    private LocalDate tanggal_input;
    private int semester;
    private String tahun_ajaran;

    // Tambahan untuk nama siswa dan nama pelajaran (dari JOIN)
    private String nama_siswa;
    private String nama_pelajaran;

    public Nilai() {
    }

    public Nilai(long id_nilai, long id_siswa, long id_pelajaran, String jenis_ujian, double nilai, LocalDate tanggal_input, int semester, String tahun_ajaran) {
        this.id_nilai = id_nilai;
        this.id_siswa = id_siswa;
        this.id_pelajaran = id_pelajaran;
        this.jenis_ujian = jenis_ujian;
        this.nilai = nilai;
        this.tanggal_input = tanggal_input;
        this.semester = semester;
        this.tahun_ajaran = tahun_ajaran;
    }

    public Nilai(ResultSet rs) throws SQLException {
        this.id_nilai = rs.getLong("id_nilai");
        this.id_siswa = rs.getLong("id_siswa");
        this.id_pelajaran = rs.getLong("id_pelajaran");
        this.jenis_ujian = rs.getString("jenis_ujian");
        this.nilai = rs.getDouble("nilai");
        this.tanggal_input = rs.getDate("tanggal_input").toLocalDate();
        this.semester = rs.getInt("semester");
        this.tahun_ajaran = rs.getString("tahun_ajaran");
        // Ambil juga dari JOIN jika query mengambilnya
        try {
            this.nama_siswa = rs.getString("nama_siswa");
        } catch (SQLException e) { /* ignore if not present in RS */ }
        try {
            this.nama_pelajaran = rs.getString("nama_pelajaran");
        } catch (SQLException e) { /* ignore if not present in RS */ }
    }

    // --- Getters ---
    public long getId_nilai() { return id_nilai; }
    public long getId_siswa() { return id_siswa; }
    public long getId_pelajaran() { return id_pelajaran; }
    public String getJenis_ujian() { return jenis_ujian; }
    public double getNilai() { return nilai; }
    public LocalDate getTanggal_input() { return tanggal_input; }
    public int getSemester() { return semester; }
    public String getTahun_ajaran() { return tahun_ajaran; }
    public String getNama_siswa() { return nama_siswa; }
    public String getNama_pelajaran() { return nama_pelajaran; }

    // --- Setters (jika diperlukan, umumnya untuk DTO tidak semua perlu setter) ---
    public void setId_nilai(long id_nilai) { this.id_nilai = id_nilai; }
    public void setJenis_ujian(String jenis_ujian) { this.jenis_ujian = jenis_ujian; }
    public void setNilai(double nilai) { this.nilai = nilai; }
    public void setTanggal_input(LocalDate tanggal_input) { this.tanggal_input = tanggal_input; }
    public void setSemester(int semester) { this.semester = semester; }
    public void setTahun_ajaran(String tahun_ajaran) { this.tahun_ajaran = tahun_ajaran; }


    @Override
    public String toString() {
        return "Nilai: " + nama_siswa + " - " + nama_pelajaran + " (" + jenis_ujian + "): " + nilai;
    }
}