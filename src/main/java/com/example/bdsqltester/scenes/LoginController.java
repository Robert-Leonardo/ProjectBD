package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.scenes.admin.AdminController;
import com.example.bdsqltester.scenes.guru.GuruController;
import com.example.bdsqltester.scenes.siswa.SiswaController;
import com.example.bdsqltester.dtos.User; // Pastikan kelas User ini ada dan sesuai

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.MessageDigest; // Diperlukan untuk MD5
import java.security.NoSuchAlgorithmException; // Diperlukan untuk MD5
import java.sql.*;

public class LoginController {

    @FXML
    private TextField passwordField;

    @FXML
    private ChoiceBox<String> selectRole;

    @FXML
    private TextField usernameField;

    // Fungsi untuk memverifikasi kredensial berdasarkan peran yang dipilih
    boolean verifyCredentials(String username, String password, String role) throws SQLException {
        // Hashing password input dari user
        String hashedPassword = password; // Menggunakan fungsi MD5 yang akan kita buat

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
                    passwordColumn = "password"; // Kolom password di tabel SISWA
                    idColumn = "id_siswa";
                }
                case "Guru", "Wali kelas" -> { // "Wali kelas" akan login melalui tabel GURU
                    tableName = "GURU";
                    usernameColumn = "username_guru";
                    passwordColumn = "password_guru"; // Kolom password di tabel GURU
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
                String dbPasswordHash = rs.getString(passwordColumn);
                // Bandingkan password yang di-hash dari input dengan yang di-hash di DB
                if (dbPasswordHash != null && dbPasswordHash.equals(hashedPassword)) {
                    return true; // Kredensial valid
                }
            }
        }
        return false; // Kredensial tidak valid
    }

    // Fungsi MD5 sederhana (untuk tujuan proyek ini saja)
    private String MD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    @FXML
    void initialize() {
        selectRole.getItems().addAll("Admin", "Siswa", "Guru", "Wali kelas");
        selectRole.setValue("Admin");
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        try {
            if (verifyCredentials(username, password, role)) {
                HelloApplication app = HelloApplication.getApplicationInstance();
                long userId = -1; // Akan menyimpan id_admin, id_siswa, atau id_guru
                String actualUsername = ""; // Akan menyimpan username_admin, nomor_induk, atau username_guru

                // Dapatkan ID pengguna dari tabel yang sesuai setelah verifikasi berhasil
                try (Connection c = MainDataSource.getConnection()) {
                    String idColumn = "";
                    String usernameDbColumn = ""; // Kolom username di database untuk role tersebut
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
                        case "Guru", "Wali kelas" -> {
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
                    // Membuat objek User. Password tidak perlu diteruskan, cukup role dan ID/username.
                    User loggedInUser = new User(userId, actualUsername, "", role);

                    switch (role) {
                        case "Admin" -> {
                            app.getPrimaryStage().setTitle("Admin View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
                            Parent root = loader.load();
                            AdminController controller = loader.getController();
                            controller.setUser(loggedInUser); // Set user object to controller
                            app.getPrimaryStage().setScene(new Scene(root));
                        }
                        case "Siswa" -> {
                            app.getPrimaryStage().setTitle("Siswa View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
                            Parent root = loader.load();
                            SiswaController controller = loader.getController();
                            controller.setUser(loggedInUser); // Set user object to controller
                            app.getPrimaryStage().setScene(new Scene(root));
                        }
                        case "Guru", "Wali kelas" -> { // Keduanya menggunakan GuruController
                            app.getPrimaryStage().setTitle("Guru View");
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml"));
                            Parent root = loader.load();
                            GuruController controller = loader.getController();
                            controller.setUser(loggedInUser); // Set user object to controller
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