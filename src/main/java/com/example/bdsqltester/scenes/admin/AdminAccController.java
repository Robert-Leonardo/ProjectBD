package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Siswa;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox; // Import VBox if needed for UI elements within FXML
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminAccController {

    // ... (Your other FXML fields and ObservableList) ...
    @FXML private TextField idField;
    @FXML private TextField nomorIndukField;
    @FXML private TextField namaSiswaField;
    @FXML private DatePicker tanggalLahirPicker;
    @FXML private TextField alamatField;
    @FXML private ChoiceBox<String> kelasChoiceBox;
    @FXML private ListView<Siswa> siswaList;
    @FXML private TextArea notesField; // Assuming you renamed instructionsField
    @FXML private TextArea queryTestField; // Assuming you renamed answerKeyField

    private final ObservableList<Siswa> siswas = FXCollections.observableArrayList();
    private Map<String, Long> kelasMap = new HashMap<>();

    @FXML
    void initialize() {
        // ... (Your existing initialize logic) ...
        idField.setEditable(false);
        idField.setMouseTransparent(true);
        idField.setFocusTraversable(false);

        refreshSiswaList();
        loadKelasData();

        siswaList.setCellFactory(param -> new ListCell<Siswa>() {
            @Override
            protected void updateItem(Siswa item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        siswaList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onSiswaSelected(newValue);
            }
        });
    }

    private void loadKelasData() {
        kelasMap.clear();
        kelasChoiceBox.getItems().clear();
        try (Connection c = MainDataSource.getConnection()) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_kelas, nama_kelas FROM KELAS ORDER BY nama_kelas");
            while (rs.next()) {
                long idKelas = rs.getLong("id_kelas");
                String namaKelas = rs.getString("nama_kelas");
                kelasMap.put(namaKelas, idKelas);
                kelasChoiceBox.getItems().add(namaKelas);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void refreshSiswaList() {
        siswas.clear();
        try (Connection c = MainDataSource.getConnection()) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_siswa, nomor_induk, nama_siswa, tanggal_lahir, alamat_rumah, id_kelas FROM SISWA");
            while (rs.next()) {
                siswas.add(new Siswa(rs));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.toString());
            e.printStackTrace();
        }
        siswaList.setItems(siswas);
        try {
            if (!idField.getText().isEmpty()) {
                long id = Long.parseLong(idField.getText());
                for (Siswa siswa : siswas) {
                    if (siswa.id_siswa == id) {
                        siswaList.getSelectionModel().select(siswa);
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Ignore, idField is empty
        }
    }

    void onSiswaSelected(Siswa siswa) {
        if (siswa == null) {
            idField.clear();
            nomorIndukField.clear();
            namaSiswaField.clear();
            tanggalLahirPicker.setValue(null);
            alamatField.clear();
            kelasChoiceBox.setValue(null);
            siswaList.getSelectionModel().clearSelection();
            return;
        }

        idField.setText(String.valueOf(siswa.id_siswa));
        nomorIndukField.setText(siswa.nomor_induk);
        namaSiswaField.setText(siswa.nama_siswa);
        tanggalLahirPicker.setValue(siswa.tanggal_lahir);
        alamatField.setText(siswa.alamat_rumah);

        String namaKelasTerpilih = null;
        for (Map.Entry<String, Long> entry : kelasMap.entrySet()) {
            if (entry.getValue() == siswa.id_kelas) {
                namaKelasTerpilih = entry.getKey();
                break;
            }
        }
        kelasChoiceBox.setValue(namaKelasTerpilih);
    }

    @FXML
    void onAddSiswa(ActionEvent event) {
        idField.clear();
        nomorIndukField.clear();
        namaSiswaField.clear();
        tanggalLahirPicker.setValue(null);
        alamatField.clear();
        kelasChoiceBox.setValue(null);
        siswaList.getSelectionModel().clearSelection();
    }

    @FXML
    void onDeleteSiswa(ActionEvent event) {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada siswa yang dipilih untuk dihapus.");
            return;
        }

        long siswaId = Long.parseLong(idField.getText());

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Apakah Anda yakin ingin menghapus data siswa ini?");
        confirmationAlert.setContentText("Tindakan ini tidak dapat dibatalkan. Menghapus siswa juga akan menghapus nilai, absensi, dan prestasi terkait.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null; // Deklarasikan conn di sini agar scope-nya luas
                try {
                    conn = MainDataSource.getConnection(); // Inisialisasi di dalam try
                    conn.setAutoCommit(false); // Matikan autocommit

                    deleteRelatedData(conn, "NILAI", "id_siswa", siswaId);
                    deleteRelatedData(conn, "ABSENSI_SISWA", "id_siswa", siswaId);
                    deleteRelatedData(conn, "PRESTASI_SISWA", "id_siswa", siswaId);

                    String deleteQuery = "DELETE FROM SISWA WHERE id_siswa = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setLong(1, siswaId);
                    stmt.executeUpdate();

                    conn.commit(); // Commit transaksi jika semua berhasil
                    refreshSiswaList();
                    onSiswaSelected(null);

                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data siswa berhasil dihapus.");

                } catch (SQLException e) {
                    try {
                        if (conn != null) conn.rollback(); // Akses conn yang sudah dideklarasikan
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus siswa: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        if (conn != null) conn.setAutoCommit(true); // Akses conn yang sudah dideklarasikan
                        if (conn != null) conn.close(); // Penting: Tutup koneksi di sini
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
    void onSaveSiswa(ActionEvent event) {
        String nomorInduk = nomorIndukField.getText();
        String namaSiswa = namaSiswaField.getText();
        LocalDate tanggalLahir = tanggalLahirPicker.getValue();
        String alamat = alamatField.getText();
        String namaKelasTerpilih = kelasChoiceBox.getValue();

        if (nomorInduk.isEmpty() || namaSiswa.isEmpty() || tanggalLahir == null || namaKelasTerpilih == null) {
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Nomor Induk, Nama Siswa, Tanggal Lahir, dan Kelas tidak boleh kosong.");
            return;
        }

        long idKelas = kelasMap.get(namaKelasTerpilih);

        if (idField.getText().isEmpty()) {
            try (Connection c = MainDataSource.getConnection()) {
                String initialPassword = tanggalLahir.toString();

                String insertQuery = "INSERT INTO SISWA (nomor_induk, password, nama_siswa, tanggal_lahir, alamat_rumah, id_kelas) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, nomorInduk);
                stmt.setString(2, initialPassword);
                stmt.setString(3, namaSiswa);
                stmt.setDate(4, Date.valueOf(tanggalLahir));
                stmt.setString(5, alamat);
                stmt.setLong(6, idKelas);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idField.setText(String.valueOf(rs.getLong(1)));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data siswa baru berhasil ditambahkan.");

            } catch (SQLIntegrityConstraintViolationException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Nomor Induk " + nomorInduk + " sudah ada. Harap gunakan Nomor Induk lain.");
                e.printStackTrace();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan siswa: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            long siswaId = Long.parseLong(idField.getText());
            try (Connection c = MainDataSource.getConnection()) {
                String updateQuery = "UPDATE SISWA SET nomor_induk = ?, nama_siswa = ?, tanggal_lahir = ?, alamat_rumah = ?, id_kelas = ? WHERE id_siswa = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setString(1, nomorInduk);
                stmt.setString(2, namaSiswa);
                stmt.setDate(3, Date.valueOf(tanggalLahir));
                stmt.setString(4, alamat);
                stmt.setLong(5, idKelas);
                stmt.setLong(6, siswaId);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data siswa berhasil diperbarui.");
            } catch (SQLIntegrityConstraintViolationException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Nomor Induk " + nomorInduk + " sudah ada untuk siswa lain.");
                e.printStackTrace();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui siswa: " + e.getMessage());
                e.printStackTrace();
            }
        }
        refreshSiswaList();
    }


    @FXML
    void onShowGradesClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur", "Fitur melihat nilai siswa akan diimplementasikan di kemudian hari.");
    }

    @FXML
    void onTestButtonClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur", "Fungsi Test Query masih tersedia.");

        Stage stage = new Stage();
        stage.setTitle("Query Results");

        TableView<ArrayList<String>> tableView = new TableView<>();

        ObservableList<ArrayList<String>> data = FXCollections.observableArrayList();
        ArrayList<String> headers = new ArrayList<>();

        // Menggunakan queryTestField untuk query
        String query = queryTestField.getText(); // Pastikan fx:id di FXML sudah diganti

        try (Connection conn = MainDataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) { // Gunakan 'query' di sini

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i - 1;
                String headerText = metaData.getColumnLabel(i);
                headers.add(headerText);

                TableColumn<ArrayList<String>, String> column = new TableColumn<>(headerText);
                column.setCellValueFactory(cellData -> {
                    ArrayList<String> rowData = cellData.getValue();
                    if (rowData != null && columnIndex < rowData.size()) {
                        return new SimpleStringProperty(rowData.get(columnIndex));
                    } else {
                        return new SimpleStringProperty("");
                    }
                });
                column.setPrefWidth(120);
                tableView.getColumns().add(column);
            }

            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    row.add(value != null ? value : "");
                }
                data.add(row);
            }

            if (headers.isEmpty() && data.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Query Results", "The query executed successfully but returned no data.");
                return;
            }

            tableView.setItems(data);
            StackPane root = new StackPane();
            root.getChildren().add(tableView);
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "SQL Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    void BackButton(ActionEvent event) throws Exception {
        HelloApplication app = HelloApplication.getApplicationInstance();
        app.getPrimaryStage().setTitle("Admin View");
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
        Scene scene = new Scene(loader.load());
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