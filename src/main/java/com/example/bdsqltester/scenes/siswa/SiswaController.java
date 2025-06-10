package com.example.bdsqltester.scenes.siswa;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

    public class SiswaController {

    @FXML
    private Button BioButton;

    @FXML
    private Button JadwalButton;

    @FXML
    private Button NilaiButton;

    @FXML
    private Text textLabel;

    public void setName(String username){
        textLabel.setText("Hi" + username + " !");
    }

}
