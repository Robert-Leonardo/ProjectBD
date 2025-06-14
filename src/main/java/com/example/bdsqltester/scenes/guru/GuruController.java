package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User; // Import DTO User

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

public class GuruController {

    @FXML
    private Label namaGuruLabel;
    @FXML
    private Label usernameGuruLabel;
    @FXML
    private Label roleLabel;

    private User currentUser; // Untuk menyimpan data user yang login

    // Metode ini akan dipanggil dari LoginController
    public void setUser(User user) {
        this.currentUser = user;
        loadGuruData(); // Panggil method untuk memuat data guru
    }

    @FXML
    void initialize() {
        // Inisialisasi awal jika diperlukan
    }

    private void loadGuruData() {
        if (currentUser == null || currentUser.getRole() == null || (!currentUser.getRole().equals("Guru") && !currentUser.getRole().equals("Wali kelas"))) {
            namaGuruLabel.setText("Selamat datang, Pengguna!");
            usernameGuruLabel.setText("-");
            roleLabel.setText("-");
            return;
        }

        long guruId = currentUser.getId();
        String roleName = currentUser.getRole(); // Ambil role yang dipilih (Guru atau Wali kelas)

        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT nama_guru, username_guru FROM GURU WHERE id_guru = ?";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, guruId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                namaGuruLabel.setText(rs.getString("nama_guru"));
                usernameGuruLabel.setText(rs.getString("username_guru"));
                roleLabel.setText(roleName); // Tampilkan role yang login
            } else {
                namaGuruLabel.setText("Data Guru Tidak Ditemukan.");
                usernameGuruLabel.setText("-");
                roleLabel.setText("-");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onLihatJadwalMengajarClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur melihat jadwal mengajar akan segera hadir!");
        // TODO: Implementasi navigasi ke halaman jadwal mengajar guru
    }

    @FXML
    void onInputNilaiClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur input nilai siswa akan segera hadir!");
        // TODO: Implementasi navigasi ke halaman input nilai guru
    }

    @FXML
    void onLihatAbsensiClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur melihat absensi siswa akan segera hadir!");
        // TODO: Implementasi navigasi ke halaman absensi guru
    }


    @FXML
    void onLogoutClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login"); // Kembali ke judul login

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