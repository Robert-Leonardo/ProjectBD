package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RaporEntry {
    private String namaMataPelajaran;
    private String jenisUjian;
    private double nilai;
    private int semester;
    private String tahunAjaran;

    public RaporEntry(String namaMataPelajaran, String jenisUjian, double nilai, int semester, String tahunAjaran) {
        this.namaMataPelajaran = namaMataPelajaran;
        this.jenisUjian = jenisUjian;
        this.nilai = nilai;
        this.semester = semester;
        this.tahunAjaran = tahunAjaran;
    }

    public RaporEntry(ResultSet rs) throws SQLException {
        this.namaMataPelajaran = rs.getString("nama_pelajaran");
        this.jenisUjian = rs.getString("jenis_ujian");
        this.nilai = rs.getDouble("nilai");
        this.semester = rs.getInt("semester");
        this.tahunAjaran = rs.getString("tahun_ajaran");
    }

    // Getters
    public String getNamaMataPelajaran() { return namaMataPelajaran; }
    public String getJenisUjian() { return jenisUjian; }
    public double getNilai() { return nilai; }
    public int getSemester() { return semester; }
    public String getTahunAjaran() { return tahunAjaran; }

    // Setters (opsional, jika perlu diubah setelah dibuat)
    public void setNamaMataPelajaran(String namaMataPelajaran) { this.namaMataPelajaran = namaMataPelajaran; }
    public void setJenisUjian(String jenisUjian) { this.jenisUjian = jenisUjian; }
    public void setNilai(double nilai) { this.nilai = nilai; }
    public void setSemester(int semester) { this.semester = semester; }
    public void setTahunAjaran(String tahunAjaran) { this.tahunAjaran = tahunAjaran; }

    @Override
    public String toString() {
        return namaMataPelajaran + " - " + jenisUjian + ": " + nilai + " (Semester " + semester + " " + tahunAjaran + ")";
    }
}