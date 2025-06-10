package com.example.bdsqltester.scenes.siswa;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

    public class SiswaController {

    @FXML
    private Button BioButton;

    @FXML
    private Button JadwalButton;

    @FXML
    private Button NilaiButton;

    @FXML
    private Label Hilabel;

    public void setName(String username){
        Hilabel.setText("Hi" + username + " !");
    }

}
