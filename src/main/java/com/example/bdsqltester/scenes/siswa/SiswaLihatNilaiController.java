package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Nilai;
import com.example.bdsqltester.dtos.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox; // Import ChoiceBox
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.format.DateTimeFormatter;

public class SiswaLihatNilaiController {

    @FXML
    private Label welcomeLabel;
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

    // FXML fields baru untuk filter
    @FXML private ChoiceBox<Integer> filterSemesterChoiceBox;
    @FXML private ChoiceBox<String> filterTahunAjaranChoiceBox;

    private User currentUser;
    private long idSiswaLoggedIn;
    private String namaSiswaLoggedIn;

    private final ObservableList<Nilai> nilaiList = FXCollections.observableArrayList();
    private final ObservableList<Integer> semesterFilterOptions = FXCollections.observableArrayList(1, 2);
    private final ObservableList<String> tahunAjaranFilterOptions = FXCollections.observableArrayList();


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
                cellData.getValue().getTanggal_input().format(DateTimeFormatter.ofPattern("dd MMMMyyyy"))
        ));

        nilaiTableView.setItems(nilaiList);

        // Inisialisasi filter ChoiceBoxes
        filterSemesterChoiceBox.setItems(semesterFilterOptions);
        filterTahunAjaranChoiceBox.setItems(tahunAjaranFilterOptions);

        // Tambahkan opsi "Semua Semester" dan "Semua Tahun Ajaran"
        filterSemesterChoiceBox.getItems().add(0, null); // null akan merepresentasikan "Semua Semester"
        filterTahunAjaranChoiceBox.getItems().add(0, null); // null akan merepresentasikan "Semua Tahun Ajaran"

        // Listener untuk filter
        filterSemesterChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterTahunAjaranChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        // Set default filter values
        filterSemesterChoiceBox.setValue(null);
        filterTahunAjaranChoiceBox.setValue(null);
    }

    private void loadSiswaDanNilaiData() {
        if (currentUser == null || !currentUser.getRole().equals("Siswa")) {
            welcomeLabel.setText("Selamat datang, Pengguna!");
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
                welcomeLabel.setText("Nilai untuk " + namaSiswaLoggedIn + "!");

                // Muat semua nilai tanpa filter awal
                loadAllNilaiBySiswa(c, idSiswaLoggedIn);
                // Muat opsi tahun ajaran untuk filter
                loadDistinctTahunAjaran(c, idSiswaLoggedIn);
                // Terapkan filter awal (Semua Semester, Semua Tahun Ajaran)
                applyFilter();

            } else {
                welcomeLabel.setText("Data Siswa Tidak Ditemukan.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa atau nilai: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Memuat semua nilai siswa dari database (tanpa filter awal)
    private void loadAllNilaiBySiswa(Connection c, long idSiswa) throws SQLException {
        nilaiList.clear(); // Bersihkan list yang akan difilter
        String query = "SELECT n.id_nilai, n.id_siswa, n.id_pelajaran, mp.nama_pelajaran, " +
                "n.jenis_ujian, n.nilai, n.tanggal_input, n.semester, n.tahun_ajaran " +
                "FROM NILAI n " +
                "JOIN MATA_PELAJARAN mp ON n.id_pelajaran = mp.id_pelajaran " +
                "WHERE n.id_siswa = ? " +
                "ORDER BY n.tahun_ajaran DESC, n.semester DESC, mp.nama_pelajaran ASC, n.jenis_ujian ASC";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idSiswa);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            nilaiList.add(new Nilai(rs));
        }
        // Jangan langsung setItems ke TableView di sini, karena akan difilter
    }

    // Memuat tahun ajaran unik untuk filter
    private void loadDistinctTahunAjaran(Connection c, long idSiswa) throws SQLException {
        tahunAjaranFilterOptions.clear();
        tahunAjaranFilterOptions.add(null); // Tambahkan "Semua Tahun Ajaran"
        String query = "SELECT DISTINCT tahun_ajaran FROM NILAI WHERE id_siswa = ? ORDER BY tahun_ajaran DESC";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idSiswa);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            tahunAjaranFilterOptions.add(rs.getString("tahun_ajaran"));
        }
    }

    // Menerapkan filter ke TableView
    private void applyFilter() {
        Integer selectedSemester = filterSemesterChoiceBox.getValue();
        String selectedTahunAjaran = filterTahunAjaranChoiceBox.getValue();

        ObservableList<Nilai> filteredData = FXCollections.observableArrayList();

        for (Nilai nilai : nilaiList) { // Iterasi melalui semua data yang sudah dimuat
            boolean semesterMatch = (selectedSemester == null) || (selectedSemester.equals(nilai.getSemester()));
            boolean tahunAjaranMatch = (selectedTahunAjaran == null) || (selectedTahunAjaran.equals(nilai.getTahun_ajaran()));

            if (semesterMatch && tahunAjaranMatch) {
                filteredData.add(nilai);
            }
        }
        nilaiTableView.setItems(filteredData);
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