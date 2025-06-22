package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.PrestasiSiswa;
import com.example.bdsqltester.dtos.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import java.time.format.DateTimeFormatter; // Hapus ini jika tidak ada pemformatan tanggal

public class SiswaPrestasiController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<PrestasiSiswa> prestasiTableView;
    @FXML private TableColumn<PrestasiSiswa, String> namaPrestasiColumn;
    @FXML private TableColumn<PrestasiSiswa, String> tingkatColumn;
    @FXML private TableColumn<PrestasiSiswa, String> jenisLombaColumn;
    @FXML private TableColumn<PrestasiSiswa, String> deskripsiColumn;


    private User currentUser;
    private long idSiswaLoggedIn;
    private String namaSiswaLoggedIn;

    private final ObservableList<PrestasiSiswa> prestasiList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.currentUser = user;
        loadSiswaDanPrestasiData();
    }

    @FXML
    void initialize() {
        // Inisialisasi kolom TableView Prestasi
        namaPrestasiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaPrestasi()));
        tingkatColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTingkat()));
        jenisLombaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenisLomba()));
        deskripsiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDeskripsi()));

        prestasiTableView.setItems(prestasiList);
    }

    private void loadSiswaDanPrestasiData() {
        if (currentUser == null || !currentUser.getRole().equals("Siswa")) {
            welcomeLabel.setText("Selamat datang!");
            return;
        }

        idSiswaLoggedIn = currentUser.getId();

        try (Connection c = MainDataSource.getConnection()) {
            String siswaQuery = "SELECT nama_siswa FROM SISWA WHERE id_siswa = ?";
            PreparedStatement stmtSiswa = c.prepareStatement(siswaQuery);
            stmtSiswa.setLong(1, idSiswaLoggedIn);
            ResultSet rsSiswa = stmtSiswa.executeQuery();

            if (rsSiswa.next()) {
                namaSiswaLoggedIn = rsSiswa.getString("nama_siswa");
                welcomeLabel.setText("Prestasi untuk " + namaSiswaLoggedIn + "!");
                loadPrestasiBySiswa(c, idSiswaLoggedIn);
            } else {
                welcomeLabel.setText("Data Siswa Tidak Ditemukan.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa atau prestasi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPrestasiBySiswa(Connection c, long idSiswa) throws SQLException {
        prestasiList.clear();
        String query = "SELECT id_prestasi, id_siswa, nama_prestasi, tingkat, jenis_lomba, deskripsi " +
                "FROM PRESTASI_SISWA WHERE id_siswa = ? ORDER BY nama_prestasi DESC";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idSiswa);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            prestasiList.add(new PrestasiSiswa(rs));
        }
    }

    @FXML
    void onBackToSiswaDashboardClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa View");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
            Parent root = loader.load();
            SiswaController controller = loader.getController();
            controller.setUser(currentUser);
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Navigasi", "Terjadi kesalahan saat kembali ke dashboard siswa.");
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