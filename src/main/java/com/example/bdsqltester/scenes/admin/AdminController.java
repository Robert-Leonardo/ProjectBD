package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert; // Import Alert

import java.io.IOException;

public class AdminController {

    @FXML
    private Label HiLabel;

    @FXML
    private Button InputDataSiswaButton; // FXML ID sudah benar

    @FXML
    private Label NameLabel;

    @FXML
    private Button inputJadwalButton; // FXML ID sudah benar

    private User user; // Objek user yang login

    // Metode ini dipanggil dari LoginController untuk set user yang login
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            NameLabel.setText(user.getUsername());
            HiLabel.setText("Hello, Admin"); // Pastikan ini juga di-set
        } else {
            NameLabel.setText("Admin");
            HiLabel.setText("Hello, Guest");
        }
    }

    @FXML
    void initialize(){
        // Tidak perlu inisialisasi di sini jika data user di-set via setUser()
    }

    @FXML
    void InputDataSiswaButton(ActionEvent event) { // Nama metode sesuai FXML
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin - Kelola Data Siswa");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminAcc-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat Tampilan", "Tidak dapat membuka halaman pengelolaan data siswa.");
        }
    }

    @FXML
    void inputJadwalButton(ActionEvent event) { // Metode baru untuk mengelola jadwal
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin - Kelola Jadwal Kelas");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminJadwal-view.fxml")); // Mengarah ke FXML jadwal
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat Tampilan", "Tidak dapat membuka halaman pengelolaan jadwal kelas.");
        }
    }

    @FXML
    void logOutButton(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login"); // Kembali ke judul login

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Logout", "Terjadi kesalahan saat mencoba logout.");
        }
    }

    // Helper method untuk menampilkan alert
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}