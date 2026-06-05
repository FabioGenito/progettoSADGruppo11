/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.model.states.PlayerState;
import com.progettosad.mediaplayergruppo11.model.states.StoppedState;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.concurrent.CompletableFuture;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author irene e Fabio
 * Implementato tramite pattern Singleton per garantire che esista
 * una sola istanza Timeline e uno solo stato di ripoduzione
 */


public class PlaybackEngine {
    private static PlaybackEngine instance;
    private List<Track> currentQueue = new ArrayList<>();
    private int currentTrackIndex = -1;
    private PlayerState currentState;
    private Track currentTrack;
    private Timeline timeline;
    private int totalTime=0;

    
    // TASK T-06/03: Proprietà esposte per il Binding con l'interfaccia
    private final DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<Track> currentTrackProperty = new SimpleObjectProperty<>(null);
    private final BooleanProperty isPlayingProperty = new SimpleBooleanProperty(false);
    private final IntegerProperty currentTimeProperty = new SimpleIntegerProperty(0);
    
    public IntegerProperty currentTimeProperty() { return currentTimeProperty; }
    public DoubleProperty progressProperty() { return progressProperty; }
    public ObjectProperty<Track> currentTrackProperty() { return currentTrackProperty; }
    public BooleanProperty isPlayingProperty() { return isPlayingProperty; }
    
    //costruttore privato per inizializzare la Timeline
    //configurandola per aggiornare il tempo logico ogni secondo
    private PlaybackEngine(){
        this.currentState=new StoppedState();
       
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int current = currentTimeProperty.get() + 1;
            currentTimeProperty.set(current);        
            // TASK T-06/03: Aggiorna la proprietà in tempo reale (da 0.0 a 1.0 per la ProgressBar)
            if (totalTime > 0) {
                progressProperty.set((double) current / totalTime);
            }
            
            if (current >= totalTime){
                stopTrack();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    public static synchronized PlaybackEngine getInstance(){
        if(instance==null){
            instance = new PlaybackEngine();
        }
        return instance;
    }
    
    //Avvia la riproduzione di una traccia. Gestisce la logica di ripresa della pausa
    //o di caricamento di un nuovo brano
    public void playTrack(Track track){
        if (track==null)
            return;
        
        //verifica se si richiede di riprendere il prano precedentemente messo in pausa
        if (currentTrack != null && currentTrack.getId() == track.getId()){
            currentState.play(this);
            isPlayingProperty.set(true);
            return;
        }
        
        //se si cambia il brano l'esecuzione precedente viene interrotta
        if(currentTrack != null){
            stopTrack();
        }
        this.currentTrack=track;
        this.totalTime=track.getLength();
        
        // TASK T-06/03: Reset della barra e aggiornamento metadati
        this.currentTimeProperty.set(0);
        this.progressProperty.set(0.0); 
        this.currentTrackProperty.set(track);

        currentState.play(this);
        isPlayingProperty.set(true);
        
        CompletableFuture.runAsync(()->{
           TrackDAO dao = new TrackDAO();
           dao.incrementPlayCount(track.getId());
        });
    }
    
    public void pauseTrack(){
        currentState.pause(this);
        isPlayingProperty.set(false);
    }
    
    public void stopTrack(){
        currentState.stop(this);
        isPlayingProperty.set(false);
        progressProperty.set(0.0);
        currentTimeProperty.set(0);
    }
    
    public PlayerState getCurrentState(){
        return this.currentState;
    }
    
    public void setCurrentState(PlayerState state){
        this.currentState=state;
    }
    
    public Timeline getTimeLine(){
        return timeline;
    }
    
    public void setCurrentTime(int t){
        this.currentTimeProperty.set(t);
    }
    
        //T - 19/01: Backend – Riorganizzazione della Coda nel PlaybackEngine
    /**
     * Sposta una traccia all'interno della coda e ricalcola matematicamente
     * l'indice del brano in riproduzione per non interrompere il flusso.
     */
    public synchronized void moveTrackInQueue(int oldIndex, int newIndex){
        if (oldIndex < 0 || oldIndex >= currentQueue.size() || 
            newIndex < 0 || newIndex >= currentQueue.size() || oldIndex == newIndex) {
            return;
        }
        // Manipolazione della lista in memoria
        Track trackToMove = currentQueue.remove(oldIndex);
        currentQueue.add(newIndex, trackToMove);
        
        // Ricalcolo matematico di currentTrackIndex
        if (oldIndex == currentTrackIndex) {
            currentTrackIndex = newIndex;
        } else if (oldIndex < currentTrackIndex && newIndex >= currentTrackIndex) {
            currentTrackIndex--;
        } else if (oldIndex > currentTrackIndex && newIndex <= currentTrackIndex) {
            currentTrackIndex++;
        }
        // La Timeline non viene toccata, l'avanzamento lineare dei secondi 
        // continua senza subire sbalzi o reset di stato!
        System.out.println("Backend: Traccia spostata. Nuovo indice traccia attiva: " + currentTrackIndex);
    }
    
    /**
     * Determina la traccia da riprodurre garantendo un fallback controllato.
     * Se l'utente preme "Play" senza aver selezionato esplicitamente un brano, 
     * la regola di dominio impone di avviare automaticamente la prima traccia della lista.
     * @throws IllegalStateException se la lista fornita è vuota o inesistente.
     */
    public void playSelection(Track selectedTrack, List<Track> currentPlaylist) throws IllegalStateException {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) {
            throw new IllegalStateException("Non ci sono tracce disponibili. Impossibile avviare la riproduzione.");
        }

        Track trackToPlay = selectedTrack;
        if (trackToPlay == null) {
            trackToPlay = currentPlaylist.get(0);
        }

        playTrack(trackToPlay);
    }   
}
