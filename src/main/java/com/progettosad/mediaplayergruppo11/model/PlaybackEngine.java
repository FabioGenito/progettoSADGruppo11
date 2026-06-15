/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.model.states.PlayerState;
import com.progettosad.mediaplayergruppo11.model.states.StoppedState;
import com.progettosad.mediaplayergruppo11.model.strategy.PlaybackStrategy;
import com.progettosad.mediaplayergruppo11.model.strategy.SequentialStrategy;
import com.progettosad.mediaplayergruppo11.observer.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author irene e Fabio
 * Implementato tramite pattern Singleton per garantire che esista
 * una sola istanza Timeline e uno solo stato di ripoduzione
 */


public class PlaybackEngine implements Subject {
    
    private static PlaybackEngine instance;
    private ConcretePlaylistIterator playlistIterator;
    private PlayerState currentState;
    private Track currentTrack;
    private PlaybackStrategy currentStrategy = new SequentialStrategy();

    private final List<Observer> observers = new ArrayList<>();

    private volatile int totalTime = 0;
    private volatile int currentTime = 0;
    private volatile boolean isPlaying = false;
    private volatile double progress = 0.0;
    
    private ScheduledExecutorService timer;
       
    //costruttore privato per inizializzare la Timeline
    //configurandola per aggiornare il tempo logico ogni secondo
    private PlaybackEngine(){
        this.currentState=new StoppedState();
    }
    
    public static synchronized PlaybackEngine getInstance(){
        if(instance == null){
            instance = new PlaybackEngine();
        }
        return instance;
    }
    
    private void startTimer() {
        stopTimer();
        timer = Executors.newSingleThreadScheduledExecutor();
        
        timer.scheduleAtFixedRate(() -> {
            currentTime++;
            if (totalTime > 0) {
                progress = (double) currentTime / totalTime;
            }
            
            // 1. Notifica l'avanzamento alla View
            notifyObservers(new AppEvent(AppEventType.PLAYBACK_TIME_TICK, currentTime));
            
            // 2. Controllo brano terminato (Auto-Next)
            if (currentTime >= totalTime) {
                stopTimer();
                System.out.println("PlaybackEngine: Brano terminato: Auto Next!");
                // Usa un thread separato per non bloccare lo scheduler
                CompletableFuture.runAsync(this::nextTrack); 
            }
        }, 1, 1, TimeUnit.SECONDS); 
    }
    
