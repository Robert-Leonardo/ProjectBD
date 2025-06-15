package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.scenes.admin.AdminController;
import com.example.bdsqltester.scenes.guru.GuruController;
import com.example.bdsqltester.scenes.siswa.SiswaController;
import com.example.bdsqltester.dtos.User;

import com.example.bdsqltester.scenes.waliKelas.WaliKelasController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
// Hapus semua import yang terkait dengan hashing
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField passwordField;

    @FXML
    private ChoiceBox<String> selectRole;

    @FXML
    private TextField usernameField;

    // Fungsi untuk memverifikasi kredensial berdasarkan peran yang dipilih
    // TIDAK MENGGUNAKAN HASHING SAMA SEKALI, LANGSUNG MEMBANDINGKAN PLAIN TEXT
    boolean verifyCredentials(String username, String plainTextPasswordDariInput, String role) throws SQLException {
        try (Connection c = MainDataSource.getConnection()) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String tableName = "";
            String usernameColumn = "";
            String passwordColumn = "";
            String idColumn = "";

            switch (role) {
                case "Admin" -> {
                    tableName = "ADMIN";
                    usernameColumn = "username_admin";
                    passwordColumn = "password_admin";
                    idColumn = "id_admin";
                }
                case "Siswa" -> {
                    tableName = "SISWA";
                    usernameColumn = "nomor_induk";
                    passwordColumn = "password";
                    idColumn = "id_siswa";
                }
                case "Guru", "Wali kelas" -> { // Keduanya login melalui tabel GURU
                    tableName = "GURU";
                    usernameColumn = "username_guru";
                    passwordColumn = "password_guru";
                    idColumn = "id_guru";
                }
                default -> {
                    return false; // Peran tidak valid
                }
            }

            String query = "SELECT " + idColumn + ", " + passwordColumn + " FROM " + tableName + " WHERE " + usernameColumn + " = ?";
            stmt = c.prepareStatement(query);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPlainTextPassword = rs.getString(passwordColumn);
                // Bandingkan password dari input langsung dengan yang ada di DB
                if (dbPlainTextPassword != null && dbPlainTextPassword.equals(plainTextPasswordDariInput)) {
                    return true; // Kredensial valid
                }
            }
        }
        return false; // Kredensial tidak valid
    }

    @FXML
    void initialize() {
        selectRole.getItems().addAll("Admin", "Siswa", "Guru", "Wali kelas");
        selectRole.setValue("Admin");
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText(); // Password dalam bentuk plain text
        String role = selectRole.getValue(); // Role yang dipilih dari ChoiceBox

        try {
            if (verifyCredentials(username, password, role)) { // Menggunakan plain text password
                HelloApplication app = HelloApplication.getApplicationInstance();
                long userId = -1;
                String actualUsername = "";

                // Dapatkan ID pengguna dari tabel yang sesuai setelah verifikasi berhasil
                try (Connection c = MainDataSource.getConnection()) {
                    String idColumn = "";
                    String usernameDbColumn = "";
                    String tableName = "";

                    switch (role) {
                        case "Admin" -> {
                            tableName = "ADMIN";
                            idColumn = "id_admin";
                            usernameDbColumn = "username_admin";
                        }
                        case "Siswa" -> {
                            tableName = "SISWA";
                            idColumn = "id_siswa";
                            usernameDbColumn = "nomor_induk";
                        }
                        case "Guru", "Wali kelas" -> { // Keduanya dari tabel GURU
                            tableName = "GURU";
                            idColumn = "id_guru";
                            usernameDbColumn = "username_guru";
                        }
                    }

                    PreparedStatement stmt = c.prepareStatement("SELECT " + idColumn + ", " + usernameDbColumn + " FROM " + tableName + " WHERE " + usernameDbColumn + " = ?");
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        userId = rs.getLong(idColumn);
                        actualUsername = rs.getString(usernameDbColumn);
                    }
                }

                // Jika ID pengguna berhasil ditemukan
                if (userId != -1) {
                    User loggedInUser = new User(userId, actualUsername, "", role); // Objek User dengan role yang dipilih

                    switch (role) {
                        case "Admin" -> {
                            app.getPrimaryStage().setTitle("Admin View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
                            Parent root = loader.load();
                            AdminController controller = loader.getController();
                            controller.setUser(loggedInUser);
                            app.getPrimaryStage().setScene(new Scene(root));
                        }
                        case "Siswa" -> {
                            app.getPrimaryStage().setTitle("Siswa View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
                            Parent root = loader.load();
                            SiswaController controller = loader.getController();
                            controller.setUser(loggedInUser);
                            app.getPrimaryStage().setScene(new Scene(root));
                        }
                        case "Guru" -> {
                            app.getPrimaryStage().setTitle("Guru View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml"));
                            Parent root = loader.load();
                            GuruController controller = loader.getController();
                            controller.setUser(loggedInUser);
                            app.getPrimaryStage().setScene(new Scene(root));
                        }
                        case "Wali kelas" -> {
                            app.getPrimaryStage().setTitle("Wali Kelas View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("waliKelas-view.fxml"));
                            Parent root = loader.load();
                            WaliKelasController controller = loader.getController();
                            controller.setUser(loggedInUser);
                            app.getPrimaryStage().setScene(new Scene(root));
                        }
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Terjadi kesalahan saat mengambil detail pengguna. ID tidak ditemukan.");
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Username atau password salah, atau peran tidak sesuai.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Kesalahan koneksi database atau query: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading View", "Tidak dapat memuat tampilan untuk peran ini.");
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