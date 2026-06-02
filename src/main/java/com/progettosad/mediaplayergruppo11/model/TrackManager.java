/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.model;
import com.progettosad.mediaplayergruppo11.model.Track;
import com.progettosad.mediaplayergruppo11.dao.TrackDAO;
import com.progettosad.mediaplayergruppo11.dao.TrackDAOInterface;
import com.progettosad.mediaplayergruppo11.observer.Observer;
import com.progettosad.mediaplayergruppo11.observer.Subject;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author gcucc e Fabio
 */
public class TrackManager implements Subject{
    
    public static final String EVENT_TRACK_ADDED = "ADDED_TRACK";
    public static final String EVENT_TRACK_DELETED = "DELETED_TRACK";
    public static final String EVENT_TRACK_UPDATED = "UPDATED_TRACK";
    
    private static TrackManager instance;
    private String state;
    private Track lastProcessedTrack;
    private TrackDAOInterface dao; 
    private List<Observer> observers = new ArrayList<>();
    
    // Singleton pattern
    public static synchronized TrackManager getInstance() {
        if (instance == null) {
            instance = new TrackManager();
        }
        return instance;
    }

    private TrackManager() {
        this.dao = new TrackDAO();
    }
    
    public void setDao(TrackDAOInterface dao) {
        this.dao = dao;
    }

    public Track insertNewTrack(Track track) {
        return dao.insertTrack(track); 
    }

    public void notifyTrackAdded(Track track) {
        this.lastProcessedTrack = track;
        this.state = EVENT_TRACK_ADDED;
        notifyObservers();
    }
    
    public void notifyTrackUpdated(Track track) {
        this.lastProcessedTrack = track;
        this.state = EVENT_TRACK_UPDATED;
        notifyObservers();
    }
    
    public Track getLastProcessedTrack() {
        return lastProcessedTrack;
    }
    
    
    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    public String getState() {
        return state;
    }


    public void deleteTrack(int trackId) {
        boolean isDeleted = dao.deleteTrack(trackId);
        if (isDeleted) {
            this.state = EVENT_TRACK_DELETED + trackId;
            System.out.println("TrackManager: Traccia eliminata dal DB. Emetto notifica agli Observer...");
            notifyObservers();
        } else {
            System.err.println("TrackManager: Impossibile eliminare la traccia (ID non trovato o errore DB).");
        }
    }
    
    /**
     * Coordina l'aggiornamento di una traccia, gestendo gli errori e le notifiche.
     * @param track L'oggetto Track con i dati modificati
     */
    public Track updateTrack(Track track) {
        try {
            boolean isUpdated = dao.updateTrack(track);

            if (isUpdated) {
                System.out.println("TrackManager: Traccia aggiornata nel DB. Emetto notifica agli Observer...");
            } 
        } catch (IllegalArgumentException e) {
            System.err.println("TrackManager Errore di Validazione: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            System.err.println("TrackManager Errore di Sistema: Impossibile comunicare con il database.");
            e.printStackTrace(); 
            throw e;
        }
        return track;
    }
    
    /**
     * Recupera l'elenco completo delle tracce delegando l'operazione al DAO.
     * Gestisce i potenziali errori di sistema per proteggere il ciclo di vita della UI.
     * @return Una lista di oggetti Track ordinati per titolo, oppure una lista vuota in caso di errore.
     */
    public List<Track> getAllTracks() {
        try {
            return dao.getAllTracks();
            
        } catch (RuntimeException e) {
            System.err.println("TrackManager Errore di Sistema: Impossibile recuperare il catalogo musicale.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
