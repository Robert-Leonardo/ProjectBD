package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Guru;
import com.example.bdsqltester.dtos.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.sql.*;

public class AdminAddGuruController {

    @FXML private TextField idGuruField;
    @FXML private TextField namaGuruField;
    @FXML private TextField usernameGuruField;

    @FXML private PasswordField passwordField;
    @FXML private TextField textField;
    @FXML private CheckBox showPasswordCheckBox;

    @FXML private TableView<Guru> guruTableView;
    @FXML private TableColumn<Guru, String> idColumn;
    @FXML private TableColumn<Guru, String> namaColumn;
    @FXML private TableColumn<Guru, String> usernameColumn;

    private User currentUser;

    private final ObservableList<Guru> guruList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    void initialize() {
        idGuruField.setEditable(false);
        idGuruField.setMouseTransparent(true);
        idGuruField.setFocusTraversable(false);

        // Inisialisasi kolom TableView
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_guru())));
        namaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_guru()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername_guru()));

        guruTableView.setItems(guruList);

        guruTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            onGuruSelected(newVal);
        });

        refreshGuruList();

        showPasswordCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                textField.setText(passwordField.getText());
                textField.setVisible(true);
                passwordField.setVisible(false);
            } else {
                passwordField.setText(textField.getText());
                passwordField.setVisible(true);
                textField.setVisible(false);
            }
        });

        // Pastikan input selalu disinkronkan saat pengguna mengetik
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!showPasswordCheckBox.isSelected()) {
                textField.setText(newText);
            }
        });

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setText(newText);
            }
        });
    }

    private void refreshGuruList() {
        guruList.clear();
        try (Connection c = MainDataSource.getConnection()) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_guru, nama_guru, username_guru, password_guru FROM GURU ORDER BY nama_guru");
            while (rs.next()) {
                guruList.add(new Guru(rs));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar guru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void onGuruSelected(Guru guru) {
        if (guru == null) {
            clearFormFields();
            return;
        }

        idGuruField.setText(String.valueOf(guru.getId_guru()));
        namaGuruField.setText(guru.getNama_guru());
        usernameGuruField.setText(guru.getUsername_guru());

        passwordField.clear();
        textField.clear();
        showPasswordCheckBox.setSelected(false);
    }

    private void clearFormFields() {
        idGuruField.clear();
        namaGuruField.clear();
        usernameGuruField.clear();
        passwordField.clear();
        textField.clear();
        showPasswordCheckBox.setSelected(false);
        passwordField.setVisible(true);
        textField.setVisible(false);
        guruTableView.getSelectionModel().clearSelection();
    }

    @FXML
    void onAddGuru(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    void onSaveGuru(ActionEvent event) {
        String namaGuru = namaGuruField.getText();
        String usernameGuru = usernameGuruField.getText();
        String passwordPlain = showPasswordCheckBox.isSelected() ? textField.getText() : passwordField.getText();

        if (namaGuru.isEmpty() || usernameGuru.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Nama Guru dan Username tidak boleh kosong.");
            return;
        }

        if (idGuruField.getText().isEmpty()) {
            if (passwordPlain.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validasi Input", "Password tidak boleh kosong untuk guru baru.");
                return;
            }
            try (Connection c = MainDataSource.getConnection()) {
                String insertQuery = "INSERT INTO GURU (nama_guru, username_guru, password_guru) VALUES (?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, namaGuru);
                stmt.setString(2, usernameGuru);
                stmt.setString(3, passwordPlain);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idGuruField.setText(String.valueOf(rs.getLong(1)));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Guru baru berhasil ditambahkan.");

            } catch (SQLIntegrityConstraintViolationException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Username " + usernameGuru + " sudah ada. Harap gunakan username lain.");
                e.printStackTrace();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan guru: " + e.getMessage());
                e.printStackTrace();
            }
        } else { // Mengedit guru yang sudah ada
            long guruId = Long.parseLong(idGuruField.getText());
            try (Connection c = MainDataSource.getConnection()) {
                String updateQuery;
                PreparedStatement stmt;

                if (passwordPlain.isEmpty()) {
                    // Update tanpa mengubah password
                    updateQuery = "UPDATE GURU SET nama_guru = ?, username_guru = ? WHERE id_guru = ?";
                    stmt = c.prepareStatement(updateQuery);
                    stmt.setString(1, namaGuru);
                    stmt.setString(2, usernameGuru);
                    stmt.setLong(3, guruId);
                } else {
                    updateQuery = "UPDATE GURU SET nama_guru = ?, username_guru = ?, password_guru = ? WHERE id_guru = ?";
                    stmt = c.prepareStatement(updateQuery);
                    stmt.setString(1, namaGuru);
                    stmt.setString(2, usernameGuru);
                    stmt.setString(3, passwordPlain);
                    stmt.setLong(4, guruId);
                }
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data guru berhasil diperbarui.");
            } catch (SQLIntegrityConstraintViolationException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Username " + usernameGuru + " sudah ada untuk guru lain.");
                e.printStackTrace();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui guru: " + e.getMessage());
                e.printStackTrace();
            }
        }
        refreshGuruList();
    }

    @FXML
    void onDeleteGuru(ActionEvent event) {
        if (idGuruField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada guru yang dipilih untuk dihapus.");
            return;
        }

        long guruId = Long.parseLong(idGuruField.getText());

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Apakah Anda yakin ingin menghapus data guru ini?");
        confirmationAlert.setContentText("Tindakan ini tidak dapat dibatalkan. Menghapus guru juga akan menghapus jadwal dan kelas yang diajar guru ini.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null;
                try {
                    conn = MainDataSource.getConnection();
                    conn.setAutoCommit(false);

                    String updateKelasQuery = "UPDATE KELAS SET id_wali_kelas = NULL WHERE id_wali_kelas = ?";
                    PreparedStatement updateKelasStmt = conn.prepareStatement(updateKelasQuery);
                    updateKelasStmt.setLong(1, guruId);
                    updateKelasStmt.executeUpdate();

                    deleteRelatedData(conn, "JADWAL_PELAJARAN", "id_guru", guruId);

                    String deleteGuruQuery = "DELETE FROM GURU WHERE id_guru = ?";
                    PreparedStatement deleteGuruStmt = conn.prepareStatement(deleteGuruQuery);
                    deleteGuruStmt.setLong(1, guruId);
                    deleteGuruStmt.executeUpdate();

                    conn.commit();
                    refreshGuruList();
                    clearFormFields();
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data guru berhasil dihapus.");

                } catch (SQLException e) {
                    try {
                        if (conn != null) conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus guru: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        if (conn != null) conn.setAutoCommit(true);
                        if (conn != null) conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void deleteRelatedData(Connection conn, String tableName, String fkColumnName, long idValue) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE " + fkColumnName + " = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setLong(1, idValue);
        stmt.executeUpdate();
    }

    @FXML
    void BackButton(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        app.getPrimaryStage().setTitle("Admin View");
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
        Parent root = loader.load();
        AdminController adminController = loader.getController();
        if (currentUser != null) {
            adminController.setUser(currentUser);
        }
        Scene scene = new Scene(root);
        app.getPrimaryStage().setScene(scene);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}