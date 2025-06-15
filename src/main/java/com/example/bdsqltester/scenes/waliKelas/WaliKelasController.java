package com.example.bdsqltester.scenes.waliKelas; // Buat package walikelas

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Guru;
import com.example.bdsqltester.dtos.Kelas;
import com.example.bdsqltester.dtos.Siswa;
import com.example.bdsqltester.dtos.RaporEntry; // DTO RaporEntry
import com.example.bdsqltester.dtos.User; // DTO User

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class WaliKelasController {

    @FXML private Label welcomeLabel; // Label selamat datang wali kelas
    @FXML private Label kelasDiampuLabel; // Menampilkan kelas yang diampu
    @FXML private ChoiceBox<Siswa> siswaChoiceBox; // Pilih siswa di kelas yang diampu

    @FXML private Label namaSiswaRaporLabel; // Nama siswa di bagian rapor
    @FXML private Label nomorIndukRaporLabel; // Nomor induk siswa di bagian rapor
    @FXML private Label kelasRaporLabel; // Kelas siswa di bagian rapor

    @FXML private TableView<RaporEntry> raporTableView; // Tabel untuk menampilkan nilai rapor
    @FXML private TableColumn<RaporEntry, String> mapelColumn;
    @FXML private TableColumn<RaporEntry, String> jenisUjianColumn;
    @FXML private TableColumn<RaporEntry, String> nilaiColumn;
    @FXML private TableColumn<RaporEntry, String> semesterColumn;
    @FXML private TableColumn<RaporEntry, String> tahunAjaranColumn;

    private User currentUser;
    private Guru waliKelasInfo; // Menyimpan informasi guru wali kelas
    private Kelas kelasDiampu; // Menyimpan informasi kelas yang diampu
    private Siswa selectedSiswaForRapor; // Siswa yang dipilih untuk rapor

    private final ObservableList<Siswa> siswaListDiKelas = FXCollections.observableArrayList();
    private final ObservableList<RaporEntry> raporEntries = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.currentUser = user;
        loadWaliKelasData(); // Memuat data wali kelas dan kelasnya
    }

    @FXML
    void initialize() {
        // Inisialisasi ChoiceBox siswa
        siswaChoiceBox.setConverter(new StringConverter<Siswa>() {
            @Override
            public String toString(Siswa siswa) {
                return siswa != null ? siswa.getNama_siswa() + " (" + siswa.getNomor_induk() + ")" : "";
            }
            @Override
            public Siswa fromString(String s) {
                return null;
            }
        });

        // Listener untuk pemilihan siswa
        siswaChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSiswaForRapor = newVal;
            if (newVal != null) {
                loadRaporSiswa(newVal.getId_siswa());
                displaySiswaRaporInfo(newVal);
            } else {
                clearRaporInfo();
            }
        });

        // Inisialisasi kolom TableView Rapor
        mapelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaMataPelajaran()));
        jenisUjianColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenisUjian()));
        nilaiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNilai())));
        semesterColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSemester())));
        tahunAjaranColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTahunAjaran()));

        raporTableView.setItems(raporEntries);
    }

    private void loadWaliKelasData() {
        if (currentUser == null || (!currentUser.getRole().equals("Wali kelas") && !currentUser.getRole().equals("Guru"))) {
            welcomeLabel.setText("Selamat datang!");
            kelasDiampuLabel.setText("Peran tidak valid.");
            return;
        }

        long guruId = currentUser.getId();

        try (Connection c = MainDataSource.getConnection()) {
            // Ambil info guru
            String guruQuery = "SELECT id_guru, nama_guru FROM GURU WHERE id_guru = ?";
            PreparedStatement stmtGuru = c.prepareStatement(guruQuery);
            stmtGuru.setLong(1, guruId);
            ResultSet rsGuru = stmtGuru.executeQuery();

            if (rsGuru.next()) {
                waliKelasInfo = new Guru(rsGuru.getLong("id_guru"), rsGuru.getString("nama_guru"), null, null);
                welcomeLabel.setText("Selamat Datang, Wali Kelas " + waliKelasInfo.getNama_guru() + "!");

                // Ambil info kelas yang diampu guru ini sebagai wali kelas
                String kelasQuery = "SELECT id_kelas, nama_kelas, tahun_ajaran FROM KELAS WHERE id_wali_kelas = ?";
                PreparedStatement stmtKelas = c.prepareStatement(kelasQuery);
                stmtKelas.setLong(1, guruId);
                ResultSet rsKelas = stmtKelas.executeQuery();

                if (rsKelas.next()) {
                    kelasDiampu = new Kelas(rsKelas.getLong("id_kelas"), rsKelas.getString("nama_kelas"), rsKelas.getString("tahun_ajaran"), guruId);
                    kelasDiampuLabel.setText("Mengampu Kelas: " + kelasDiampu.getNama_kelas() + " (" + kelasDiampu.getTahun_ajaran() + ")");
                    loadSiswaDiKelas(kelasDiampu.getId_kelas()); // Muat siswa di kelas ini
                } else {
                    kelasDiampuLabel.setText("Anda belum mengampu kelas apapun sebagai Wali Kelas.");
                    siswaChoiceBox.setDisable(true);
                }
            } else {
                welcomeLabel.setText("Data Guru Tidak Ditemukan.");
                kelasDiampuLabel.setText("");
                siswaChoiceBox.setDisable(true);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data wali kelas atau kelas diampu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSiswaDiKelas(long idKelas) {
        siswaListDiKelas.clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT id_siswa, nomor_induk, nama_siswa, tanggal_lahir, alamat_rumah, id_kelas, " +
                    "k.nama_kelas AS nama_kelas_terkini, k.tahun_ajaran AS tahun_ajaran_kelas_terkini " +
                    "FROM SISWA s JOIN KELAS k ON s.id_kelas = k.id_kelas " +
                    "WHERE s.id_kelas = ? ORDER BY nama_siswa";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, idKelas);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                siswaListDiKelas.add(new Siswa(rs));
            }
            siswaChoiceBox.setItems(siswaListDiKelas);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa di kelas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRaporSiswa(long idSiswa) {
        raporEntries.clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT n.jenis_ujian, n.nilai, n.semester, n.tahun_ajaran, mp.nama_pelajaran " +
                    "FROM NILAI n JOIN MATA_PELAJARAN mp ON n.id_pelajaran = mp.id_pelajaran " +
                    "WHERE n.id_siswa = ? " +
                    "ORDER BY n.tahun_ajaran DESC, n.semester DESC, mp.nama_pelajaran ASC, n.jenis_ujian ASC";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, idSiswa);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                raporEntries.add(new RaporEntry(rs));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat rapor siswa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displaySiswaRaporInfo(Siswa siswa) {
        if (siswa != null) {
            namaSiswaRaporLabel.setText("Nama: " + siswa.getNama_siswa());
            nomorIndukRaporLabel.setText("No. Induk: " + siswa.getNomor_induk());
            kelasRaporLabel.setText("Kelas: " + siswa.getNama_kelas_terkini() + " (" + siswa.getTahun_ajaran_kelas_terkini() + ")");
        } else {
            clearRaporInfo();
        }
    }

    private void clearRaporInfo() {
        namaSiswaRaporLabel.setText("Nama:");
        nomorIndukRaporLabel.setText("No. Induk:");
        kelasRaporLabel.setText("Kelas:");
        raporEntries.clear();
    }

    @FXML
    void onCetakRaporClick(ActionEvent event) {
        if (selectedSiswaForRapor == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Siswa", "Pilih siswa terlebih dahulu untuk mencetak rapor.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Fitur Cetak Rapor", "Fitur cetak rapor siswa " + selectedSiswaForRapor.getNama_siswa() + " akan segera diimplementasikan!");
        // TODO: Implementasi logika cetak rapor (misalnya ke PDF)
        // Ini akan melibatkan library seperti Apache PDFBox atau JasperReports
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}