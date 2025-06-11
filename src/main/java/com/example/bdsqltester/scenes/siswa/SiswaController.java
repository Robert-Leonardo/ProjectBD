package com.example.bdsqltester.scenes.siswa;
import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SiswaController {

    @FXML
    private Button BioButton;

    @FXML
    private Button JadwalButton;

    @FXML
    private Button NilaiButton;

    @FXML
    private Label nameLabel;

    @FXML
    private Text textLabel;

    private String userId;

    public void setUserId(String id) {
        userId = id;
    }

    private void updateNameLabel() {
        if (userId != null) {
            try(Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT username FROM users WHERE id = ?");
                stmt.setString(1, userId);
                // Execute the query
                ResultSet rs = stmt.executeQuery();
                if (rs.next()){
                    nameLabel.setText(rs.getString("username"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @FXML
    void initialize(){
        updateNameLabel();
    }

    @FXML
    void logOutButton(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        // Load the admin view
        app.getPrimaryStage().setTitle("Admin View");

        // Load fxml and set the scene
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
    }
}
