/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.strategy.LoopStrategy;
import com.progettosad.mediaplayergruppo11.model.strategy.SequentialStrategy;
import com.progettosad.mediaplayergruppo11.model.strategy.ShuffleStrategy;
import com.progettosad.mediaplayergruppo11.utils.AlertUtils;
import com.progettosad.mediaplayergruppo11.utils.TimeUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Fabio
 */

public class PlayerBarController implements Initializable {

    @FXML private Label currentTrackTitle;
    @FXML private Label currentTrackArtist;
    @FXML private Label currentTrackTime;
    @FXML private Label currentTrackDuration;
    @FXML private ProgressBar playerProgressBar;
    @FXML private Button playerButton;
    @FXML private Button skipButton;
    @FXML private Button backButton;
    @FXML private ToggleButton btnShuffle;
    @FXML private ToggleButton btnLoop;

    private PlaybackEngine engine;
    private MainShellController mainShell;

    public void setMainShell(MainShellController mainShell) {
        this.mainShell = mainShell;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        engine = PlaybackEngine.getInstance();
        setupStrategies();

        if (playerProgressBar != null) {
            playerProgressBar.progressProperty().bind(engine.progressProperty());
        }

        engine.currentTrackProperty().addListener((observable, oldTrack, newTrack) -> {
            Platform.runLater(() -> updateTrackInfo(newTrack));
        });

        updateTrackInfo(engine.currentTrackProperty().get());

        engine.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            Platform.runLater(() -> {
                if (currentTrackTime != null) {
                    currentTrackTime.setText(TimeUtils.formatSecondsToMinutes(newTime.intValue()));
                }
            });
        });

        // 5. Listener per il cambio stato Play/Pausa
        engine.isPlayingProperty().addListener((observable, oldValue, isPlaying) -> {
            Platform.runLater(() -> {
                if (playerButton != null) {
                    playerButton.setText(isPlaying ? "||" : "▶");
                }
            });
        });
    }
    
    /** T- 06/01
     * Delega l'interruzione temporanea della riproduzione al PlaybackEngine.
     * Il rispetto del pattern Passive View impone che il Controller non mantenga
     * traccia dello stato in esecuzione.
     */
    @FXML
    private void handlePause() {
        engine.pauseTrack();
    }

    /** T-06/01
     * Delega l'arresto definitivo della riproduzione al PlaybackEngine,
     * reimpostando il ciclo di vita del brano a livello di Modello.
     */
    @FXML
    private void handleStop() {
        engine.stopTrack();
    }

    /**
     * T-08/03 GEstione dell'avanzamento al brano successivo.
     * invoca il metodo nextTrack() del motore di ripoduzione
    **/
    @FXML
    private void handleNextTrack() {
        engine.nextTrack();
    }  

    private void setupStrategies() {
        ToggleGroup strategyGroup = new ToggleGroup();
        if (btnShuffle != null && btnLoop != null) {
            btnShuffle.setToggleGroup(strategyGroup);
            btnLoop.setToggleGroup(strategyGroup);

            strategyGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
                if (newToggle == btnShuffle) {
                    engine.setPlaybackStrategy(new ShuffleStrategy());
                    System.out.println("PlayerBar: Strategia SHUFFLE");
                } else if (newToggle == btnLoop) {
                    engine.setPlaybackStrategy(new LoopStrategy());
                    System.out.println("PlayerBar: Strategia LOOP");
                } else {
                    engine.setPlaybackStrategy(new SequentialStrategy());
                    System.out.println("PlayerBar: Strategia SEQUENZIALE");
                }
            });
        }
    }

    private void updateTrackInfo(Track track) {
        if (track != null) {
            if (currentTrackTitle != null) currentTrackTitle.setText(track.getTitle());
            if (currentTrackArtist != null) currentTrackArtist.setText(track.getArtist());
            if (currentTrackDuration != null) currentTrackDuration.setText(track.getFormattedLength());
        } else {
            if (currentTrackTitle != null) currentTrackTitle.setText("");
            if (currentTrackArtist != null) currentTrackArtist.setText("");
            if (currentTrackDuration != null) currentTrackDuration.setText("00:00");
            if (currentTrackTime != null) currentTrackTime.setText("00:00");
        }
    }
    
    /** T-06/02
     * Inoltra la richiesta di riproduzione al motore di playback.
     * Mantiene il Controller passivo delegando il controllo dello stato (es. libreria vuota) 
     * al Modello; intercetta eventuali eccezioni di dominio per fornire un feedback visivo all'utente.
     */
    @FXML
    private void handlePlayPause() {
        if (engine.isPlayingProperty().get()) {
            engine.pauseTrack();
        } else {
            if (engine.currentTrackProperty().get() != null) {
                engine.playTrack(engine.currentTrackProperty().get());
            } else {
                // Se non c'è nessun brano caricato, chiediamo al guscio padre di far partire il primo
                if (mainShell != null) {
                    mainShell.playFirstAvailableTrack();
                }
            }
        }
    }
}