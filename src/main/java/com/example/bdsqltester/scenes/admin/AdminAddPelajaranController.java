package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.MataPelajaran; // Import MataPelajaran DTO
import com.example.bdsqltester.dtos.User; // Import User DTO (untuk admin yang login)

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*; // Import semua kontrol
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.sql.*;

public class AdminAddPelajaranController {

    @FXML private TextField idPelajaranField;
    @FXML private TextField namaPelajaranField;

    @FXML private TableView<MataPelajaran> pelajaranTableView;
    @FXML private TableColumn<MataPelajaran, String> idColumn;
    @FXML private TableColumn<MataPelajaran, String> namaPelajaranColumn;

    private User currentUser; // Admin yang sedang login

    private final ObservableList<MataPelajaran> pelajaranList = FXCollections.observableArrayList();

    // Metode ini dipanggil dari AdminController
    public void setUser(User user) {
        this.currentUser = user;
        // Data user ini akan diteruskan kembali ke AdminController saat kembali
    }

    @FXML
    void initialize() {
        idPelajaranField.setEditable(false);
        idPelajaranField.setMouseTransparent(true);
        idPelajaranField.setFocusTraversable(false);

        // Inisialisasi kolom TableView
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_pelajaran())));
        namaPelajaranColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_pelajaran()));

        pelajaranTableView.setItems(pelajaranList);

        // Listener untuk pemilihan mata pelajaran di TableView
        pelajaranTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            onPelajaranSelected(newVal);
        });

        refreshPelajaranList();
    }

    private void refreshPelajaranList() {
        pelajaranList.clear();
        try (Connection c = MainDataSource.getConnection()) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_pelajaran, nama_pelajaran FROM MATA_PELAJARAN ORDER BY nama_pelajaran");
            while (rs.next()) {
                pelajaranList.add(new MataPelajaran(rs));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar mata pelajaran: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void onPelajaranSelected(MataPelajaran mapel) {
        if (mapel == null) {
            clearFormFields();
            return;
        }

        idPelajaranField.setText(String.valueOf(mapel.getId_pelajaran()));
        namaPelajaranField.setText(mapel.getNama_pelajaran());
    }

    private void clearFormFields() {
        idPelajaranField.clear();
        namaPelajaranField.clear();
        pelajaranTableView.getSelectionModel().clearSelection();
    }

    @FXML
    void onAddPelajaran(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    void onSavePelajaran(ActionEvent event) {
        String namaPelajaran = namaPelajaranField.getText();

        if (namaPelajaran.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Nama Mata Pelajaran tidak boleh kosong.");
            return;
        }

        if (idPelajaranField.getText().isEmpty()) { // Menambah mata pelajaran baru
            try (Connection c = MainDataSource.getConnection()) {
                String insertQuery = "INSERT INTO MATA_PELAJARAN (nama_pelajaran) VALUES (?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, namaPelajaran);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idPelajaranField.setText(String.valueOf(rs.getLong(1)));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Mata pelajaran baru berhasil ditambahkan.");

            } catch (SQLException e) {
                if (e instanceof SQLIntegrityConstraintViolationException && e.getMessage().contains("mata_pelajaran_nama_pelajaran_key")) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Nama mata pelajaran '" + namaPelajaran + "' sudah ada. Harap gunakan nama lain.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan mata pelajaran: " + e.getMessage());
                }
                e.printStackTrace();
            }
        } else { // Mengedit mata pelajaran yang sudah ada
            long idMapel = Long.parseLong(idPelajaranField.getText());
            try (Connection c = MainDataSource.getConnection()) {
                String updateQuery = "UPDATE MATA_PELAJARAN SET nama_pelajaran = ? WHERE id_pelajaran = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setString(1, namaPelajaran);
                stmt.setLong(2, idMapel);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data mata pelajaran berhasil diperbarui.");
            } catch (SQLException e) {
                if (e instanceof SQLIntegrityConstraintViolationException && e.getMessage().contains("mata_pelajaran_nama_pelajaran_key")) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Nama mata pelajaran '" + namaPelajaran + "' sudah ada untuk mata pelajaran lain.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui mata pelajaran: " + e.getMessage());
                }
                e.printStackTrace();
            }
        }
        refreshPelajaranList();
    }

    @FXML
    void onDeletePelajaran(ActionEvent event) {
        if (idPelajaranField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada mata pelajaran yang dipilih untuk dihapus.");
            return;
        }

        long idMapel = Long.parseLong(idPelajaranField.getText());

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Apakah Anda yakin ingin menghapus mata pelajaran ini?");
        confirmationAlert.setContentText("Tindakan ini tidak dapat dibatalkan. Menghapus mata pelajaran juga akan menghapus jadwal dan nilai terkait.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null;
                try {
                    conn = MainDataSource.getConnection();
                    conn.setAutoCommit(false); // Mulai transaksi

                    // Hapus relasi di tabel JADWAL_PELAJARAN
                    deleteRelatedData(conn, "JADWAL_PELAJARAN", "id_pelajaran", idMapel);
                    // Hapus relasi di tabel NILAI
                    deleteRelatedData(conn, "NILAI", "id_pelajaran", idMapel);

                    // Hapus mata pelajaran dari tabel MATA_PELAJARAN
                    String deleteQuery = "DELETE FROM MATA_PELAJARAN WHERE id_pelajaran = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setLong(1, idMapel);
                    stmt.executeUpdate();

                    conn.commit(); // Commit transaksi
                    refreshPelajaranList();
                    clearFormFields();
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Mata pelajaran berhasil dihapus.");

                } catch (SQLException e) {
                    try {
                        if (conn != null) conn.rollback(); // Rollback jika ada error
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus mata pelajaran: " + e.getMessage());
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
            adminController.setUser(currentUser); // Meneruskan objek user kembali
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