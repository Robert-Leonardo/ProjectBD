package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.AbsensiSiswa;
import com.example.bdsqltester.dtos.Kelas;
import com.example.bdsqltester.dtos.MataPelajaran;
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
import javafx.util.StringConverter;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuruAbsensiController {

    @FXML private Label welcomeLabel;
    @FXML private Label infoGuruLabel;
    @FXML private ChoiceBox<Kelas> kelasChoiceBox;
    @FXML private ChoiceBox<MataPelajaran> mapelChoiceBox;
    @FXML private DatePicker tanggalAbsensiPicker;

    @FXML private TableView<AbsensiSiswa> absensiTableView;
    @FXML private TableColumn<AbsensiSiswa, String> siswaIdColumn;
    @FXML private TableColumn<AbsensiSiswa, String> siswaNamaColumn;
    @FXML private TableColumn<AbsensiSiswa, String> statusAbsensiColumn;
    @FXML private TableColumn<AbsensiSiswa, String> keteranganColumn;

    private User currentUser;
    private long idGuruLoggedIn;
    private String namaGuruLoggedIn;

    private final ObservableList<Kelas> kelasDiampuList = FXCollections.observableArrayList();
    private final ObservableList<MataPelajaran> mapelDiajarList = FXCollections.observableArrayList();
    private final ObservableList<AbsensiSiswa> absensiList = FXCollections.observableArrayList();

    private Map<Long, List<Long>> jadwalMengajarKelasMapel = new HashMap<>();


    public void setUser(User user) {
        this.currentUser = user;
        loadGuruData();
    }

    @FXML
    void initialize() {
        absensiTableView.setEditable(true);

        kelasChoiceBox.setConverter(new StringConverter<>() {
            @Override public String toString(Kelas kelas) { return kelas != null ? kelas.getNama_kelas() + " (" + kelas.getTahun_ajaran() + ")" : ""; }
            @Override public Kelas fromString(String s) { return null; }
        });
        kelasChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMataPelajaranByKelas(newVal.getId_kelas()); // Muat mapel saat kelas dipilih
                loadAbsensiUntukKelasDanTanggal(newVal.getId_kelas(), tanggalAbsensiPicker.getValue());
            } else {
                mapelChoiceBox.getItems().clear();
                absensiList.clear();
            }
        });

        mapelChoiceBox.setConverter(new StringConverter<>() {
            @Override public String toString(MataPelajaran mapel) { return mapel != null ? mapel.getNama_pelajaran() : ""; }
            @Override public MataPelajaran fromString(String s) { return null; }
        });

        tanggalAbsensiPicker.setValue(LocalDate.now()); // Default tanggal hari ini
        tanggalAbsensiPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (kelasChoiceBox.getValue() != null && newVal != null) {
                loadAbsensiUntukKelasDanTanggal(kelasChoiceBox.getValue().getId_kelas(), newVal);
            }
        });

        // Inisialisasi kolom TableView Absensi Siswa
        siswaIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_siswa())));
        siswaNamaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaSiswa()));

        // Kolom Status Absensi (menggunakan ComboBoxTableCell untuk pilihan)
        statusAbsensiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        statusAbsensiColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Hadir", "Sakit", "Izin", "Alpha"));
        statusAbsensiColumn.setOnEditCommit(event -> {
            AbsensiSiswa absensi = event.getRowValue();
            absensi.setStatus(event.getNewValue());
            // Simpan perubahan status ke database
            saveAbsensiStatus(absensi);
        });

        // Kolom Keterangan (menggunakan TextFieldTableCell untuk input bebas)
        keteranganColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKeterangan()));
        keteranganColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        keteranganColumn.setOnEditCommit(event -> {
            AbsensiSiswa absensi = event.getRowValue();
            absensi.setKeterangan(event.getNewValue());
            // Simpan perubahan keterangan ke database
            saveAbsensiStatus(absensi);
        });

        absensiTableView.setItems(absensiList);
    }

    private void loadGuruData() {
        if (currentUser == null || (!currentUser.getRole().equals("Guru") && !currentUser.getRole().equals("Wali kelas"))) {
            welcomeLabel.setText("Selamat datang!");
            infoGuruLabel.setText("Akses tidak diizinkan untuk peran ini.");
            kelasChoiceBox.setDisable(true);
            mapelChoiceBox.setDisable(true);
            tanggalAbsensiPicker.setDisable(true);
            absensiTableView.setDisable(true);
            return;
        }

        idGuruLoggedIn = currentUser.getId();

        try (Connection c = MainDataSource.getConnection()) {
            String guruQuery = "SELECT nama_guru FROM GURU WHERE id_guru = ?";
            PreparedStatement stmtGuru = c.prepareStatement(guruQuery);
            stmtGuru.setLong(1, idGuruLoggedIn);
            ResultSet rsGuru = stmtGuru.executeQuery();

            if (rsGuru.next()) {
                namaGuruLoggedIn = rsGuru.getString("nama_guru");
                welcomeLabel.setText("Absensi untuk " + namaGuruLoggedIn + "!");

                loadKelasDiajarGuru(c);
            } else {
                welcomeLabel.setText("Data Guru Tidak Ditemukan.");
                infoGuruLabel.setText("");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadKelasDiajarGuru(Connection c) throws SQLException {
        kelasDiampuList.clear();
        jadwalMengajarKelasMapel.clear();

        String query = "SELECT DISTINCT j.id_kelas, k.nama_kelas, k.tahun_ajaran, j.id_pelajaran " +
                "FROM JADWAL_PELAJARAN j " +
                "JOIN KELAS k ON j.id_kelas = k.id_kelas " +
                "WHERE j.id_guru = ? ORDER BY k.nama_kelas";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idGuruLoggedIn);
        ResultSet rs = stmt.executeQuery();

        Map<Long, Kelas> uniqueKelas = new HashMap<>();
        while (rs.next()) {
            long idKelas = rs.getLong("id_kelas");
            String namaKelas = rs.getString("nama_kelas");
            String tahunAjaran = rs.getString("tahun_ajaran");
            long idMapel = rs.getLong("id_pelajaran");

            uniqueKelas.putIfAbsent(idKelas, new Kelas(idKelas, namaKelas, tahunAjaran, null));
            jadwalMengajarKelasMapel.computeIfAbsent(idKelas, k -> new ArrayList<>()).add(idMapel);
        }
        kelasDiampuList.addAll(uniqueKelas.values());
        kelasChoiceBox.setItems(kelasDiampuList);
        kelasChoiceBox.setDisable(false);

        if (!kelasDiampuList.isEmpty()) {
            kelasChoiceBox.getSelectionModel().selectFirst();
        } else {
            infoGuruLabel.setText("Anda belum memiliki jadwal mengajar di kelas manapun.");
            kelasChoiceBox.setDisable(true);
            mapelChoiceBox.setDisable(true);
            tanggalAbsensiPicker.setDisable(true);
            absensiTableView.setDisable(true);
        }
    }

    private void loadMataPelajaranByKelas(long idKelas) {
        mapelDiajarList.clear();
        mapelChoiceBox.getItems().clear();

        List<Long> mapelIdsDiajarDiKelasIni = jadwalMengajarKelasMapel.get(idKelas);
        if (mapelIdsDiajarDiKelasIni == null || mapelIdsDiajarDiKelasIni.isEmpty()) {
            mapelChoiceBox.setDisable(true);
            return;
        }

        try (Connection c = MainDataSource.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("SELECT id_pelajaran, nama_pelajaran FROM MATA_PELAJARAN WHERE id_pelajaran IN (");
            for (int i = 0; i < mapelIdsDiajarDiKelasIni.size(); i++) {
                queryBuilder.append("?");
                if (i < mapelIdsDiajarDiKelasIni.size() - 1) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append(") ORDER BY nama_pelajaran");
            PreparedStatement stmt = c.prepareStatement(queryBuilder.toString());

            for (int i = 0; i < mapelIdsDiajarDiKelasIni.size(); i++) {
                stmt.setLong(i + 1, mapelIdsDiajarDiKelasIni.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                mapelDiajarList.add(new MataPelajaran(rs.getLong("id_pelajaran"), rs.getString("nama_pelajaran")));
            }
            mapelChoiceBox.setItems(mapelDiajarList);
            mapelChoiceBox.setDisable(false);

            if (!mapelDiajarList.isEmpty()) {
                mapelChoiceBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat mata pelajaran yang diajar di kelas ini: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadAbsensiUntukKelasDanTanggal(long idKelas, LocalDate tanggal) {
        absensiList.clear();
        if (tanggal == null) return;

        try (Connection c = MainDataSource.getConnection()) {
            // Ambil semua siswa di kelas tersebut
            String siswaQuery = "SELECT id_siswa, nama_siswa FROM SISWA WHERE id_kelas = ? ORDER BY nama_siswa";
            PreparedStatement stmtSiswa = c.prepareStatement(siswaQuery);
            stmtSiswa.setLong(1, idKelas);
            ResultSet rsSiswa = stmtSiswa.executeQuery();

            List<AbsensiSiswa> tempAbsensi = new ArrayList<>();
            while (rsSiswa.next()) {
                long idSiswa = rsSiswa.getLong("id_siswa");
                String namaSiswa = rsSiswa.getString("nama_siswa");

                // Cek status absensi siswa ini untuk tanggal yang dipilih
                String absensiQuery = "SELECT id_absensi, status, keterangan FROM ABSENSI_SISWA WHERE id_siswa = ? AND tanggal = ?";
                PreparedStatement stmtAbsensi = c.prepareStatement(absensiQuery);
                stmtAbsensi.setLong(1, idSiswa);
                stmtAbsensi.setDate(2, java.sql.Date.valueOf(tanggal));
                ResultSet rsAbsensi = stmtAbsensi.executeQuery();

                if (rsAbsensi.next()) {
                    // Jika sudah ada data absensi
                    long idAbsensi = rsAbsensi.getLong("id_absensi");
                    String status = rsAbsensi.getString("status");
                    String keterangan = rsAbsensi.getString("keterangan");
                    AbsensiSiswa absensi = new AbsensiSiswa(idAbsensi, idSiswa, tanggal, status, keterangan);
                    absensi.setNamaSiswa(namaSiswa);
                    tempAbsensi.add(absensi);
                } else {
                    // Jika belum ada data absensi, buat entri default "Hadir"
                    AbsensiSiswa absensi = new AbsensiSiswa(-1, idSiswa, tanggal, "Hadir", ""); // id_absensi -1 menandakan baru
                    absensi.setNamaSiswa(namaSiswa);
                    tempAbsensi.add(absensi);
                }
            }
            absensiList.addAll(tempAbsensi);
            absensiTableView.setDisable(false); // Aktifkan tabel
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar absensi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveAbsensiStatus(AbsensiSiswa absensi) {
        try (Connection c = MainDataSource.getConnection()) {
            if (absensi.getId_absensi() == -1) { // Absensi baru, lakukan INSERT
                String insertQuery = "INSERT INTO ABSENSI_SISWA (id_siswa, tanggal, status, keterangan) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, absensi.getId_siswa());
                stmt.setDate(2, java.sql.Date.valueOf(absensi.getTanggal()));
                stmt.setString(3, absensi.getStatus());
                stmt.setString(4, absensi.getKeterangan());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    absensi.setId_absensi(rs.getLong(1)); // Update id_absensi di DTO
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Absensi siswa " + absensi.getNamaSiswa() + " berhasil ditambahkan.");
            } else { // Absensi sudah ada, lakukan UPDATE
                String updateQuery = "UPDATE ABSENSI_SISWA SET status = ?, keterangan = ? WHERE id_absensi = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setString(1, absensi.getStatus());
                stmt.setString(2, absensi.getKeterangan());
                stmt.setLong(3, absensi.getId_absensi());
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Absensi siswa " + absensi.getNamaSiswa() + " berhasil diperbarui.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Tangani UNIQUE constraint jika ada (id_siswa, tanggal)
            showAlert(Alert.AlertType.ERROR, "Database Error", "Absensi untuk siswa " + absensi.getNamaSiswa() + " pada tanggal ini sudah ada. " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan absensi: " + e.getMessage());
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
            controller.setUser(currentUser); // Meneruskan objek user kembali
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