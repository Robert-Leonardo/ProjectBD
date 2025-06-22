package com.example.bdsqltester.scenes.waliKelas;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Guru;
import com.example.bdsqltester.dtos.Kelas;
import com.example.bdsqltester.dtos.Siswa; // Menggunakan DTO Siswa
import com.example.bdsqltester.dtos.RaporEntry;
import com.example.bdsqltester.dtos.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty; // Untuk TableView CellValueFactory

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WaliKelasController {

    @FXML private Label welcomeLabel;
    @FXML private Label kelasDiampuLabel;

    @FXML private TableView<Siswa> siswaTableView; // Mengubah dari ChoiceBox ke TableView
    @FXML private TableColumn<Siswa, String> siswaIdColumn;
    @FXML private TableColumn<Siswa, String> siswaNomorIndukColumn;
    @FXML private TableColumn<Siswa, String> siswaNamaColumn;

    @FXML private Label namaSiswaRaporLabel;
    @FXML private Label nomorIndukRaporLabel;
    @FXML private Label kelasRaporLabel;

    @FXML private TableView<RaporEntry> raporTableView;
    @FXML private TableColumn<RaporEntry, String> mapelColumn;
    @FXML private TableColumn<RaporEntry, String> jenisUjianColumn;
    @FXML private TableColumn<RaporEntry, String> nilaiColumn;
    @FXML private TableColumn<RaporEntry, String> semesterColumn;
    @FXML private TableColumn<RaporEntry, String> tahunAjaranColumn;

    private User currentUser;
    private Guru waliKelasInfo;
    private Kelas kelasDiampu;
    private Siswa selectedSiswaForRapor;

    private final ObservableList<Siswa> siswaListDiKelas = FXCollections.observableArrayList();
    private final ObservableList<RaporEntry> raporEntries = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.currentUser = user;
        loadWaliKelasData();
    }

    @FXML
    void initialize() {
        // Inisialisasi kolom TableView Siswa
        siswaIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_siswa())));
        siswaNomorIndukColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomor_induk()));
        siswaNamaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_siswa()));
        siswaTableView.setItems(siswaListDiKelas);

        // Listener untuk pemilihan siswa di TableView
        siswaTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
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
        if (currentUser == null || !currentUser.getRole().equals("Wali kelas")) {
            welcomeLabel.setText("Selamat datang!");
            kelasDiampuLabel.setText("Akses tidak diizinkan untuk peran ini.");
            siswaTableView.setDisable(true); // Disable table jika bukan wali kelas
            return;
        }

        long guruId = currentUser.getId();

        try (Connection c = MainDataSource.getConnection()) {
            String guruQuery = "SELECT id_guru, nama_guru FROM GURU WHERE id_guru = ?";
            PreparedStatement stmtGuru = c.prepareStatement(guruQuery);
            stmtGuru.setLong(1, guruId);
            ResultSet rsGuru = stmtGuru.executeQuery();

            if (rsGuru.next()) {
                waliKelasInfo = new Guru(rsGuru.getLong("id_guru"), rsGuru.getString("nama_guru"), null, null);
                welcomeLabel.setText("Selamat Datang, Wali Kelas " + waliKelasInfo.getNama_guru() + "!");

                String kelasQuery = "SELECT id_kelas, nama_kelas, tahun_ajaran FROM KELAS WHERE id_wali_kelas = ?";
                PreparedStatement stmtKelas = c.prepareStatement(kelasQuery);
                stmtKelas.setLong(1, guruId);
                ResultSet rsKelas = stmtKelas.executeQuery();

                if (rsKelas.next()) {
                    kelasDiampu = new Kelas(rsKelas.getLong("id_kelas"), rsKelas.getString("nama_kelas"), rsKelas.getString("tahun_ajaran"), guruId);
                    kelasDiampuLabel.setText("Mengampu Kelas: " + kelasDiampu.getNama_kelas() + " (" + kelasDiampu.getTahun_ajaran() + ")");
                    loadSiswaDiKelas(kelasDiampu.getId_kelas());
                    siswaTableView.setDisable(false); // Enable table jika ada kelas
                } else {
                    kelasDiampuLabel.setText("Anda belum mengampu kelas apapun sebagai Wali Kelas.");
                    siswaTableView.setDisable(true);
                }
            } else {
                welcomeLabel.setText("Data Guru Tidak Ditemukan.");
                kelasDiampuLabel.setText("");
                siswaTableView.setDisable(true);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data wali kelas atau kelas diampu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSiswaDiKelas(long idKelas) {
        siswaListDiKelas.clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT s.id_siswa, s.nomor_induk, s.nama_siswa, s.tanggal_lahir, s.alamat_rumah, s.id_kelas, " +
                    "k.nama_kelas AS nama_kelas_terkini, k.tahun_ajaran AS tahun_ajaran_kelas_terkini " +
                    "FROM SISWA s JOIN KELAS k ON s.id_kelas = k.id_kelas " +
                    "WHERE s.id_kelas = ? ORDER BY nama_siswa";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, idKelas);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                siswaListDiKelas.add(new Siswa(rs));
            }
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
        showAlert(Alert.AlertType.INFORMATION, "Fitur Cetak Rapor", "Rapor dicetak !");
    }

    @FXML
    void onLogoutClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Parent root = loader.load();
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Logout", "Terjadi kesalahan saat mencoba logout.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}