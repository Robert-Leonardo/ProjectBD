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
    private Label NameLabel;

    @FXML
    private Button InputDataSiswaButton;

    @FXML
    private Button inputJadwalButton;

    // FXML IDs untuk tombol baru tidak diperlukan di sini karena tidak ada fx:id yang langsung dikaitkan
    // tapi metodenya tetap dibutuhkan

    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            NameLabel.setText(user.getUsername());
            HiLabel.setText("Hello,");
        } else {
            NameLabel.setText("Admin");
            HiLabel.setText("Hello, Guest");
        }
    }

    @FXML
    void initialize(){
        // Tidak ada inisialisasi tambahan yang diperlukan di sini untuk FXML ini
    }

    @FXML
    void InputDataSiswaButton(ActionEvent event) {
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
    void inputJadwalButton(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin - Kelola Jadwal Kelas");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminJadwal-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat Tampilan", "Tidak dapat membuka halaman pengelolaan jadwal kelas.");
        }
    }

    // Metode baru untuk tombol "Kelola Data Guru"
    @FXML
    void onKelolaGuruClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur pengelolaan data guru akan segera hadir!");
        // try {
        //     HelloApplication app = HelloApplication.getApplicationInstance();
        //     app.getPrimaryStage().setTitle("Admin - Kelola Data Guru");
        //     FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-guru-view.fxml"));
        //     Scene scene = new Scene(loader.load());
        //     app.getPrimaryStage().setScene(scene);
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     showAlert(Alert.AlertType.ERROR, "Error", "Tidak dapat memuat halaman kelola guru.");
        // }
    }

    // Metode baru untuk tombol "Kelola Mata Pelajaran"
    @FXML
    void onKelolaMataPelajaranClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Tersedia", "Fitur pengelolaan mata pelajaran akan segera hadir!");
        // try {
        //     HelloApplication app = HelloApplication.getApplicationInstance();
        //     app.getPrimaryStage().setTitle("Admin - Kelola Mata Pelajaran");
        //     FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-mapel-view.fxml"));
        //     Scene scene = new Scene(loader.load());
        //     app.getPrimaryStage().setScene(scene);
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     showAlert(Alert.AlertType.ERROR, "Error", "Tidak dapat memuat halaman kelola mata pelajaran.");
        // }
    }


    @FXML
    void logOutButton(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Logout", "Terjadi kesalahan saat mencoba logout.");
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