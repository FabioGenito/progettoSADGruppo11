/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author irene e Fabio
 * Implementato tramite pattern Singleton per garantire che esista
 * una sola istanza Timeline e uno solo stato di ripoduzione
 */


public class PlaybackEngine {
    private static PlaybackEngine instance;
    private PlayerState currentState;
    private Track currentTrack;
    private Timeline timeline;
    private int currentTime=0;
    private int totalTime=0;
    
    //costruttore privato per inizializzare la Timeline
    //configurandola per aggiornare il tempo logico ogni secondo
    private PlaybackEngine(){
        this.currentState=new StoppedState();
       
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        currentTime++;
        System.out.println("Tempo: "+ currentTime + "/" + totalTime);
        if (currentTime >= totalTime){
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
        if (currentTrack != null && currentTrack.getId()==track.getId()){
            currentState.play(this);
            return;
        }
        
        //se si cambia il brano l'esecuzione precedente viene interrotta
        if(currentTrack != null){
            stopTrack();
        }
        this.currentTrack=track;
        this.totalTime=track.getLength();
        currentState.play(this);
        
        //Tracciamento ascolti in backgrount
        //evita il blocco della UI
        CompletableFuture.runAsync(()->{
           TrackDAO dao = new TrackDAO();
           dao.incrementPlayCount(track.getId());
        });
    }
    
    public void pauseTrack(){
        currentState.pause(this);
    }
    
    public void stopTrack(){
        currentState.stop(this);
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
        this.currentTime=t;
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
