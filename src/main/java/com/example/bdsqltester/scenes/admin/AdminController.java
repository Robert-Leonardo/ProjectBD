package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

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
        // Initialization if needed
    }

    @FXML
    void InputDataSiswaButton(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin - Kelola Data Siswa");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminAcc-view.fxml"));
            Parent root = loader.load();
            AdminAccController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
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
            Parent root = loader.load();
            AdminJadwalController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat Tampilan", "Tidak dapat membuka halaman pengelolaan jadwal kelas.");
        }
    }

    @FXML
    void onKelolaGuruClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin - Kelola Data Guru");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminAddGuru-view.fxml"));
            Parent root = loader.load();
            AdminAddGuruController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat Tampilan", "Tidak dapat membuka halaman pengelolaan data guru.");
        }
    }

    @FXML
    void onKelolaMataPelajaranClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin - Kelola Mata Pelajaran");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminAddPelajaran-view.fxml")); // Muat FXML baru
            Parent root = loader.load();
            AdminAddPelajaranController controller = loader.getController();
            controller.setUser(user); // Teruskan objek user ke controller kelola mata pelajaran
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat Tampilan", "Tidak dapat membuka halaman pengelolaan mata pelajaran.");
        }
    }

    @FXML
    void logOutButton(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Parent root = loader.load();
            app.getPrimaryStage().setScene(new Scene(root));
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