    private void stopTimer() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdownNow();
        }
    }
    
    //Avvia la riproduzione di una traccia. Gestisce la logica di ripresa della pausa
    //o di caricamento di un nuovo brano
    public void playTrack(Track track){
        if (track==null) return;
        
        //verifica se si richiede di riprendere il prano precedentemente messo in pausa
        if (currentTrack != null && currentTrack.getId() == track.getId() && currentTime > 0 && currentTime < totalTime - 1) {
            currentState.play(this);
            this.isPlaying = true;
            startTimer(); 
            notifyObservers(new AppEvent(AppEventType.PLAYBACK_STATE_CHANGED, currentTrack));
            return;
        }
        
        stopTimer();
        if (currentState != null) {
            currentState.stop(this);
        }
        
        this.currentTrack = track;
        this.totalTime = track.getLength();
        this.currentTime = 0;
        this.progress = 0.0;
        this.isPlaying = true;

        currentState.play(this);
        startTimer(); 
        
        notifyObservers(new AppEvent(AppEventType.PLAYBACK_STATE_CHANGED, track));

        //T-08/02
        CompletableFuture.runAsync(() -> {
            try {
                TrackDAO dao = new TrackDAO();
                dao.incrementPlayCount(track.getId());
            } catch (Exception e) {
                // Gestione sicura nel thread separato per evitare crash a cascata
                System.err.println("Errore durante l'aggiornamento asincrono del contatore riproduzioni: " + e.getMessage());
            }
        });
    }
    
    public void pauseTrack(){
        stopTimer();
        this.isPlaying = false;
        currentState.pause(this);
        notifyObservers(new AppEvent(AppEventType.PLAYBACK_STATE_CHANGED, currentTrack));    
    }
    
    public void stopTrack(){
        stopTimer();
        this.isPlaying = false;
        this.currentTime = 0;
        this.progress = 0.0;
        currentState.stop(this);
        notifyObservers(new AppEvent(AppEventType.PLAYBACK_STATE_CHANGED, null));
    }
    
    
    /*
    * T-08/01: Metodo per determinare l'azione successiva usando l'iteratore
    *T-21/01: Modifica della logica di avanzamento automatico.
    *Calcola la traccia successiva leggendo la coda in tempo reale senza indici 
    *statici.
    */
    
    public void nextTrack(){
        //Delega allo stato corrente l'interruzione della timeline
        if(currentState!=null){
            currentState.stop(this);
        }
        
        //Verifica le precondizioni: se manca la coda o il brano corrente restituisce
        //un flag di errore
        if(playlistIterator != null && playlistIterator.getQueue()== null || currentTrack == null){
            stopTrack();
            return;
            }
        List<Track> currentQueue = playlistIterator.getQueue();
        
        //Ricerca della posizione esatta del brano corrente
        //effettuata confrontando l'ID univoco del brano per intercettare lo spostamento
        //a runtime
        int currentTrackIndex = -1;
        for(int i =0; i<currentQueue.size(); i++){
            if(currentQueue.get(i).getId()==currentTrack.getId()){
                currentTrackIndex=i;
                break;
            }
        }
        
        //Fallback di sizurezza. Se il codice non è più presente nella coda,
        // si preserva l'ultimo indice noto memorizzato
        if(currentTrackIndex==-1){
            currentTrackIndex=playlistIterator.getCurrentIndex();
        }
        
        //Sicronizzazione dell'iteratore con la posizione post-riordinamento
        playlistIterator.setCurrentIndex(currentTrackIndex);
        Track nextTrack=null;
        
        //comportamento coerente con la stategia attiva
        if(currentStrategy != null){
            nextTrack=currentStrategy.getNextTrack(currentQueue, currentTrackIndex);
        }
        
        if(nextTrack == null && (currentTrackIndex +1) < currentQueue.size()){
            nextTrack=currentQueue.get(currentTrackIndex + 1);
        }
        
        if(nextTrack!= null){
            int nextTrackIndex=currentQueue.indexOf(nextTrack);
            if(nextTrackIndex != -1){
                playlistIterator.setCurrentIndex(nextTrackIndex);
            }

            //T-08/02: il riutilizzo di playTrack garantisce che la logica asincrona
            //del DB venga ereditata automaticamente
            //T - 08/01: Backend – Logica Next e Coda di Riproduzione)
            this.currentTime = 0;
            playTrack(nextTrack);
        }else{
            //Nessun brano successivo: stoppa la ripdouzione
            stopTrack();
        }
    }
    
    public PlayerState getCurrentState(){
        return this.currentState;
    }
    
    public void setCurrentState(PlayerState state){
        this.currentState=state;
    }
    
    public double getProgress() {
        return progress; 
    }
    
    public int getCurrentTime() { 
        return currentTime; 
    }
    
    public Track getCurrentTrack() { 
        return currentTrack; 
    }
    
    public boolean isPlaying() { 
        return isPlaying; 
    }
    
    //T-11/01
    public void setPlaybackStrategy(PlaybackStrategy strategy) {
        if (strategy != null) {
            this.currentStrategy = strategy;
        }
    }
    
    public PlaybackStrategy getPlaybackStrategy() {
        return this.currentStrategy;
    }
        
    //T - 19/01: Backend – Riorganizzazione della Coda nel PlaybackEngine
    /**
     * Sposta una traccia all'interno della coda e ricalcola matematicamente
     * l'indice del brano in riproduzione per non interrompere il flusso.
     */
    public synchronized void moveTrackInQueue(int oldIndex, int newIndex){
        if (playlistIterator == null || playlistIterator.getQueue()==null){
            return;
        }
        List<Track> currentQueue=playlistIterator.getQueue();
        int currentTrackIndex=playlistIterator.getCurrentIndex();
        
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
        
        playlistIterator.setCurrentIndex(currentTrackIndex);
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
        int startIndex=0;
        if (trackToPlay == null) {
            trackToPlay = currentPlaylist.get(0);
        }else{
            startIndex=currentPlaylist.indexOf(trackToPlay);
            if(startIndex==-1){
                startIndex=0;
            }
        }
        //usiamo una independent Queue per non creare conflitto con la queue usata 
        //per modificare l'ordine dei brani in coda
        List<Track> independentQueue = new ArrayList<>(currentPlaylist);
        this.playlistIterator = new ConcretePlaylistIterator(independentQueue, startIndex);
        playTrack(trackToPlay);
    }
    
    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(AppEvent event) {
        for (Observer o : observers) {
            o.update(event);
        }
    }
}
