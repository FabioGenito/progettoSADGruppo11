package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.App;
import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}