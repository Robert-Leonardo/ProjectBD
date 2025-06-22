package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.PrestasiSiswa; // Import PrestasiSiswa DTO
import com.example.bdsqltester.dtos.User; // Import User DTO (untuk admin yang login)
import com.example.bdsqltester.dtos.Siswa; // Import Siswa DTO (untuk menampilkan info siswa terpilih)

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker; // Ini bisa dihapus jika tidak ada DatePicker sama sekali
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty; // Untuk TableView CellValueFactory

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Untuk Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate; // Ini bisa dihapus jika tidak ada DatePicker sama sekali
import java.time.format.DateTimeFormatter; // Ini bisa dihapus jika tidak ada pemformatan tanggal

public class AdminKelolaPrestasiController {

    @FXML private Label namaSiswaLabel;
    @FXML private Label nomorIndukLabel;

    @FXML private TextField idPrestasiField;
    @FXML private TextField namaPrestasiField;
    @FXML private TextField tingkatPrestasiField;
    @FXML private TextField jenisLombaField;
    @FXML private TextArea deskripsiPrestasiArea;
    // @FXML private DatePicker tanggalPrestasiPicker; // HAPUS INI

    @FXML private TableView<PrestasiSiswa> prestasiTableView;
    @FXML private TableColumn<PrestasiSiswa, String> idPrestasiColumn;
    @FXML private TableColumn<PrestasiSiswa, String> namaPrestasiColumn;
    @FXML private TableColumn<PrestasiSiswa, String> tingkatColumn;
    @FXML private TableColumn<PrestasiSiswa, String> jenisLombaColumn;
    // @FXML private TableColumn<PrestasiSiswa, String> tanggalColumn; // HAPUS INI

    private User currentUser;
    private long idSiswaTarget;
    private String namaSiswaTarget;

    private final ObservableList<PrestasiSiswa> prestasiList = FXCollections.observableArrayList();

    public void setSiswaData(long idSiswa, String namaSiswa, User adminUser) {
        this.idSiswaTarget = idSiswa;
        this.namaSiswaTarget = namaSiswa;
        this.currentUser = adminUser;

        namaSiswaLabel.setText("Nama Siswa: " + namaSiswaTarget);
        loadNomorIndukSiswa(idSiswaTarget);

        refreshPrestasiList();
    }

