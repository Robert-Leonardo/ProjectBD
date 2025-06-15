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

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        loadGuruData();
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
        String roleName = currentUser.getRole();

        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT nama_guru, username_guru FROM GURU WHERE id_guru = ?";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, guruId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                namaGuruLabel.setText(rs.getString("nama_guru"));
                usernameGuruLabel.setText(rs.getString("username_guru"));
                roleLabel.setText(roleName);
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
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Jadwal Mengajar Guru");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guruJadwal-view.fxml"));
            Parent root = loader.load();
            GuruJadwalController controller = loader.getController();
            controller.setUser(currentUser);
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Jadwal", "Tidak dapat memuat tampilan jadwal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onInputNilaiClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Input Nilai Siswa"); // Judul baru

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guruInputNilai-view.fxml")); // Muat FXML baru
            Parent root = loader.load();
            GuruInputNilaiController controller = loader.getController();
            controller.setUser(currentUser); // Teruskan objek user ke controller input nilai
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Input Nilai", "Tidak dapat memuat tampilan input nilai: " + e.getMessage());
            e.printStackTrace();
        }
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