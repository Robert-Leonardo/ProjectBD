package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Nilai; // Import Nilai DTO
import com.example.bdsqltester.dtos.User; // Import User DTO

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

public class SiswaLihatNilaiController {

    @FXML
    private Label welcomeLabel; // Label untuk menampilkan nama siswa
    @FXML
    private TableView<Nilai> nilaiTableView;
    @FXML
    private TableColumn<Nilai, String> mapelColumn;
    @FXML
    private TableColumn<Nilai, String> jenisUjianColumn;
    @FXML
    private TableColumn<Nilai, String> nilaiColumn;
    @FXML
    private TableColumn<Nilai, String> semesterColumn;
    @FXML
    private TableColumn<Nilai, String> tahunAjaranColumn;
    @FXML
    private TableColumn<Nilai, String> tanggalInputColumn;

    private User currentUser;
    private long idSiswaLoggedIn; // Untuk menyimpan id_siswa yang login
    private String namaSiswaLoggedIn; // Untuk menyimpan nama siswa

    private final ObservableList<Nilai> nilaiList = FXCollections.observableArrayList();

    // Metode ini dipanggil dari SiswaController (atau LoginController)
    public void setUser(User user) {
        this.currentUser = user;
        loadSiswaDanNilaiData();
    }

    @FXML
    void initialize() {
        // Inisialisasi kolom TableView
        mapelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_pelajaran()));
        jenisUjianColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenis_ujian()));
        nilaiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNilai())));
        semesterColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSemester())));
        tahunAjaranColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTahun_ajaran()));
        tanggalInputColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTanggal_input().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        ));

        nilaiTableView.setItems(nilaiList);
    }

    private void loadSiswaDanNilaiData() {
        if (currentUser == null || !currentUser.getRole().equals("Siswa")) {
            welcomeLabel.setText("Selamat datang, Pengguna!");
            return;
        }

        idSiswaLoggedIn = currentUser.getId(); // Menggunakan ID dari objek User

        try (Connection c = MainDataSource.getConnection()) {
            // Ambil nama siswa dari tabel SISWA
            String siswaQuery = "SELECT nama_siswa FROM SISWA WHERE id_siswa = ?";
            PreparedStatement stmtSiswa = c.prepareStatement(siswaQuery);
            stmtSiswa.setLong(1, idSiswaLoggedIn);
            ResultSet rsSiswa = stmtSiswa.executeQuery();

            if (rsSiswa.next()) {
                namaSiswaLoggedIn = rsSiswa.getString("nama_siswa");
                welcomeLabel.setText("Nilai untuk " + namaSiswaLoggedIn + "!");

                // Sekarang muat nilai berdasarkan id_siswa
                loadNilaiBySiswa(c, idSiswaLoggedIn);

            } else {
                welcomeLabel.setText("Data Siswa Tidak Ditemukan.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa atau nilai: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadNilaiBySiswa(Connection c, long idSiswa) throws SQLException {
        nilaiList.clear();
        String query = "SELECT n.id_nilai, n.id_siswa, n.id_pelajaran, mp.nama_pelajaran, " +
                "n.jenis_ujian, n.nilai, n.tanggal_input, n.semester, n.tahun_ajaran " +
                "FROM NILAI n " +
                "JOIN MATA_PELAJARAN mp ON n.id_pelajaran = mp.id_pelajaran " +
                "WHERE n.id_siswa = ? " + // Filter berdasarkan id_siswa yang login
                "ORDER BY n.tahun_ajaran DESC, n.semester DESC, mp.nama_pelajaran ASC";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idSiswa);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            nilaiList.add(new Nilai(rs));
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
            controller.setUser(currentUser); // Penting: Teruskan kembali objek user yang sama
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