    private void loadNomorIndukSiswa(long idSiswa) {
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT nomor_induk FROM SISWA WHERE id_siswa = ?";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, idSiswa);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nomorIndukLabel.setText("Nomor Induk: " + rs.getString("nomor_induk"));
            } else {
                nomorIndukLabel.setText("Nomor Induk: Tidak Ditemukan");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat nomor induk siswa: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void initialize() {
        idPrestasiField.setEditable(false);
        idPrestasiField.setMouseTransparent(true);
        idPrestasiField.setFocusTraversable(false);

        // Inisialisasi kolom TableView Prestasi
        idPrestasiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_prestasi())));
        namaPrestasiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaPrestasi()));
        tingkatColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTingkat()));
        jenisLombaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenisLomba()));
        // tanggalColumn.setCellValueFactory(cellData -> new SimpleStringProperty( // HAPUS INI
        //         cellData.getValue().getTanggalPrestasi().format(DateTimeFormatter.ofPattern("dd MMMMımda")) // HAPUS INI
        // )); // HAPUS INI

        prestasiTableView.setItems(prestasiList);

        // Listener untuk pemilihan prestasi di TableView
        prestasiTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            onPrestasiSelected(newVal);
        });

        // tanggalPrestasiPicker.setValue(LocalDate.now()); // HAPUS INI
    }

    private void refreshPrestasiList() {
        prestasiList.clear();
        try (Connection c = MainDataSource.getConnection()) {
            // Hapus 'tanggal_prestasi' dari SELECT query
            String query = "SELECT id_prestasi, id_siswa, nama_prestasi, tingkat, jenis_lomba, deskripsi " +
                    "FROM PRESTASI_SISWA WHERE id_siswa = ? ORDER BY nama_prestasi ASC"; // Urutkan berdasarkan nama
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, idSiswaTarget);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                prestasiList.add(new PrestasiSiswa(rs));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar prestasi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void onPrestasiSelected(PrestasiSiswa prestasi) {
        if (prestasi == null) {
            clearFormFields();
            return;
        }

        idPrestasiField.setText(String.valueOf(prestasi.getId_prestasi()));
        namaPrestasiField.setText(prestasi.getNamaPrestasi());
        tingkatPrestasiField.setText(prestasi.getTingkat());
        jenisLombaField.setText(prestasi.getJenisLomba());
        deskripsiPrestasiArea.setText(prestasi.getDeskripsi());
        // tanggalPrestasiPicker.setValue(prestasi.getTanggalPrestasi()); // HAPUS INI
    }

    private void clearFormFields() {
        idPrestasiField.clear();
        namaPrestasiField.clear();
        tingkatPrestasiField.clear();
        jenisLombaField.clear();
        deskripsiPrestasiArea.clear();
        // tanggalPrestasiPicker.setValue(LocalDate.now()); // HAPUS INI
        prestasiTableView.getSelectionModel().clearSelection();
    }

    @FXML
    void onAddPrestasi(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    void onSavePrestasi(ActionEvent event) {
        String namaPrestasi = namaPrestasiField.getText();
        String tingkatPrestasi = tingkatPrestasiField.getText();
        String jenisLomba = jenisLombaField.getText();
        String deskripsi = deskripsiPrestasiArea.getText();
        // LocalDate tanggalPrestasi = tanggalPrestasiPicker.getValue(); // HAPUS INI

        // Validasi input (hapus tanggalPrestasi dari validasi)
        if (namaPrestasi.isEmpty() || tingkatPrestasi.isEmpty()) { // Hapus && tanggalPrestasi == null
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Nama Prestasi dan Tingkat tidak boleh kosong.");
            return;
        }

        try (Connection c = MainDataSource.getConnection()) {
            if (idPrestasiField.getText().isEmpty()) { // Menambah prestasi baru
                // Hapus 'tanggal_prestasi' dari INSERT query dan parameternya
                String insertQuery = "INSERT INTO PRESTASI_SISWA (id_siswa, nama_prestasi, tingkat, jenis_lomba, deskripsi) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, idSiswaTarget);
                stmt.setString(2, namaPrestasi);
                stmt.setString(3, tingkatPrestasi);
                stmt.setString(4, jenisLomba);
                stmt.setString(5, deskripsi);
                // stmt.setDate(6, java.sql.Date.valueOf(tanggalPrestasi)); // HAPUS INI
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idPrestasiField.setText(String.valueOf(rs.getLong(1)));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Prestasi baru berhasil ditambahkan.");

            } else { // Mengedit prestasi yang sudah ada
                long idPrestasi = Long.parseLong(idPrestasiField.getText());
                // Hapus 'tanggal_prestasi' dari UPDATE query dan parameternya
                String updateQuery = "UPDATE PRESTASI_SISWA SET nama_prestasi = ?, tingkat = ?, jenis_lomba = ?, deskripsi = ? WHERE id_prestasi = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setString(1, namaPrestasi);
                stmt.setString(2, tingkatPrestasi);
                stmt.setString(3, jenisLomba);
                stmt.setString(4, deskripsi);
                // stmt.setDate(5, java.sql.Date.valueOf(tanggalPrestasi)); // HAPUS INI
                stmt.setLong(5, idPrestasi); // Sesuaikan indeks parameter
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data prestasi berhasil diperbarui.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan prestasi: " + e.getMessage());
            e.printStackTrace();
        }
        refreshPrestasiList();
    }

    @FXML
    void onDeletePrestasi(ActionEvent event) {
        if (idPrestasiField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada prestasi yang dipilih untuk dihapus.");
            return;
        }

        long idPrestasi = Long.parseLong(idPrestasiField.getText());

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Apakah Anda yakin ingin menghapus prestasi ini?");
        confirmationAlert.setContentText("Tindakan ini tidak dapat dibatalkan.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = MainDataSource.getConnection()) {
                    String deleteQuery = "DELETE FROM PRESTASI_SISWA WHERE id_prestasi = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setLong(1, idPrestasi);
                    stmt.executeUpdate();

                    refreshPrestasiList();
                    clearFormFields();
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Prestasi berhasil dihapus.");

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus prestasi: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void BackButton(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        app.getPrimaryStage().setTitle("Admin - Kelola Data Siswa");
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminAcc-view.fxml"));
        Parent root = loader.load();
        AdminAccController controller = loader.getController();
        controller.setUser(currentUser);
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