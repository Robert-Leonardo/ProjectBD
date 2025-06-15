package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Jadwal;
import com.example.bdsqltester.dtos.User; // Import DTO User

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
import javafx.beans.property.SimpleStringProperty; // Untuk TableView CellValueFactory

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class GuruJadwalController {

    @FXML
    private Label welcomeLabel; // Label untuk menampilkan nama guru
    @FXML
    private Label guruInfoLabel; // Label untuk menampilkan username atau role
    @FXML
    private TableView<Jadwal> jadwalTableView;
    @FXML
    private TableColumn<Jadwal, String> hariColumn;
    @FXML
    private TableColumn<Jadwal, String> kelasColumn; // Menambahkan kolom kelas
    @FXML
    private TableColumn<Jadwal, String> mapelColumn;
    @FXML
    private TableColumn<Jadwal, String> jamMulaiColumn;
    @FXML
    private TableColumn<Jadwal, String> jamSelesaiColumn;

    private User currentUser;
    private long idGuruLoggedIn; // Untuk menyimpan id_guru yang login
    private String namaGuruLoggedIn; // Untuk menyimpan nama guru

    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();

    // Metode ini dipanggil dari GuruController (atau LoginController)
    public void setUser(User user) {
        this.currentUser = user;
        loadGuruAndJadwalData();
    }

    @FXML
    void initialize() {
        // Inisialisasi kolom TableView
        hariColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHari()));
        kelasColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_kelas())); // Tampilkan nama kelas
        mapelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_pelajaran()));
        jamMulaiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getJam_mulai().format(DateTimeFormatter.ofPattern("HH:mm"))
        ));
        jamSelesaiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getJam_selesai().format(DateTimeFormatter.ofPattern("HH:mm"))
        ));

        jadwalTableView.setItems(jadwalList);
    }

    private void loadGuruAndJadwalData() {
        if (currentUser == null || (!currentUser.getRole().equals("Guru") && !currentUser.getRole().equals("Wali kelas"))) {
            welcomeLabel.setText("Selamat datang, Pengguna!");
            guruInfoLabel.setText("");
            return;
        }

        idGuruLoggedIn = currentUser.getId(); // Menggunakan ID dari objek User
        String roleName = currentUser.getRole();

        try (Connection c = MainDataSource.getConnection()) {
            // Ambil nama guru dari tabel GURU
            String guruQuery = "SELECT nama_guru FROM GURU WHERE id_guru = ?";
            PreparedStatement stmtGuru = c.prepareStatement(guruQuery);
            stmtGuru.setLong(1, idGuruLoggedIn);
            ResultSet rsGuru = stmtGuru.executeQuery();

            if (rsGuru.next()) {
                namaGuruLoggedIn = rsGuru.getString("nama_guru");

                welcomeLabel.setText("Jadwal Mengajar " + namaGuruLoggedIn + "!");
                guruInfoLabel.setText("Peran: " + roleName);

                // Sekarang muat jadwal berdasarkan id_guru
                loadJadwalByGuru(c, idGuruLoggedIn);

            } else {
                welcomeLabel.setText("Data Guru Tidak Ditemukan.");
                guruInfoLabel.setText("");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru atau jadwal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadJadwalByGuru(Connection c, long idGuru) throws SQLException {
        jadwalList.clear();
        String query = "SELECT j.id_jadwal, j.id_kelas, k.nama_kelas, j.id_pelajaran, mp.nama_pelajaran, j.id_guru, g.nama_guru, j.hari, j.jam_mulai, j.jam_selesai " +
                "FROM JADWAL_PELAJARAN j " +
                "JOIN KELAS k ON j.id_kelas = k.id_kelas " +
                "JOIN MATA_PELAJARAN mp ON j.id_pelajaran = mp.id_pelajaran " +
                "JOIN GURU g ON j.id_guru = g.id_guru " +
                "WHERE j.id_guru = ? " + // Filter berdasarkan id_guru yang login
                "ORDER BY ARRAY_POSITION(ARRAY['Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu'], j.hari), j.jam_mulai";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idGuru);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            jadwalList.add(new Jadwal(rs));
        }
    }

    @FXML
    void onBackToGuruDashboardClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Guru View");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml"));
            Parent root = loader.load();
            GuruController controller = loader.getController();
            controller.setUser(currentUser); // Penting: Teruskan kembali objek user yang sama
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Navigasi", "Terjadi kesalahan saat kembali ke dashboard guru.");
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
