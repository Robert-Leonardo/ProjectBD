package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Siswa;
import com.example.bdsqltester.dtos.MataPelajaran;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.dtos.Jadwal; // Kita akan pakai DTO Jadwal untuk menyimpan informasi kelas dan mapel yang diajar guru

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuruInputNilaiController {

    @FXML private TextField siswaFilterField;
    @FXML private TableView<Siswa> siswaTableView;
    @FXML private TableColumn<Siswa, String> nomorIndukColumn;
    @FXML private TableColumn<Siswa, String> namaSiswaColumn;
    @FXML private TableColumn<Siswa, String> kelasSiswaColumn;

    @FXML private Label selectedSiswaLabel;
    @FXML private ChoiceBox<MataPelajaran> mapelChoiceBox;
    @FXML private ChoiceBox<String> jenisUjianChoiceBox;
    @FXML private Spinner<Double> nilaiSpinner;
    @FXML private ChoiceBox<Integer> semesterChoiceBox;
    @FXML private TextField tahunAjaranField;
    @FXML private DatePicker tanggalInputDatePicker;

    private User currentUser;
    private Siswa selectedSiswa;

    private final ObservableList<Siswa> allSiswaList = FXCollections.observableArrayList(); // Semua siswa dari DB
    private final ObservableList<Siswa> filteredSiswaList = FXCollections.observableArrayList(); // Siswa yang difilter (termasuk filter kelas guru)

    private final ObservableList<MataPelajaran> mapelListYangDiajar = FXCollections.observableArrayList(); // Hanya mapel yang diajar guru
    private Map<String, Long> kelasMap = new HashMap<>(); // Nama kelas -> ID kelas

    // Ini akan menyimpan jadwal mengajar guru yang login
    // Key: id_kelas, Value: List MataPelajaran yang diajarkan guru tersebut di kelas itu
    private Map<Long, List<MataPelajaran>> jadwalMengajarGuru = new HashMap<>();


    // Metode ini dipanggil dari GuruController
    public void setUser(User user) {
        this.currentUser = user;
        loadData(); // Muat semua data yang diperlukan
    }

    @FXML
    void initialize() {
        // Inisialisasi kolom TableView siswa
        nomorIndukColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomor_induk()));
        namaSiswaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_siswa()));
        kelasSiswaColumn.setCellValueFactory(cellData -> {
            String namaKelas = null;
            for (Map.Entry<String, Long> entry : kelasMap.entrySet()) {
                if (entry.getValue() == cellData.getValue().getId_kelas()) {
                    namaKelas = entry.getKey();
                    break;
                }
            }
            return new SimpleStringProperty(namaKelas != null ? namaKelas : "N/A");
        });

        siswaTableView.setItems(filteredSiswaList); // Set items ke filteredList, bukan allSiswaList

        // Listener untuk pemilihan siswa di TableView
        siswaTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSiswa = newVal;
            displaySelectedSiswaData();
            updateMapelChoiceBoxForSelectedSiswa(); // Perbarui pilihan mapel sesuai kelas siswa
        });

        // Inisialisasi ChoiceBox jenisUjian
        jenisUjianChoiceBox.getItems().addAll("UTS", "UAS", "Harian", "Tugas");

        // Inisialisasi Spinner Nilai (0-100 dengan langkah 0.01)
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 0.01);
        nilaiSpinner.setValueFactory(valueFactory);
        nilaiSpinner.setEditable(true);

        // Inisialisasi ChoiceBox Semester
        semesterChoiceBox.getItems().addAll(1, 2);

        // Inisialisasi DatePicker
        tanggalInputDatePicker.setValue(LocalDate.now());

        // Set StringConverter untuk mapelChoiceBox
        mapelChoiceBox.setConverter(new StringConverter<MataPelajaran>() {
            @Override
            public String toString(MataPelajaran object) {
                return object != null ? object.getNama_pelajaran() : "";
            }

            @Override
            public MataPelajaran fromString(String string) {
                return null;
            }
        });

        // Listener untuk filter siswa
        siswaFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void loadData() {
        loadKelasData();
        loadJadwalMengajarGuru(); // Muat jadwal mengajar guru ini terlebih dahulu
        refreshAllSiswaList(); // Muat semua siswa, kemudian filter
        // Set tahun ajaran saat ini secara default
        tahunAjaranField.setText(String.valueOf(LocalDate.now().getYear()) + "/" + (LocalDate.now().getYear() + 1));
    }

    private void loadKelasData() {
        kelasMap.clear();
        try (Connection c = MainDataSource.getConnection()) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_kelas, nama_kelas FROM KELAS ORDER BY nama_kelas");
            while (rs.next()) {
                long idKelas = rs.getLong("id_kelas");
                String namaKelas = rs.getString("nama_kelas");
                kelasMap.put(namaKelas, idKelas);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NEW: Memuat jadwal mengajar guru yang login
    private void loadJadwalMengajarGuru() {
        jadwalMengajarGuru.clear();
        if (currentUser == null) return;

        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT j.id_kelas, j.id_pelajaran, mp.nama_pelajaran " +
                    "FROM JADWAL_PELAJARAN j " +
                    "JOIN MATA_PELAJARAN mp ON j.id_pelajaran = mp.id_pelajaran " +
                    "WHERE j.id_guru = ?";
            PreparedStatement stmt = c.prepareStatement(query);
            stmt.setLong(1, currentUser.getId()); // ID guru yang sedang login
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long idKelas = rs.getLong("id_kelas");
                long idMapel = rs.getLong("id_pelajaran");
                String namaMapel = rs.getString("nama_pelajaran");
                MataPelajaran mapel = new MataPelajaran(idMapel, namaMapel);

                jadwalMengajarGuru.computeIfAbsent(idKelas, k -> new ArrayList<>()).add(mapel);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat jadwal mengajar guru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshAllSiswaList() {
        allSiswaList.clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT id_siswa, nomor_induk, nama_siswa, tanggal_lahir, alamat_rumah, id_kelas FROM SISWA ORDER BY nama_siswa";
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                allSiswaList.add(new Siswa(rs));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa: " + e.getMessage());
            e.printStackTrace();
        }
        applyFilters(); // Terapkan filter setelah memuat semua siswa
    }

    // NEW: Fungsi untuk menerapkan filter (pencarian + filter kelas yang diajar guru)
    private void applyFilters() {
        filteredSiswaList.clear();
        String searchText = siswaFilterField.getText() == null ? "" : siswaFilterField.getText().toLowerCase();

        // Dapatkan daftar ID kelas yang diajar oleh guru yang sedang login
        List<Long> kelasDiajarGuru = new ArrayList<>(jadwalMengajarGuru.keySet());

        for (Siswa siswa : allSiswaList) {
            boolean matchesSearch = siswa.getNama_siswa().toLowerCase().contains(searchText) ||
                    siswa.getNomor_induk().toLowerCase().contains(searchText);

            // Cek apakah siswa berada di salah satu kelas yang diajar guru
            boolean isInTaughtClass = kelasDiajarGuru.contains(siswa.getId_kelas());

            if (matchesSearch && isInTaughtClass) {
                filteredSiswaList.add(siswa);
            }
        }
    }

    // NEW: Memperbarui pilihan Mata Pelajaran berdasarkan siswa yang dipilih
    private void updateMapelChoiceBoxForSelectedSiswa() {
        mapelChoiceBox.getItems().clear();
        if (selectedSiswa != null) {
            long idKelasSiswa = selectedSiswa.getId_kelas();
            List<MataPelajaran> mapelTaughtInClass = jadwalMengajarGuru.get(idKelasSiswa);
            if (mapelTaughtInClass != null) {
                mapelChoiceBox.getItems().addAll(mapelTaughtInClass);
            }
        }
    }


    private void displaySelectedSiswaData() {
        if (selectedSiswa != null) {
            selectedSiswaLabel.setText("Siswa Terpilih: " + selectedSiswa.getNama_siswa() + " (" + selectedSiswa.getNomor_induk() + ")");
            // Reset input nilai form
            mapelChoiceBox.setValue(null);
            jenisUjianChoiceBox.setValue(null);
            nilaiSpinner.getValueFactory().setValue(0.0);
            semesterChoiceBox.setValue(null);
            tanggalInputDatePicker.setValue(LocalDate.now());
        } else {
            selectedSiswaLabel.setText("Tidak ada siswa terpilih");
            mapelChoiceBox.setValue(null);
            jenisUjianChoiceBox.setValue(null);
            nilaiSpinner.getValueFactory().setValue(0.0);
            semesterChoiceBox.setValue(null);
            tanggalInputDatePicker.setValue(null);
            mapelChoiceBox.getItems().clear(); // Penting: Kosongkan pilihan mapel jika tidak ada siswa
        }
    }

    @FXML
    void onInputNilai(ActionEvent event) {
        if (selectedSiswa == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Pilih siswa terlebih dahulu.");
            return;
        }
        MataPelajaran selectedMapel = mapelChoiceBox.getValue();
        String jenisUjian = jenisUjianChoiceBox.getValue();
        Double nilai = nilaiSpinner.getValue();
        Integer semester = semesterChoiceBox.getValue();
        String tahunAjaran = tahunAjaranField.getText();
        LocalDate tanggalInput = tanggalInputDatePicker.getValue();

        if (selectedMapel == null || jenisUjian == null || nilai == null || semester == null || tahunAjaran.isEmpty() || tanggalInput == null) {
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Semua bidang nilai harus diisi.");
            return;
        }
        if (nilai < 0 || nilai > 100) {
            showAlert(Alert.AlertType.ERROR, "Validasi Nilai", "Nilai harus antara 0 dan 100.");
            return;
        }

        // NEW VALIDATION: Pastikan guru memang mengajar mata pelajaran ini di kelas siswa tersebut
        long idKelasSiswa = selectedSiswa.getId_kelas();
        long idMapelSelected = selectedMapel.getId_pelajaran();

        if (!jadwalMengajarGuru.containsKey(idKelasSiswa) ||
                jadwalMengajarGuru.get(idKelasSiswa).stream().noneMatch(mp -> mp.getId_pelajaran() == idMapelSelected)) {
            showAlert(Alert.AlertType.ERROR, "Otorisasi Gagal", "Anda tidak memiliki wewenang untuk mengajar mata pelajaran ini di kelas siswa yang dipilih.");
            return;
        }


        try (Connection c = MainDataSource.getConnection()) {
            String checkQuery = "SELECT id_nilai FROM NILAI WHERE id_siswa = ? AND id_pelajaran = ? AND jenis_ujian = ? AND semester = ? AND tahun_ajaran = ?";
            PreparedStatement checkStmt = c.prepareStatement(checkQuery);
            checkStmt.setLong(1, selectedSiswa.getId_siswa());
            checkStmt.setLong(2, selectedMapel.getId_pelajaran());
            checkStmt.setString(3, jenisUjian);
            checkStmt.setInt(4, semester);
            checkStmt.setString(5, tahunAjaran);
            ResultSet rsCheck = checkStmt.executeQuery();

            if (rsCheck.next()) {
                long idNilaiToUpdate = rsCheck.getLong("id_nilai");
                String updateQuery = "UPDATE NILAI SET nilai = ?, tanggal_input = ? WHERE id_nilai = ?";
                PreparedStatement updateStmt = c.prepareStatement(updateQuery);
                updateStmt.setDouble(1, nilai);
                updateStmt.setDate(2, Date.valueOf(tanggalInput));
                updateStmt.setLong(3, idNilaiToUpdate);
                updateStmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Nilai berhasil diperbarui.");
            } else {
                String insertQuery = "INSERT INTO NILAI (id_siswa, id_pelajaran, jenis_ujian, nilai, tanggal_input, semester, tahun_ajaran) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setLong(1, selectedSiswa.getId_siswa());
                insertStmt.setLong(2, selectedMapel.getId_pelajaran());
                insertStmt.setString(3, jenisUjian);
                insertStmt.setDouble(4, nilai);
                insertStmt.setDate(5, Date.valueOf(tanggalInput));
                insertStmt.setInt(6, semester);
                insertStmt.setString(7, tahunAjaran);
                insertStmt.executeUpdate();

                ResultSet rsGeneratedKeys = insertStmt.getGeneratedKeys();
                if (rsGeneratedKeys.next()) {
                    // long newIdNilai = rsGeneratedKeys.getLong(1);
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Nilai baru berhasil ditambahkan.");
            }
            displaySelectedSiswaData();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan nilai: " + e.getMessage());
            e.printStackTrace();
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
            controller.setUser(currentUser);
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Navigasi", "Terjadi kesalahan saat kembali ke dashboard guru.");
            e.printStackTrace();
        }
    }

    @FXML
    void onLogoutClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Login");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Parent root = loader.load();
            app.getPrimaryStage().setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Logout", "Terjadi kesalahan saat mencoba logout.");
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