package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Jadwal;
import com.example.bdsqltester.dtos.User; // Pastikan User DTO diimport

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty; // Untuk TableView CellValueFactory

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SiswaJadwalController {

    @FXML
    private Label welcomeLabel; // Label untuk menampilkan nama siswa
    @FXML
    private Label kelasInfoLabel; // Label untuk menampilkan informasi kelas
    @FXML
    private TableView<Jadwal> jadwalTableView;
    @FXML
    private TableColumn<Jadwal, String> hariColumn;
    @FXML
    private TableColumn<Jadwal, String> mapelColumn;
    @FXML
    private TableColumn<Jadwal, String> guruColumn;
    @FXML
    private TableColumn<Jadwal, String> jamMulaiColumn;
    @FXML
    private TableColumn<Jadwal, String> jamSelesaiColumn;

    private User currentUser;
    private long idKelasSiswa; // Untuk menyimpan id_kelas siswa yang login
    private String namaKelasSiswa; // Untuk menyimpan nama kelas siswa
    private String tahunAjaranSiswa; // Untuk menyimpan tahun ajaran kelas

    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();

    // Metode ini dipanggil dari SiswaController (atau LoginController)
    public void setUser(User user) {
        this.currentUser = user;
        loadSiswaAndJadwalData();
    }

    @FXML
    void initialize() {
        // Inisialisasi kolom TableView
        hariColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHari()));
        mapelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_pelajaran()));
        guruColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_guru()));
        jamMulaiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getJam_mulai().format(DateTimeFormatter.ofPattern("HH:mm"))
        ));
        jamSelesaiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getJam_selesai().format(DateTimeFormatter.ofPattern("HH:mm"))
        ));

        jadwalTableView.setItems(jadwalList);
    }

    private void loadSiswaAndJadwalData() {
        if (currentUser == null || !currentUser.getRole().equals("Siswa")) {
            welcomeLabel.setText("Selamat datang, Pengguna!");
            kelasInfoLabel.setText("");
            return;
        }

        long siswaId = currentUser.getId();
        String siswaName = "";

        try (Connection c = MainDataSource.getConnection()) {
            // Ambil data siswa dan ID kelasnya
            String siswaQuery = "SELECT s.nama_siswa, s.id_kelas, k.nama_kelas, k.tahun_ajaran " +
                    "FROM SISWA s JOIN KELAS k ON s.id_kelas = k.id_kelas " +
                    "WHERE s.id_siswa = ?";
            PreparedStatement stmtSiswa = c.prepareStatement(siswaQuery);
            stmtSiswa.setLong(1, siswaId);
            ResultSet rsSiswa = stmtSiswa.executeQuery();

            if (rsSiswa.next()) {
                siswaName = rsSiswa.getString("nama_siswa");
                idKelasSiswa = rsSiswa.getLong("id_kelas");
                namaKelasSiswa = rsSiswa.getString("nama_kelas");
                tahunAjaranSiswa = rsSiswa.getString("tahun_ajaran");

                welcomeLabel.setText("Jadwal untuk " + siswaName + "!");
                kelasInfoLabel.setText("Kelas: " + namaKelasSiswa + " (" + tahunAjaranSiswa + ")");

                // Sekarang muat jadwal berdasarkan id_kelas siswa
                loadJadwalByKelas(c, idKelasSiswa);

            } else {
                welcomeLabel.setText("Data Siswa Tidak Ditemukan.");
                kelasInfoLabel.setText("");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa atau jadwal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadJadwalByKelas(Connection c, long idKelas) throws SQLException {
        jadwalList.clear();
        String query = "SELECT j.id_jadwal, j.id_kelas, k.nama_kelas, j.id_pelajaran, mp.nama_pelajaran, j.id_guru, g.nama_guru, j.hari, j.jam_mulai, j.jam_selesai " +
                "FROM JADWAL_PELAJARAN j " +
                "JOIN KELAS k ON j.id_kelas = k.id_kelas " +
                "JOIN MATA_PELAJARAN mp ON j.id_pelajaran = mp.id_pelajaran " +
                "JOIN GURU g ON j.id_guru = g.id_guru " +
                "WHERE j.id_kelas = ? " + // Filter berdasarkan id_kelas siswa
                "ORDER BY ARRAY_POSITION(ARRAY['Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu'], j.hari), j.jam_mulai";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idKelas);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            jadwalList.add(new Jadwal(rs));
        }
    }

    @FXML
    void onBackToSiswaDashboardClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa View");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
            Parent root = loader.load();
            SiswaController controller = loader.getController();
            controller.setUser(currentUser);
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Navigasi", "Terjadi kesalahan saat kembali ke dashboard siswa.");
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
