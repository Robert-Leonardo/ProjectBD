package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class AdminController {

    @FXML
    private Label HiLabel;

    @FXML
    private Button InputDataSiswaButton;

    @FXML
    private Label NameLabel;

    @FXML
    private Button inputJadwalButton;

    private User user;

    public void setUser(User user) {
        this.user = user;
        NameLabel.setText(user.getUsername());
    }

    @FXML
    void initialize(){

    }

    @FXML
    void InputDataSiswaButton(ActionEvent event) throws IOException{
        HelloApplication app = HelloApplication.getApplicationInstance();
        // Load the admin view
        app.getPrimaryStage().setTitle("Add Account");

        // Load fxml and set the scene
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("adminAcc-view.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
    }

    @FXML
    void logOutButton(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        // Load the admin view
        app.getPrimaryStage().setTitle("Login");

        // Load fxml and set the scene
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
    }

}
