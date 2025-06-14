package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Jadwal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdminJadwalController {

    @FXML private TextField idJadwalField;
    @FXML private ChoiceBox<String> kelasChoiceBox;
    @FXML private ChoiceBox<String> mapelChoiceBox;
    @FXML private ChoiceBox<String> guruChoiceBox;
    @FXML private ChoiceBox<String> hariChoiceBox;
    @FXML private Spinner<LocalTime> jamMulaiSpinner;
    @FXML private Spinner<LocalTime> jamSelesaiSpinner;
    @FXML private ListView<Jadwal> jadwalList;

    private final ObservableList<Jadwal> jadwals = FXCollections.observableArrayList();
    private Map<String, Long> kelasMap = new HashMap<>();
    private Map<String, Long> mapelMap = new HashMap<>();
    private Map<String, Long> guruMap = new HashMap<>();

    @FXML
    void initialize() {
        idJadwalField.setEditable(false);
        idJadwalField.setMouseTransparent(true);
        idJadwalField.setFocusTraversable(false);

        // Inisialisasi ChoiceBox Hari
        hariChoiceBox.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu");

        // Inisialisasi Spinner untuk Jam Mulai dan Jam Selesai
        setupTimeSpinner(jamMulaiSpinner);
        setupTimeSpinner(jamSelesaiSpinner);

        refreshJadwalList();
        loadDropdownData(); // Muat data untuk ChoiceBox lainnya

        jadwalList.setCellFactory(param -> new ListCell<Jadwal>() {
            @Override
            protected void updateItem(Jadwal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        jadwalList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onJadwalSelected(newValue);
            }
        });
    }

    private void setupTimeSpinner(Spinner<LocalTime> spinner) {
        SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<LocalTime>() {
            {
                setConverter(new StringConverter<LocalTime>() {
                    @Override
                    public String toString(LocalTime object) {
                        return (object != null) ? object.toString() : "";
                    }

                    @Override
                    public LocalTime fromString(String string) {
                        return (string != null && !string.isEmpty()) ? LocalTime.parse(string) : null;
                    }
                });
            }

            @Override
            public void decrement(int steps) {
                LocalTime current = getValue();
                if (current != null) {
                    setValue(current.minusMinutes(steps * 15)); // Decrement by 15 minutes
                }
            }

            @Override
            public void increment(int steps) {
                LocalTime current = getValue();
                if (current != null) {
                    setValue(current.plusMinutes(steps * 15)); // Increment by 15 minutes
                }
            }
        };
        spinner.setValueFactory(valueFactory);
        spinner.getValueFactory().setValue(LocalTime.of(8, 0)); // Default value
    }


    private void loadDropdownData() {
        kelasMap.clear();
        mapelMap.clear();
        guruMap.clear();
        kelasChoiceBox.getItems().clear();
        mapelChoiceBox.getItems().clear();
        guruChoiceBox.getItems().clear();

        try (Connection c = MainDataSource.getConnection()) {
            // Load Kelas
            Statement stmtKelas = c.createStatement();
            ResultSet rsKelas = stmtKelas.executeQuery("SELECT id_kelas, nama_kelas FROM KELAS ORDER BY nama_kelas");
            while (rsKelas.next()) {
                long id = rsKelas.getLong("id_kelas");
                String nama = rsKelas.getString("nama_kelas");
                kelasMap.put(nama, id);
                kelasChoiceBox.getItems().add(nama);
            }

            // Load Mata Pelajaran
            Statement stmtMapel = c.createStatement();
            ResultSet rsMapel = stmtMapel.executeQuery("SELECT id_pelajaran, nama_pelajaran FROM MATA_PELAJARAN ORDER BY nama_pelajaran");
            while (rsMapel.next()) {
                long id = rsMapel.getLong("id_pelajaran");
                String nama = rsMapel.getString("nama_pelajaran");
                mapelMap.put(nama, id);
                mapelChoiceBox.getItems().add(nama);
            }

            // Load Guru
            Statement stmtGuru = c.createStatement();
            ResultSet rsGuru = stmtGuru.executeQuery("SELECT id_guru, nama_guru FROM GURU ORDER BY nama_guru");
            while (rsGuru.next()) {
                long id = rsGuru.getLong("id_guru");
                String nama = rsGuru.getString("nama_guru");
                guruMap.put(nama, id);
                guruChoiceBox.getItems().add(nama);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data dropdown: " + e.getMessage());
            e.printStackTrace();
        }
    }


    void refreshJadwalList() {
        jadwals.clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT j.id_jadwal, j.id_kelas, k.nama_kelas, j.id_pelajaran, mp.nama_pelajaran, j.id_guru, g.nama_guru, j.hari, j.jam_mulai, j.jam_selesai " +
                    "FROM JADWAL_PELAJARAN j " +
                    "JOIN KELAS k ON j.id_kelas = k.id_kelas " +
                    "JOIN MATA_PELAJARAN mp ON j.id_pelajaran = mp.id_pelajaran " +
                    "JOIN GURU g ON j.id_guru = g.id_guru " +
                    "ORDER BY FIELD(j.hari, 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu'), j.jam_mulai, k.nama_kelas"; // Mengurutkan berdasarkan hari dan jam
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                jadwals.add(new Jadwal(rs));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar jadwal: " + e.toString());
            e.printStackTrace();
        }
        jadwalList.setItems(jadwals);
        // Coba pilih jadwal yang terakhir dipilih atau baru ditambahkan
        try {
            if (!idJadwalField.getText().isEmpty()) {
                long id = Long.parseLong(idJadwalField.getText());
                for (Jadwal jadwal : jadwals) {
                    if (jadwal.id_jadwal == id) {
                        jadwalList.getSelectionModel().select(jadwal);
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    void onJadwalSelected(Jadwal jadwal) {
        if (jadwal == null) {
            clearFormFields();
            return;
        }

        idJadwalField.setText(String.valueOf(jadwal.id_jadwal));
        kelasChoiceBox.setValue(jadwal.nama_kelas);
        mapelChoiceBox.setValue(jadwal.nama_pelajaran);
        guruChoiceBox.setValue(jadwal.nama_guru);
        hariChoiceBox.setValue(jadwal.hari);
        jamMulaiSpinner.getValueFactory().setValue(jadwal.jam_mulai);
        jamSelesaiSpinner.getValueFactory().setValue(jadwal.jam_selesai);
    }

    @FXML
    void onAddJadwal(ActionEvent event) {
        clearFormFields();
        jadwalList.getSelectionModel().clearSelection();
    }

    private void clearFormFields() {
        idJadwalField.clear();
        kelasChoiceBox.setValue(null);
        mapelChoiceBox.setValue(null);
        guruChoiceBox.setValue(null);
        hariChoiceBox.setValue(null);
        jamMulaiSpinner.getValueFactory().setValue(LocalTime.of(8, 0));
        jamSelesaiSpinner.getValueFactory().setValue(LocalTime.of(9, 0));
    }

    @FXML
    void onDeleteJadwal(ActionEvent event) {
        if (idJadwalField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada jadwal yang dipilih untuk dihapus.");
            return;
        }

        long jadwalId = Long.parseLong(idJadwalField.getText());

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Apakah Anda yakin ingin menghapus jadwal ini?");
        confirmationAlert.setContentText("Tindakan ini tidak dapat dibatalkan.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = MainDataSource.getConnection()) {
                    String deleteQuery = "DELETE FROM JADWAL_PELAJARAN WHERE id_jadwal = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setLong(1, jadwalId);
                    stmt.executeUpdate();

                    refreshJadwalList();
                    clearFormFields();

                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Jadwal berhasil dihapus.");

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus jadwal: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void onSaveJadwal(ActionEvent event) {
        // Ambil data dari form
        String selectedKelas = kelasChoiceBox.getValue();
        String selectedMapel = mapelChoiceBox.getValue();
        String selectedGuru = guruChoiceBox.getValue();
        String selectedHari = hariChoiceBox.getValue();
        LocalTime jamMulai = jamMulaiSpinner.getValue();
        LocalTime jamSelesai = jamSelesaiSpinner.getValue();

        // Validasi input
        if (selectedKelas == null || selectedMapel == null || selectedGuru == null || selectedHari == null || jamMulai == null || jamSelesai == null) {
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Semua bidang harus diisi.");
            return;
        }
        if (jamMulai.isAfter(jamSelesai) || jamMulai.equals(jamSelesai)) {
            showAlert(Alert.AlertType.ERROR, "Validasi Waktu", "Jam mulai harus sebelum Jam selesai.");
            return;
        }

        long idKelas = kelasMap.get(selectedKelas);
        long idMapel = mapelMap.get(selectedMapel);
        long idGuru = guruMap.get(selectedGuru);

        try (Connection c = MainDataSource.getConnection()) {
            if (idJadwalField.getText().isEmpty()) {
                // Insert new jadwal
                String insertQuery = "INSERT INTO JADWAL_PELAJARAN (id_kelas, id_pelajaran, id_guru, hari, jam_mulai, jam_selesai) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, idKelas);
                stmt.setLong(2, idMapel);
                stmt.setLong(3, idGuru);
                stmt.setString(4, selectedHari);
                stmt.setTime(5, Time.valueOf(jamMulai));
                stmt.setTime(6, Time.valueOf(jamSelesai));
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idJadwalField.setText(String.valueOf(rs.getLong(1)));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Jadwal baru berhasil ditambahkan.");

            } else {
                // Update existing jadwal
                long jadwalId = Long.parseLong(idJadwalField.getText());
                String updateQuery = "UPDATE JADWAL_PELAJARAN SET id_kelas = ?, id_pelajaran = ?, id_guru = ?, hari = ?, jam_mulai = ?, jam_selesai = ? WHERE id_jadwal = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setLong(1, idKelas);
                stmt.setLong(2, idMapel);
                stmt.setLong(3, idGuru);
                stmt.setString(4, selectedHari);
                stmt.setTime(5, Time.valueOf(jamMulai));
                stmt.setTime(6, Time.valueOf(jamSelesai));
                stmt.setLong(7, jadwalId);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Jadwal berhasil diperbarui.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Tangani kasus unik constraint (misal: jadwal yang sama persis sudah ada)
            showAlert(Alert.AlertType.ERROR, "Database Error", "Jadwal yang sama (Kelas, Mapel, Guru, Hari, Jam) sudah ada atau terjadi konflik: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan jadwal: " + e.getMessage());
            e.printStackTrace();
        }

        refreshJadwalList();
    }

    @FXML
    void BackButton(ActionEvent event) throws IOException {
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
