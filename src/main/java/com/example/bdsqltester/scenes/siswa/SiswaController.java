package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.dtos.Kelas;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class SiswaController {

    @FXML
    private Label namaSiswaLabel;
    @FXML
    private Label nomorIndukLabel;
    @FXML
    private Label tanggalLahirLabel;
    @FXML
    private Label kelasLabel;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        loadSiswaData();
    }

    @FXML
    void initialize() {
        // Inisialisasi awal jika diperlukan
    }

    private void loadSiswaData() {
        if (currentUser == null || currentUser.getRole() == null || !currentUser.getRole().equals("Siswa")) {
            namaSiswaLabel.setText("Selamat datang, Pengguna!");
            nomorIndukLabel.setText("-");
            tanggalLahirLabel.setText("-");
            kelasLabel.setText("-");
            return;
        }

        long siswaId = currentUser.getId();

        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT s.nama_siswa, s.nomor_induk, s.tanggal_lahir, k.nama_kelas, k.tahun_ajaran " +
                    "FROM SISWA s JOIN KELAS k ON s.id_kelas = k.id_kelas " +
                    "WHERE s.id_siswa = ?";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, siswaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                namaSiswaLabel.setText(rs.getString("nama_siswa"));
                nomorIndukLabel.setText(rs.getString("nomor_induk"));
                tanggalLahirLabel.setText(rs.getDate("tanggal_lahir").toLocalDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
                kelasLabel.setText(rs.getString("nama_kelas") + " (" + rs.getString("tahun_ajaran") + ")");
            } else {
                namaSiswaLabel.setText("Data Siswa Tidak Ditemukan.");
                nomorIndukLabel.setText("-");
                tanggalLahirLabel.setText("-");
                kelasLabel.setText("-");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onLihatJadwalClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Jadwal Kelas Siswa"); // Judul baru

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswaJadwal-view.fxml")); // Muat FXML baru
            Parent root = loader.load();
            SiswaJadwalController controller = loader.getController();
            controller.setUser(currentUser); // Teruskan objek user ke controller jadwal
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Jadwal", "Tidak dapat memuat tampilan jadwal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onLihatNilaiClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur melihat nilai ujian akan segera hadir!");
        // TODO: Implementasi navigasi ke halaman nilai siswa
    }

    @FXML
    void onLihatPrestasiClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur melihat prestasi siswa akan segera hadir!");
        // TODO: Implementasi navigasi ke halaman prestasi siswa
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