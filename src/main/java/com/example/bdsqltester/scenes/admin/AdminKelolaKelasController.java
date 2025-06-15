package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Kelas;
import com.example.bdsqltester.dtos.Guru;
import com.example.bdsqltester.dtos.Siswa; // Menggunakan DTO Siswa biasa
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

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class AdminKelolaKelasController { // Nama controller tetap sama

    @FXML private TextField idKelasField;
    @FXML private TextField namaKelasField;
    @FXML private TextField tahunAjaranField;
    @FXML private ChoiceBox<Guru> waliKelasChoiceBox;
    @FXML private Button clearWaliKelasButton;

    @FXML private TableView<Kelas> kelasTableView;
    @FXML private TableColumn<Kelas, String> kelasIdColumn;
    @FXML private TableColumn<Kelas, String> kelasNamaColumn;
    @FXML private TableColumn<Kelas, String> kelasTahunAjaranColumn;
    @FXML private TableColumn<Kelas, String> kelasWaliKelasColumn;

    // Siswa dalam kelas yang sedang dipilih (TableView Utama untuk Siswa)
    @FXML private TableView<Siswa> siswaDalamKelasTableView;
    @FXML private TableColumn<Siswa, String> siswaDalamKelasIdColumn;
    @FXML private TableColumn<Siswa, String> siswaDalamKelasNomorIndukColumn;
    @FXML private TableColumn<Siswa, String> siswaDalamKelasNamaColumn;
    @FXML private TableColumn<Siswa, String> siswaDalamKelasKelasAsalColumn; // Menambahkan kolom kelas asal siswa

    // Pilihan Kelas Tujuan untuk memindahkan siswa
    @FXML private ChoiceBox<Kelas> kelasTujuanChoiceBox; // Menggunakan Kelas DTO

    private User currentUser;

    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Guru> guruList = FXCollections.observableArrayList();
    private final ObservableList<Siswa> siswaDalamKelasList = FXCollections.observableArrayList(); // Hanya satu daftar siswa

    private Kelas selectedKelas; // Kelas yang sedang dipilih di tabel kelas

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    void initialize() {
        idKelasField.setEditable(false);
        idKelasField.setMouseTransparent(true);
        idKelasField.setFocusTraversable(false);

        // Inisialisasi kolom TableView Kelas
        kelasIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_kelas())));
        kelasNamaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_kelas()));
        kelasTahunAjaranColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTahun_ajaran()));
        kelasWaliKelasColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getNama_wali_kelas() != null ? cellData.getValue().getNama_wali_kelas() : "Belum Ditentukan"
        ));
        kelasTableView.setItems(kelasList);
        kelasTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            onKelasSelected(newVal);
        });

        // Inisialisasi kolom TableView Siswa Dalam Kelas
        siswaDalamKelasIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId_siswa())));
        siswaDalamKelasNomorIndukColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomor_induk()));
        siswaDalamKelasNamaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_siswa()));
        siswaDalamKelasKelasAsalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama_kelas_terkini())); // Mengambil nama kelas terkini
        siswaDalamKelasTableView.setItems(siswaDalamKelasList);

        // Inisialisasi ChoiceBox Wali Kelas
        waliKelasChoiceBox.setConverter(new StringConverter<Guru>() {
            @Override
            public String toString(Guru guru) {
                return guru != null ? guru.getNama_guru() : "";
            }
            @Override
            public Guru fromString(String s) {
                return null;
            }
        });

        // Inisialisasi ChoiceBox Kelas Tujuan
        kelasTujuanChoiceBox.setConverter(new StringConverter<Kelas>() {
            @Override
            public String toString(Kelas kelas) {
                return kelas != null ? kelas.getNama_kelas() + " (" + kelas.getTahun_ajaran() + ")" : "";
            }
            @Override
            public Kelas fromString(String s) {
                return null;
            }
        });

        // Load semua data saat inisialisasi
        loadAllData();
    }

    private void loadAllData() {
        loadKelasData();
        loadGuruData();
        // Kelas tujuan akan diisi oleh loadKelasData(), jadi kita bisa menggunakannya di sini
        kelasTujuanChoiceBox.setItems(kelasList); // Set semua kelas yang ada sebagai tujuan
        refreshSiswaListInSelectedKelas(); // Memuat siswa untuk kelas yang sedang dipilih (atau kosong jika belum ada)
        clearFormFields();
    }

    private void loadKelasData() {
        kelasList.clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT k.id_kelas, k.nama_kelas, k.tahun_ajaran, k.id_wali_kelas, g.nama_guru AS nama_wali_kelas " +
                    "FROM KELAS k LEFT JOIN GURU g ON k.id_wali_kelas = g.id_guru " +
                    "ORDER BY k.nama_kelas, k.tahun_ajaran";
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                kelasList.add(new Kelas(rs));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGuruData() {
        guruList.clear();
        waliKelasChoiceBox.getItems().clear();
        try (Connection c = MainDataSource.getConnection()) {
            String query = "SELECT id_guru, nama_guru FROM GURU ORDER BY nama_guru";
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                guruList.add(new Guru(rs.getLong("id_guru"), rs.getString("nama_guru"), null, null));
            }
            waliKelasChoiceBox.getItems().addAll(guruList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metode untuk memuat siswa hanya untuk kelas yang sedang dipilih
    private void refreshSiswaListInSelectedKelas() {
        siswaDalamKelasList.clear();
        if (selectedKelas != null) {
            try (Connection c = MainDataSource.getConnection()) {
                // Mengambil siswa dan nama kelas mereka saat ini
                String query = "SELECT s.id_siswa, s.nomor_induk, s.nama_siswa, s.tanggal_lahir, s.alamat_rumah, s.id_kelas, k_current.nama_kelas AS nama_kelas_terkini " +
                        "FROM SISWA s " +
                        "LEFT JOIN KELAS k_current ON s.id_kelas = k_current.id_kelas " +
                        "WHERE s.id_kelas = ? OR s.id_kelas IS NULL " + // Tampilkan juga siswa belum punya kelas di sini jika itu tujuannya
                        "ORDER BY s.nama_siswa";
                PreparedStatement stmt = c.prepareStatement(query);
                stmt.setLong(1, selectedKelas.getId_kelas()); // Filter siswa berdasarkan kelas yang dipilih
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    siswaDalamKelasList.add(new Siswa(rs));
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa untuk kelas: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    void onKelasSelected(Kelas kelas) {
        this.selectedKelas = kelas;
        if (kelas == null) {
            clearFormFields();
            return;
        }

        idKelasField.setText(String.valueOf(kelas.getId_kelas()));
        namaKelasField.setText(kelas.getNama_kelas());
        tahunAjaranField.setText(kelas.getTahun_ajaran());

        waliKelasChoiceBox.setValue(null);
        if (kelas.getId_wali_kelas() != null) {
            for (Guru guru : guruList) {
                if (guru.getId_guru() == kelas.getId_wali_kelas()) {
                    waliKelasChoiceBox.setValue(guru);
                    break;
                }
            }
        }
        refreshSiswaListInSelectedKelas(); // Perbarui daftar siswa di kelas ini
    }

    private void clearFormFields() {
        idKelasField.clear();
        namaKelasField.clear();
        tahunAjaranField.clear();
        waliKelasChoiceBox.setValue(null);
        selectedKelas = null;
        siswaDalamKelasList.clear(); // Kosongkan daftar siswa
        kelasTableView.getSelectionModel().clearSelection();
    }

    @FXML
    void onAddKelas(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    void onClearWaliKelas(ActionEvent event) {
        waliKelasChoiceBox.setValue(null);
    }

    @FXML
    void onSaveKelas(ActionEvent event) {
        String namaKelas = namaKelasField.getText();
        String tahunAjaran = tahunAjaranField.getText();
        Guru selectedWaliKelas = waliKelasChoiceBox.getValue();
        Long idWaliKelas = (selectedWaliKelas != null) ? selectedWaliKelas.getId_guru() : null;

        if (namaKelas.isEmpty() || tahunAjaran.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validasi Input", "Nama Kelas dan Tahun Ajaran tidak boleh kosong.");
            return;
        }

        try (Connection c = MainDataSource.getConnection()) {
            if (idKelasField.getText().isEmpty()) {
                String insertQuery = "INSERT INTO KELAS (nama_kelas, tahun_ajaran, id_wali_kelas) VALUES (?, ?, ?)";
                PreparedStatement stmt = c.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, namaKelas);
                stmt.setString(2, tahunAjaran);
                if (idWaliKelas != null) {
                    stmt.setLong(3, idWaliKelas);
                } else {
                    stmt.setNull(3, Types.BIGINT);
                }
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idKelasField.setText(String.valueOf(rs.getLong(1)));
                }
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Kelas baru berhasil ditambahkan.");

            } else {
                long idKelas = Long.parseLong(idKelasField.getText());
                String updateQuery = "UPDATE KELAS SET nama_kelas = ?, tahun_ajaran = ?, id_wali_kelas = ? WHERE id_kelas = ?";
                PreparedStatement stmt = c.prepareStatement(updateQuery);
                stmt.setString(1, namaKelas);
                stmt.setString(2, tahunAjaran);
                if (idWaliKelas != null) {
                    stmt.setLong(3, idWaliKelas);
                } else {
                    stmt.setNull(3, Types.BIGINT);
                }
                stmt.setLong(4, idKelas);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data kelas berhasil diperbarui.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("kelas_nama_kelas_key") || e.getMessage().contains("kelas_id_wali_kelas_key")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Nama kelas atau wali kelas sudah digunakan. Silakan cek kembali.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan kelas: " + e.getMessage());
            }
            e.printStackTrace();
        }
        loadAllData();
    }

    @FXML
    void onDeleteKelas(ActionEvent event) {
        if (idKelasField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Tidak ada kelas yang dipilih untuk dihapus.");
            return;
        }

        long idKelas = Long.parseLong(idKelasField.getText());

        try (Connection c = MainDataSource.getConnection()) {
            PreparedStatement checkSiswaStmt = c.prepareStatement("SELECT COUNT(*) FROM SISWA WHERE id_kelas = ?");
            checkSiswaStmt.setLong(1, idKelas);
            ResultSet rs = checkSiswaStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "Penghapusan Gagal", "Tidak dapat menghapus kelas ini karena masih ada siswa yang terdaftar di dalamnya. Pindahkan siswa terlebih dahulu.");
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memeriksa siswa di kelas: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Penghapusan");
        confirmationAlert.setHeaderText("Apakah Anda yakin ingin menghapus kelas ini?");
        confirmationAlert.setContentText("Tindakan ini tidak dapat dibatalkan. Menghapus kelas juga akan menghapus jadwal terkait.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null;
                try {
                    conn = MainDataSource.getConnection();
                    conn.setAutoCommit(false);

                    String deleteJadwalQuery = "DELETE FROM JADWAL_PELAJARAN WHERE id_kelas = ?";
                    PreparedStatement deleteJadwalStmt = conn.prepareStatement(deleteJadwalQuery);
                    deleteJadwalStmt.setLong(1, idKelas);
                    deleteJadwalStmt.executeUpdate();

                    String deleteKelasQuery = "DELETE FROM KELAS WHERE id_kelas = ?";
                    PreparedStatement deleteKelasStmt = conn.prepareStatement(deleteKelasQuery);
                    deleteKelasStmt.setLong(1, idKelas);
                    deleteKelasStmt.executeUpdate();

                    conn.commit();
                    loadAllData();
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Kelas berhasil dihapus.");

                } catch (SQLException e) {
                    try {
                        if (conn != null) conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus kelas: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        if (conn != null) conn.setAutoCommit(true);
                        if (conn != null) conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    // Aksi untuk memindahkan siswa ke kelas tujuan yang dipilih
    @FXML
    void onMoveSiswaToTargetKelas(ActionEvent event) {
        Siswa selectedSiswaToMove = siswaDalamKelasTableView.getSelectionModel().getSelectedItem();
        Kelas targetKelas = kelasTujuanChoiceBox.getValue();

        if (selectedSiswaToMove == null) {
            showAlert(Alert.AlertType.ERROR, "Pilih Siswa", "Pilih siswa yang akan dipindahkan.");
            return;
        }
        if (targetKelas == null) {
            showAlert(Alert.AlertType.ERROR, "Pilih Kelas Tujuan", "Pilih kelas tujuan untuk siswa.");
            return;
        }
        if (selectedKelas != null && selectedSiswaToMove.getId_kelas() == targetKelas.getId_kelas()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Siswa sudah berada di kelas tujuan yang dipilih.");
            return;
        }
        if (selectedKelas == null && selectedSiswaToMove.getId_kelas() == targetKelas.getId_kelas()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Siswa ini belum terdaftar di kelas manapun. Pilih kelas tujuannya.");
            return;
        }


        // Konfirmasi pemindahan
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Pemindahan Siswa");
        confirmationAlert.setHeaderText("Yakin ingin memindahkan siswa " + selectedSiswaToMove.getNama_siswa() + " ke kelas " + targetKelas.getNama_kelas() + " (" + targetKelas.getTahun_ajaran() + ")?");
        confirmationAlert.setContentText("Tindakan ini akan memperbarui kelas siswa.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection c = MainDataSource.getConnection()) {
                    String updateSiswaQuery = "UPDATE SISWA SET id_kelas = ? WHERE id_siswa = ?";
                    PreparedStatement stmt = c.prepareStatement(updateSiswaQuery);
                    stmt.setLong(1, targetKelas.getId_kelas());
                    stmt.setLong(2, selectedSiswaToMove.getId_siswa());
                    stmt.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Siswa berhasil dipindahkan ke kelas " + targetKelas.getNama_kelas() + ".");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memindahkan siswa: " + e.getMessage());
                    e.printStackTrace();
                }
                refreshSiswaListInSelectedKelas(); // Refresh daftar siswa di kelas yang sedang dipilih
                // Juga perlu refresh siswa di kelas tujuan jika kelas tujuan adalah kelas yang sedang aktif
                if (selectedKelas != null && selectedKelas.getId_kelas() == targetKelas.getId_kelas()) {
                    refreshSiswaListInSelectedKelas();
                } else if (targetKelas.getId_kelas() == (selectedKelas != null ? selectedKelas.getId_kelas() : 0)) { // handles if siswa moves to initially empty current class
                    refreshSiswaListInSelectedKelas();
                } else { // Jika siswa dipindahkan ke kelas yang bukan selectedKelas, refresh selectedKelas juga untuk menghilangkan siswa dari sana
                    if(selectedKelas != null) refreshSiswaListInSelectedKelas(); // Hapus siswa dari kelas lama jika bukan yang sama
                }
            }
        });
    }

    @FXML
    void BackButton(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        app.getPrimaryStage().setTitle("Admin View");
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
        Parent root = loader.load();
        AdminController adminController = loader.getController();
        if (currentUser != null) {
            adminController.setUser(currentUser);
        }
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