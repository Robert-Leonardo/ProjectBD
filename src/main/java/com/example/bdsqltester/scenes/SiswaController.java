package com.example.bdsqltester.scenes;
import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.io.IOException;

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

    private User user;

    public void setUser(User user) {
        this.user = user;
        nameLabel.setText(user.getUsername());
    }

    @FXML
    void initialize(){

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
