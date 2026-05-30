/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.TrackManager;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Fabio
 */

/**
 * Controller per la gestione dell'inserimento di una nuova traccia.
 * Implementa la validazione grafica real-time e delega l'operazione I/O
 * a un thread separato (Task) per preservare la reattività della UI.
 */
public class InsertTrackController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField albumField;
    @FXML private TextField minuteField;
    @FXML private TextField secondField;
    @FXML private TextField yearField;
    @FXML private TextField genreField;
    @FXML private TextField imageField;
    
    @FXML private Button submitButton;
    @FXML private Button backButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setOnAction(event -> navigateToHome());
        submitButton.setOnAction(event -> handleInsertTrack());
        
        setupGraphicalValidation();
    }

    /**
     * Sfrutta i listener sulle proprietà testuali per abilitare il pulsante 
     * di sottomissione solo quando i vincoli di dominio obbligatori sono soddisfatti.
     */
    private void setupGraphicalValidation() {
        titleField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        artistField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        minuteField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        secondField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        yearField.textProperty().addListener((obs, oldV, newV) -> validateFields());
        genreField.textProperty().addListener((obs, oldV, newV) -> validateFields());
    }

    private void validateFields() {
        boolean isInvalid = titleField.getText().trim().isEmpty() ||
                            artistField.getText().trim().isEmpty() ||
                            minuteField.getText().trim().isEmpty() ||
                            secondField.getText().trim().isEmpty() ||
                            yearField.getText().trim().isEmpty() ||
                            genreField.getText().trim().isEmpty();
                            
        submitButton.setDisable(isInvalid);
    }

    /**
     * Istanzia il Task asincrono per l'inserimento. 
     * Il parsing dei dati numerici avviene all'interno del call() in modo che,
     * in caso di NumberFormatException, l'eccezione venga catturata
     * dal setOnFailed e non causi il crash dell'applicazione.
     */
    private void handleInsertTrack() {
        Task<Track> insertTask = new Task<Track>() {
            @Override
            protected Track call() throws Exception {
                
                int minutes, seconds, year;
                        
                try {
                    minutes = Integer.parseInt(minuteField.getText().trim());
                    seconds = Integer.parseInt(secondField.getText().trim());
                    year = Integer.parseInt(yearField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new Exception("I campi Durata (Min/Sec) e Anno devono contenere solo numeri interi.");
                }

                int totalSeconds = (minutes * 60) + seconds;
                
                Track newTrack = new Track(
                    titleField.getText().trim(),
                    artistField.getText().trim(),
                    totalSeconds,
                    albumField.getText().trim(),
                    year,
                    genreField.getText().trim(),
                    imageField.getText().trim()
                );

                return TrackManager.getInstance().insertNewTrack(newTrack);
            }
        };

        // Gestione del fallimento (es. Database offline o formato numerico errato)
        insertTask.setOnFailed(event -> {
            Throwable error = insertTask.getException();
            AlertUtils.show(Alert.AlertType.ERROR, "Errore di Inserimento", error.getMessage());
        });

        // Gestione del successo: lancio evento di dominio e chiusura schermata
        insertTask.setOnSucceeded(event -> {
            Track insertedTrack = insertTask.getValue();            
            TrackManager.getInstance().notifyTrackAdded(insertedTrack);
            AlertUtils.show(Alert.AlertType.INFORMATION, "Successo", "Traccia inserita correttamente nella libreria!");
            navigateToHome();
        });

        // Esecuzione del task in un nuovo Thread (Background)
        new Thread(insertTask).start();
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/progettosad/mediaplayergruppo11/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}