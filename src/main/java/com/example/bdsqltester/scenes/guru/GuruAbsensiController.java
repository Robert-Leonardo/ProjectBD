package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.AbsensiSiswa;
import com.example.bdsqltester.dtos.Kelas;
import com.example.bdsqltester.dtos.MataPelajaran;
import com.example.bdsqltester.dtos.Siswa;
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

    private Map<Long, List<MataPelajaran>> jadwalMengajarKelasMapel = new HashMap<>();


    public void setUser(User user) {
        this.currentUser = user;
        loadGuruData();
    }

    @FXML
    void initialize() {
        absensiTableView.setEditable(true);

        kelasChoiceBox.setConverter(new StringConverter<Kelas>() {
            @Override public String toString(Kelas kelas) { return kelas != null ? kelas.getNama_kelas() + " (" + kelas.getTahun_ajaran() + ")" : ""; }
            @Override public Kelas fromString(String s) { return null; }
        });
        kelasChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMataPelajaranDiajarDiKelas(newVal.getId_kelas());
            } else {
                mapelChoiceBox.getItems().clear();
                absensiList.clear();
            }
        });

        mapelChoiceBox.setConverter(new StringConverter<MataPelajaran>() {
            @Override public String toString(MataPelajaran mapel) { return mapel != null ? mapel.getNama_pelajaran() : ""; }
            @Override public MataPelajaran fromString(String s) { return null; }
        });
        mapelChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (kelasChoiceBox.getValue() != null && newVal != null && tanggalAbsensiPicker.getValue() != null) {
                loadAbsensiUntukKelasTanggalMapel(
                        kelasChoiceBox.getValue().getId_kelas(),
                        tanggalAbsensiPicker.getValue(),
                        newVal.getId_pelajaran()
                );
            } else {
                absensiList.clear();
            }
        });


        tanggalAbsensiPicker.setValue(LocalDate.now());
        tanggalAbsensiPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (kelasChoiceBox.getValue() != null && mapelChoiceBox.getValue() != null && newVal != null) {
                loadAbsensiUntukKelasTanggalMapel(
                        kelasChoiceBox.getValue().getId_kelas(),
                        newVal,
                        mapelChoiceBox.getValue().getId_pelajaran()
                );
            } else {
                absensiList.clear();
            }
        });

        siswaIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_siswa())));
        siswaNamaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaSiswa()));

        statusAbsensiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        statusAbsensiColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Hadir", "Sakit", "Izin", "Alpha"));
        statusAbsensiColumn.setOnEditCommit(event -> {
            AbsensiSiswa absensi = event.getRowValue();
            absensi.setStatus(event.getNewValue());
            saveAbsensiStatus(absensi, kelasChoiceBox.getValue().getId_kelas(), mapelChoiceBox.getValue().getId_pelajaran());
        });

        keteranganColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKeterangan()));
        keteranganColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        keteranganColumn.setOnEditCommit(event -> {
            AbsensiSiswa absensi = event.getRowValue();
            absensi.setKeterangan(event.getNewValue());
            saveAbsensiStatus(absensi, kelasChoiceBox.getValue().getId_kelas(), mapelChoiceBox.getValue().getId_pelajaran());
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

                loadJadwalMengajarGuru(c);
            } else {
                welcomeLabel.setText("Data Guru Tidak Ditemukan.");
                infoGuruLabel.setText("");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadJadwalMengajarGuru(Connection c) throws SQLException {
        kelasDiampuList.clear();
        jadwalMengajarKelasMapel.clear();

        String query = "SELECT DISTINCT j.id_kelas, k.nama_kelas, k.tahun_ajaran, j.id_pelajaran, mp.nama_pelajaran " +
                "FROM JADWAL_PELAJARAN j " +
                "JOIN KELAS k ON j.id_kelas = k.id_kelas " +
                "JOIN MATA_PELAJARAN mp ON j.id_pelajaran = mp.id_pelajaran " +
                "WHERE j.id_guru = ? ORDER BY k.nama_kelas, mp.nama_pelajaran";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setLong(1, idGuruLoggedIn);
        ResultSet rs = stmt.executeQuery();

        Map<Long, Kelas> tempUniqueKelas = new HashMap<>();
        while (rs.next()) {
            long idKelas = rs.getLong("id_kelas");
            String namaKelas = rs.getString("nama_kelas");
            String tahunAjaran = rs.getString("tahun_ajaran");
            long idMapel = rs.getLong("id_pelajaran");
            String namaMapel = rs.getString("nama_pelajaran");

            tempUniqueKelas.putIfAbsent(idKelas, new Kelas(idKelas, namaKelas, tahunAjaran, null));
            jadwalMengajarKelasMapel.computeIfAbsent(idKelas, k -> new ArrayList<>()).add(new MataPelajaran(idMapel, namaMapel));
        }
        kelasDiampuList.addAll(tempUniqueKelas.values());
        kelasChoiceBox.setItems(kelasDiampuList);
        kelasChoiceBox.setDisable(false);

        if (!kelasDiampuList.isEmpty()) {
            kelasChoiceBox.getSelectionModel().selectFirst();
            tanggalAbsensiPicker.setDisable(false);
            // mapelChoiceBox akan diaktifkan di loadMataPelajaranDiajarDiKelas
        } else {
            infoGuruLabel.setText("Anda belum memiliki jadwal mengajar di kelas manapun.");
            kelasChoiceBox.setDisable(true);
            mapelChoiceBox.setDisable(true);
            tanggalAbsensiPicker.setDisable(true);
            absensiTableView.setDisable(true);
        }
    }

    private void loadMataPelajaranDiajarDiKelas(long idKelas) {
        mapelDiajarList.clear();
        mapelChoiceBox.getItems().clear();

        List<MataPelajaran> mapelTaughtInClass = jadwalMengajarKelasMapel.get(idKelas);
        if (mapelTaughtInClass != null && !mapelTaughtInClass.isEmpty()) {
            mapelDiajarList.addAll(mapelTaughtInClass);
            mapelChoiceBox.setItems(mapelDiajarList);
            mapelChoiceBox.setDisable(false); // Aktifkan mapelChoiceBox
            mapelChoiceBox.getSelectionModel().selectFirst();
        } else {
            mapelChoiceBox.setDisable(true);
        }
    }


    private void loadAbsensiUntukKelasTanggalMapel(long idKelas, LocalDate tanggal, long idMapel) {
        absensiList.clear();
        if (tanggal == null || idMapel <= 0) return;

        try (Connection c = MainDataSource.getConnection()) {
            String siswaQuery = "SELECT id_siswa, nama_siswa FROM SISWA WHERE id_kelas = ? ORDER BY nama_siswa";
            PreparedStatement stmtSiswa = c.prepareStatement(siswaQuery);
            stmtSiswa.setLong(1, idKelas);
            ResultSet rsSiswa = stmtSiswa.executeQuery();

            List<AbsensiSiswa> tempAbsensi = new ArrayList<>();
            while (rsSiswa.next()) {
                long idSiswa = rsSiswa.getLong("id_siswa");
                String namaSiswa = rsSiswa.getString("nama_siswa");
                MataPelajaran selectedMapel = mapelChoiceBox.getValue();
                String namaMapel = (selectedMapel != null) ? selectedMapel.getNama_pelajaran() : "";


                String absensiQuery = "SELECT id_absensi, status, keterangan, id_pelajaran FROM ABSENSI_SISWA WHERE id_siswa = ? AND tanggal = ? AND id_pelajaran = ?";
                PreparedStatement stmtAbsensi = c.prepareStatement(absensiQuery);
                stmtAbsensi.setLong(1, idSiswa);
                stmtAbsensi.setDate(2, java.sql.Date.valueOf(tanggal));
                stmtAbsensi.setLong(3, idMapel);
                ResultSet rsAbsensi = stmtAbsensi.executeQuery();

                if (rsAbsensi.next()) {
                    long idAbsensi = rsAbsensi.getLong("id_absensi");
                    String status = rsAbsensi.getString("status");
                    String keterangan = rsAbsensi.getString("keterangan");
                    AbsensiSiswa absensi = new AbsensiSiswa(idAbsensi, idSiswa, tanggal, status, keterangan, idMapel);
                    absensi.setNamaSiswa(namaSiswa);
                    absensi.setNamaMataPelajaran(namaMapel);
                    tempAbsensi.add(absensi);
                } else {
                    AbsensiSiswa absensi = new AbsensiSiswa(-1, idSiswa, tanggal, "Hadir", "", idMapel);
                    absensi.setNamaSiswa(namaSiswa);
                    absensi.setNamaMataPelajaran(namaMapel);
                    tempAbsensi.add(absensi);
                }
            }
            absensiList.addAll(tempAbsensi);
            absensiTableView.setDisable(false);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar absensi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveAbsensiStatus(AbsensiSiswa absensi, long idKelas, long idMapel) {
        try (Connection c = MainDataSource.getConnection()) {
            if (absensi.getId_absensi() == -1) {
                String insertQuery = "INSERT INTO ABSENSI_SISWA (id_siswa, tanggal, status, keterangan, id_pelajaran) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, absensi.getId_siswa());
                stmt.setDate(2, java.sql.Date.valueOf(absensi.getTanggal()));
                stmt.setString(3, absensi.getStatus());
                stmt.setString(4, absensi.getKeterangan());
                stmt.setLong(5, idMapel);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    absensi.setId_absensi(rs.getLong(1));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Absensi siswa " + absensi.getNamaSiswa() + " untuk " + absensi.getNamaMataPelajaran() + " berhasil ditambahkan.");
            } else {
                String updateQuery = "UPDATE ABSENSI_SISWA SET status = ?, keterangan = ? WHERE id_siswa = ? AND tanggal = ? AND id_pelajaran = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setString(1, absensi.getStatus());
                stmt.setString(2, absensi.getKeterangan());
                stmt.setLong(3, absensi.getId_siswa());
                stmt.setDate(4, java.sql.Date.valueOf(absensi.getTanggal()));
                stmt.setLong(5, idMapel);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Absensi siswa " + absensi.getNamaSiswa() + " untuk " + absensi.getNamaMataPelajaran() + " berhasil diperbarui.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Absensi untuk siswa " + absensi.getNamaSiswa() + " pada tanggal ini dan mata pelajaran ini sudah ada. " + e.getMessage());
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
            controller.setUser(currentUser);
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