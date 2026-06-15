/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.controller;

import com.progettosad.mediaplayergruppo11.model.PlaybackEngine;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.model.strategy.*;
import com.progettosad.mediaplayergruppo11.observer.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

public class PlayerBarController implements Initializable, Observer {

    @FXML private Label currentTrackTitle;
    @FXML private Label currentTrackArtist;
    @FXML private Label currentTrackTime;
    @FXML private Label currentTrackDuration;
    @FXML private ProgressBar playerProgressBar;
    @FXML private Button playerButton;
    @FXML private Button skipButton;
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
        
        engine.attach(this);
        setupStrategies();

        updateTrackInfo(engine.getCurrentTrack());        
            
        if (playerButton != null) {
            playerButton.setText(engine.isPlaying() ? "||" : "▶");
        }
        if (playerProgressBar != null) {
            playerProgressBar.setProgress(engine.getProgress());
        }
    }
    
    @Override
    public void update(AppEvent event) {
        if(event == null) return;
        
        Platform.runLater(() -> {
            switch(event.getType()) {
                case PLAYBACK_TIME_TICK:
                    // Aggiorna il timer numerico e la barra di avanzamento visiva
                    int newTime = (Integer) event.getPayload();
                    if (currentTrackTime != null) {
                        currentTrackTime.setText(formatTime(newTime));   
                    }
                    if (playerProgressBar != null) {
                        playerProgressBar.setProgress(engine.getProgress());
                    }
                    break;

                case PLAYBACK_STATE_CHANGED:
                    // Aggiorna le etichette del brano e il bottone Play/Pausa
                    Track track = (Track) event.getPayload();
                    updateTrackInfo(track);
                    
                    if (playerButton != null) {
                        playerButton.setText(engine.isPlaying() ? "||" : "▶");
                    }
                    
                    // Se la riproduzione è stata stoppata completamente (es. fine libreria)
                    if (playerProgressBar != null && !engine.isPlaying() && track == null) {
                        playerProgressBar.setProgress(0.0);
                        if (currentTrackTime != null) currentTrackTime.setText("00:00");
                    }
                    break;
                    
                default:
                    // Eventi non di competenza di questo controller
                    break; 
            }
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
        if (engine.isPlaying()) {
            engine.pauseTrack();
        } else {
            if (engine.getCurrentTrack() != null) {
                engine.playTrack(engine.getCurrentTrack());
            } else {
                // Se non c'è nessun brano caricato, chiediamo al guscio padre di far partire il primo
                if (mainShell != null) {
                    mainShell.playFirstAvailableTrack();
                }
            }
        }
    }
    
    /**
    * Converte i secondi correnti di riproduzione nel formato standard "MM:SS" per la UI.
    */
   private String formatTime(int totalSeconds) {
       if (totalSeconds < 0) return "00:00";
       int minuti = totalSeconds / 60;
       int secondi = totalSeconds % 60;
       return String.format("%02d:%02d", minuti, secondi);
   }
}